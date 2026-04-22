package com.englishway.course.service;

import com.englishway.course.model.QuizRequest;
import com.englishway.course.dto.QuizQuestionDto;
import com.englishway.course.dto.QuizResponse;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Lesson;
import com.englishway.course.entity.Quiz;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.exception.NotFoundException;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.repository.QuizRepository;
import com.englishway.course.util.RequestContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;
    private final ObjectMapper objectMapper;

    public QuizService(
        QuizRepository quizRepository,
        LessonRepository lessonRepository,
        CourseRepository courseRepository,
        AccessControlService accessControlService,
        ObjectMapper objectMapper
    ) {
        this.quizRepository = quizRepository;
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.accessControlService = accessControlService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public QuizResponse createQuiz(RequestContext context, QuizRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());

        if (request.getCourseId() == null) {
            throw new BadRequestException("Course ID is required");
        }
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new NotFoundException("Course not found"));
        accessControlService.requireCourseOwnership(context, course.getTutorId());

        Lesson lesson = null;
        if (request.getLessonId() != null) {
            lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new NotFoundException("Lesson not found"));
            
            if (lesson.getChapter() == null || lesson.getChapter().getCourse() == null) {
                throw new BadRequestException("Lesson is not correctly linked to a course hierarchy");
            }
            
            if (!lesson.getChapter().getCourse().getId().equals(course.getId())) {
                throw new BadRequestException("Lesson does not belong to the specified course");
            }
            if (quizRepository.existsByLessonId(request.getLessonId())) {
                throw new BadRequestException("A quiz already exists for this lesson");
            }
        }

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setLesson(lesson);
        quiz.setTitle(request.getTitle() != null ? request.getTitle() : "Untitled Quiz");
        quiz.setTimeLimitMin(request.getTimeLimitMin());
        quiz.setPassingScore(request.getPassingScore() != null ? request.getPassingScore() : 70);
        quiz.setMaxAttempts(request.getMaxAttempts() != null ? request.getMaxAttempts() : 1);
        quiz.setCooldownMin(request.getCooldownMin() != null ? request.getCooldownMin() : 0);
        
        try {
            quiz.setQuestions(questionsToJson(request.getQuestions()));
        } catch (Exception e) {
            throw new BadRequestException("Invalid questions format: " + e.getMessage());
        }

        try {
            Quiz saved = quizRepository.save(quiz);
            return toResponse(saved);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to save quiz: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save quiz due to database error: " + e.getMessage());
        }
    }

    @Transactional
    public QuizResponse updateQuiz(RequestContext context, UUID quizId, QuizRequest request) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        accessControlService.requireCourseOwnership(context, quiz.getCourse().getTutorId());

        quiz.setTitle(request.getTitle());
        quiz.setTimeLimitMin(request.getTimeLimitMin());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setMaxAttempts(request.getMaxAttempts());
        quiz.setCooldownMin(request.getCooldownMin());
        quiz.setQuestions(questionsToJson(request.getQuestions()));

        Quiz saved = quizRepository.save(quiz);
        return toResponse(saved);
    }

    private JsonNode questionsToJson(JsonNode questionsNode) {
        if (questionsNode == null || !questionsNode.isArray()) {
            return objectMapper.createArrayNode();
        }
        try {
            List<QuizQuestionDto> questions = objectMapper.convertValue(questionsNode, new TypeReference<List<QuizQuestionDto>>() {});
            questions.forEach(q -> {
                if (q.getId() == null || q.getId().trim().isEmpty()) {
                    q.setId("q-" + UUID.randomUUID().toString().substring(0, 8));
                }
            });
            return objectMapper.valueToTree(questions);
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to parse questions JSON: " + e.getMessage());
            return questionsNode; // Fallback to raw node if conversion fails
        }
    }

    @Transactional
    public void deleteQuiz(RequestContext context, UUID quizId) {
        accessControlService.requireAuthenticated(context);
        accessControlService.requireContentWrite(context.getRole());

        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        accessControlService.requireCourseOwnership(context, quiz.getCourse().getTutorId());

        quizRepository.deleteById(quizId);
    }

    public QuizResponse getQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        return toResponse(quiz);
    }

    public QuizResponse getQuizByLesson(UUID lessonId) {
        Quiz quiz = quizRepository.findByLessonId(lessonId)
            .orElseThrow(() -> new NotFoundException("No quiz found for this lesson"));
        return toResponse(quiz);
    }

    public List<QuizResponse> getQuizzesByCourse(UUID courseId) {
        return quizRepository.findByCourseId(courseId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Calculate the score for a quiz submission.
     * Returns the percentage score (0-100).
     */
    public int calculateScore(UUID quizId, List<Integer> answers) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        
        List<QuizQuestionDto> questions = jsonToQuestions(quiz.getQuestions());
        if (questions.isEmpty()) {
            return 0;
        }
        
        int correct = 0;
        for (int i = 0; i < questions.size() && i < answers.size(); i++) {
            Integer correctIndex = questions.get(i).getCorrectIndex();
            if (correctIndex != null && correctIndex.equals(answers.get(i))) {
                correct++;
            }
        }
        
        return Math.round((float) correct / questions.size() * 100);
    }

    /**
     * Check if the score meets the passing threshold.
     */
    public boolean isPassing(UUID quizId, int score) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new NotFoundException("Quiz not found"));
        return score >= quiz.getPassingScore();
    }

    private QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(
            quiz.getId(),
            quiz.getLesson() != null ? quiz.getLesson().getId() : null,
            quiz.getCourse().getId(),
            quiz.getTitle(),
            jsonToQuestions(quiz.getQuestions()),
            quiz.getTimeLimitMin(),
            quiz.getPassingScore(),
            quiz.getMaxAttempts(),
            quiz.getCooldownMin(),
            quiz.getCreatedAt(),
            quiz.getUpdatedAt()
        );
    }

    private QuizResponse toSimpleResponse(Quiz quiz) {
        return new QuizResponse(
            quiz.getId(),
            quiz.getLesson() != null ? quiz.getLesson().getId() : null,
            quiz.getCourse().getId(),
            quiz.getTitle(),
            Collections.emptyList(), // No questions in simple response
            quiz.getTimeLimitMin(),
            quiz.getPassingScore(),
            quiz.getMaxAttempts(),
            quiz.getCooldownMin(),
            quiz.getCreatedAt(),
            quiz.getUpdatedAt()
        );
    }

    private List<QuizQuestionDto> jsonToQuestions(JsonNode json) {
        if (json == null || json.isNull()) {
            return Collections.emptyList();
        }
        return objectMapper.convertValue(json, new TypeReference<List<QuizQuestionDto>>() {});
    }

}
