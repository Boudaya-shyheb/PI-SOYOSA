package com.englishway.course.service;

import com.englishway.course.dto.QuizQuestionDto;
import com.englishway.course.dto.QuizSubmissionGradeRequest;
import com.englishway.course.dto.QuizSubmissionRequest;
import com.englishway.course.dto.QuizSubmissionResponse;
import com.englishway.course.entity.Lesson;
import com.englishway.course.entity.Quiz;
import com.englishway.course.entity.QuizSubmission;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.repository.QuizRepository;
import com.englishway.course.repository.QuizSubmissionRepository;
import com.englishway.course.repository.EnrollmentRepository;
import com.englishway.course.client.UserServiceClient;
import com.englishway.course.util.RequestContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizSubmissionService {
    private final QuizSubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final AccessControlService accessControlService;
    private final ObjectMapper objectMapper;
    private final EnrollmentRepository enrollmentRepository;
    private final UserServiceFeignService userServiceFeignService;

    public QuizSubmissionService(
        QuizSubmissionRepository submissionRepository,
        LessonRepository lessonRepository,
        QuizRepository quizRepository,
        AccessControlService accessControlService,
        ObjectMapper objectMapper,
        EnrollmentRepository enrollmentRepository,
        UserServiceFeignService userServiceFeignService
    ) {
        this.submissionRepository = submissionRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.accessControlService = accessControlService;
        this.objectMapper = objectMapper;
        this.enrollmentRepository = enrollmentRepository;
        this.userServiceFeignService = userServiceFeignService;
    }

    @Transactional
    public QuizSubmissionResponse submitQuiz(RequestContext context, UUID quizId, QuizSubmissionRequest request) {
        accessControlService.requireStudent(context);

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        Lesson lesson = quiz.getLesson();

        long attemptsUsed = submissionRepository.countByQuizIdAndStudentId(quizId, context.getUserId());
        int maxAttempts = quiz.getMaxAttempts() == null ? 1 : Math.max(1, quiz.getMaxAttempts());
        int cooldownMin = quiz.getCooldownMin() == null ? 0 : Math.max(0, quiz.getCooldownMin());

        if (attemptsUsed >= maxAttempts) {
            throw new BadRequestException("Maximum quiz attempts reached");
        }

        if (attemptsUsed > 0 && cooldownMin > 0) {
            submissionRepository.findTopByQuizIdAndStudentIdOrderBySubmittedAtDesc(quizId, context.getUserId())
                .ifPresent(lastAttempt -> {
                    Instant nextAllowedAt = lastAttempt.getSubmittedAt().plusSeconds((long) cooldownMin * 60);
                    if (Instant.now().isBefore(nextAllowedAt)) {
                        throw new BadRequestException("Quiz retry cooldown active. Please try again later.");
                    }
                });
        }
        
        // Verify student is enrolled in the course
        UUID courseId = lesson.getChapter().getCourse().getId();
        enrollmentRepository.findByCourseIdAndUserId(courseId, context.getUserId())
            .orElseThrow(() -> new BadRequestException("Student is not enrolled in this course"));

        QuizSubmission submission = new QuizSubmission();
        submission.setQuizId(quizId);
        submission.setCourseId(lesson.getChapter().getCourse().getId());
        submission.setStudentId(context.getUserId());
        submission.setAnswers(writeAnswers(request.getAnswers()));
        
        // Auto-calculate score from quiz questions
        int calculatedScore = calculateScoreFromQuiz(quizId, request.getAnswers());
        submission.setScore(calculatedScore);
        submission.setGradedAt(Instant.now());
        submission.setGradedBy("AUTO");
        
        QuizSubmission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    /**
     * Calculate score by comparing answers with quiz questions.
     */
    private int calculateScoreFromQuiz(UUID quizId, List<Object> answers) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);
        if (quizOpt.isEmpty()) {
            return 0;
        }
        
        Quiz quiz = quizOpt.get();
        List<QuizQuestionDto> questions = jsonToQuestionDtos(quiz.getQuestions());
        if (questions.isEmpty()) {
            return 0;
        }
        
        int correct = 0;
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            QuizQuestionDto q = questions.get(i);
            Object userAnswer = answers.get(i);
            if (userAnswer == null) continue;
            
            if ("FILL_BLANK".equals(q.getType()) || "SHORT_ANSWER".equals(q.getType())) {
                if (q.getCorrectAnswer() != null && q.getCorrectAnswer().equalsIgnoreCase(userAnswer.toString().trim())) {
                    correct++;
                }
            } else {
                if (q.getCorrectIndex() != null) {
                    try {
                        int ansInt = Integer.parseInt(userAnswer.toString());
                        if (q.getCorrectIndex() == ansInt) correct++;
                    } catch (NumberFormatException e) { }
                }
            }
        }
        
        return Math.round((float) correct / questions.size() * 100);
    }

    private List<QuizQuestionDto> jsonToQuestionDtos(JsonNode json) {
        if (json == null || json.isNull()) {
            return Collections.emptyList();
        }
        return objectMapper.convertValue(json, new TypeReference<List<QuizQuestionDto>>() {});
    }


    public List<QuizSubmissionResponse> listSubmissions(RequestContext context, UUID quizId) {
        accessControlService.requireContentWrite(context.getRole());
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        accessControlService.requireCourseOwnership(context, quiz.getLesson().getChapter().getCourse().getTutorId());
        return submissionRepository.findByQuizIdOrderBySubmittedAtDesc(quizId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public List<QuizSubmissionResponse> getLeaderboard(UUID quizId) {
        return submissionRepository.findTop10ByQuizIdOrderByScoreDesc(quizId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public QuizSubmissionResponse gradeSubmission(RequestContext context, UUID submissionId, QuizSubmissionGradeRequest request) {
        accessControlService.requireContentWrite(context.getRole());
        QuizSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Quiz submission not found"));
        Quiz quiz = quizRepository.findById(submission.getQuizId())
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        accessControlService.requireCourseOwnership(context, quiz.getLesson().getChapter().getCourse().getTutorId());
        submission.setScore(request.getScore());
        submission.setGradedAt(Instant.now());
        submission.setGradedBy(context.getUserId());
        QuizSubmission saved = submissionRepository.save(submission);
        return toResponse(saved);
    }

    public List<QuizSubmissionResponse> getMyResults(RequestContext context) {
        if (context.getRole() != com.englishway.course.enums.Role.STUDENT) {
            return Collections.emptyList();
        }
        return submissionRepository.findByStudentIdOrderBySubmittedAtDesc(context.getUserId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public QuizSubmissionResponse getMySubmissionByLesson(RequestContext context, UUID lessonId) {
        if (context.getRole() != com.englishway.course.enums.Role.STUDENT) {
            return null;
        }
        return quizRepository.findByLessonId(lessonId)
            .flatMap(quiz -> submissionRepository.findTopByQuizIdAndStudentIdOrderBySubmittedAtDesc(quiz.getId(), context.getUserId()))
            .map(this::toResponse)
            .orElse(null);
    }

    private QuizSubmissionResponse toResponse(QuizSubmission submission) {
        QuizSubmissionResponse response = new QuizSubmissionResponse();
        response.setId(submission.getId());
        response.setCourseId(submission.getCourseId());
        response.setQuizId(submission.getQuizId());
        response.setStudentId(submission.getStudentId());
        
        // Fetch and set student name
        try {
            UserServiceClient.UserInfoDto userInfo = userServiceFeignService.getUserInfo(submission.getStudentId());
            response.setStudentName(userInfo.getDisplayName());
        } catch (Exception e) {
            response.setStudentName(submission.getStudentId());
        }
        response.setAnswers(readAnswers(submission.getAnswers()));
        response.setScore(submission.getScore());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setGradedAt(submission.getGradedAt());
        response.setGradedBy(submission.getGradedBy());
        
        // Add passing information from quiz
        Optional<Quiz> quizOpt = quizRepository.findById(submission.getQuizId());
        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();
            response.setLessonId(quiz.getLesson().getId());
            response.setPassingScore(quiz.getPassingScore());
            if (submission.getScore() != null) {
                response.setPassed(submission.getScore() >= quiz.getPassingScore());
            }
        }
        
        return response;
    }

    private List<Object> readAnswers(JsonNode answers) {
        if (answers == null || answers.isNull()) {
            return Collections.emptyList();
        }
        return objectMapper.convertValue(answers, new TypeReference<List<Object>>() {});
    }

    private JsonNode writeAnswers(List<Object> answers) {
        List<Object> safeAnswers = answers == null ? Collections.emptyList() : answers;
        return objectMapper.valueToTree(safeAnswers);
    }

    @Transactional
    public void deleteSubmission(RequestContext context, UUID submissionId) {
        QuizSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new NotFoundException("Quiz submission not found"));
        Quiz quiz = quizRepository.findById(submission.getQuizId())
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        
        // Students can only delete their own submissions; teachers can delete any
        if (!context.getUserId().equals(submission.getStudentId())) {
            accessControlService.requireContentWrite(context.getRole());
        }
        
        submissionRepository.deleteById(submissionId);
    }
}
