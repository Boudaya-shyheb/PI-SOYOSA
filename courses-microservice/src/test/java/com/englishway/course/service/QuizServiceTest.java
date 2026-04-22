package com.englishway.course.service;

import com.englishway.course.model.QuizRequest;
import com.englishway.course.dto.QuizQuestionDto;
import com.englishway.course.dto.QuizResponse;
import com.englishway.course.entity.Chapter;
import com.englishway.course.entity.Course;
import com.englishway.course.entity.Lesson;
import com.englishway.course.entity.Quiz;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.repository.CourseRepository;
import com.englishway.course.repository.LessonRepository;
import com.englishway.course.repository.QuizRepository;
import com.englishway.course.util.RequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService Tests")
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private AccessControlService accessControlService;

    @InjectMocks
    private QuizService quizService;

    private RequestContext tutorContext;
    private UUID courseId;
    private UUID lessonId;
    private UUID quizId;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        quizService = new QuizService(quizRepository, lessonRepository, courseRepository, accessControlService, new ObjectMapper());

        tutorContext = RequestContext.fromHeaders("tutor-1", "TUTOR");
        courseId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        quizId = UUID.randomUUID();

        Course course = new Course();
        course.setId(courseId);
        course.setTutorId("tutor-1");

        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setCourse(course);

        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setChapter(chapter);
        lesson.setTitle("Final Quiz Lesson");
        lesson.setOrderIndex(1);
        lesson.setXpReward(50);
    }

    @Test
    @DisplayName("createQuiz: creates quiz and auto-generates missing question ids")
    void createQuiz_success_generatesQuestionIds() {
        QuizRequest request = new QuizRequest();
        request.setCourseId(courseId);
        request.setLessonId(lessonId);
        request.setTitle("Final Exam");
        request.setTimeLimitMin(20);
        request.setPassingScore(70);
        request.setMaxAttempts(2);
        request.setCooldownMin(10);

        QuizQuestionDto question = new QuizQuestionDto();
        question.setId(" ");
        question.setText("2 + 2 = ?");
        question.setOptions(List.of("3", "4", "5"));
        question.setCorrectIndex(1);
        request.setQuestions(new ObjectMapper().valueToTree(List.of(question)));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(lesson.getChapter().getCourse()));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(quizRepository.existsByLessonId(lessonId)).thenReturn(false);
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz quiz = invocation.getArgument(0);
            quiz.setId(quizId);
            return quiz;
        });

        QuizResponse response = quizService.createQuiz(tutorContext, request);

        assertThat(response.getId()).isEqualTo(quizId);
        assertThat(response.getCourseId()).isEqualTo(courseId);
        assertThat(response.getLessonId()).isEqualTo(lessonId);
        assertThat(response.getQuestions()).hasSize(1);
        assertThat(response.getQuestions().get(0).getId()).startsWith("q-");

        ArgumentCaptor<Quiz> captor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizRepository).save(captor.capture());
        verify(accessControlService).requireAuthenticated(tutorContext);
        verify(accessControlService).requireContentWrite(tutorContext.getRole());
        verify(accessControlService).requireCourseOwnership(tutorContext, "tutor-1");
        assertThat(captor.getValue().getTitle()).isEqualTo("Final Exam");
    }

    @Test
    @DisplayName("createQuiz: throws BadRequestException when lesson already has quiz")
    void createQuiz_duplicateLessonQuiz_throws() {
        QuizRequest request = new QuizRequest();
        request.setCourseId(courseId);
        request.setLessonId(lessonId);
        request.setTitle("Duplicate quiz");
        request.setTimeLimitMin(10);
        request.setPassingScore(60);
        request.setMaxAttempts(1);
        request.setCooldownMin(0);
        request.setQuestions(new ObjectMapper().valueToTree(List.of(new QuizQuestionDto("q1", "Question", List.of("A", "B"), 0))));

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(lesson.getChapter().getCourse()));
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(quizRepository.existsByLessonId(lessonId)).thenReturn(true);

        assertThatThrownBy(() -> quizService.createQuiz(tutorContext, request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("already exists");

        verify(quizRepository, never()).save(any(Quiz.class));
    }

    @Test
    @DisplayName("calculateScore: returns rounded percentage from answers")
    void calculateScore_returnsRoundedPercent() {
        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setLesson(lesson);
        quiz.setTitle("Math quiz");
        quiz.setPassingScore(70);
        quiz.setTimeLimitMin(10);
        quiz.setMaxAttempts(1);
        quiz.setCooldownMin(0);
        quiz.setQuestions(new ObjectMapper().valueToTree(List.of(
            new QuizQuestionDto("q1", "Q1", List.of("A", "B"), 0),
            new QuizQuestionDto("q2", "Q2", List.of("A", "B"), 1),
            new QuizQuestionDto("q3", "Q3", List.of("A", "B"), 1)
        )));

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        int score = quizService.calculateScore(quizId, List.of(0, 1, 0));

        assertThat(score).isEqualTo(67);
    }

    @Test
    @DisplayName("isPassing: returns true only when score meets threshold")
    void isPassing_usesConfiguredPassingScore() {
        Quiz quiz = new Quiz();
        quiz.setId(quizId);
        quiz.setLesson(lesson);
        quiz.setPassingScore(75);
        quiz.setQuestions(new ObjectMapper().valueToTree(List.of(
            new QuizQuestionDto("q1", "Q1", List.of("A", "B"), 0)
        )));

        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        assertThat(quizService.isPassing(quizId, 74)).isFalse();
        assertThat(quizService.isPassing(quizId, 75)).isTrue();
    }
}