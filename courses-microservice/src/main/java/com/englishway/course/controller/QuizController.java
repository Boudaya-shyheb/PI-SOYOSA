package com.englishway.course.controller;

import com.englishway.course.dto.QuizResponse;
import com.englishway.course.model.QuizRequest;
import com.englishway.course.service.QuizService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse createQuiz(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody QuizRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return quizService.createQuiz(context, request);
    }

    @PutMapping("/{quizId}")
    public QuizResponse updateQuiz(
        @PathVariable("quizId") UUID quizId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody QuizRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return quizService.updateQuiz(context, quizId, request);
    }

    @DeleteMapping("/{quizId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(
        @PathVariable("quizId") UUID quizId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        quizService.deleteQuiz(context, quizId);
    }

    @GetMapping("/{quizId}")
    public QuizResponse getQuiz(@PathVariable("quizId") UUID quizId) {
        return quizService.getQuiz(quizId);
    }

    @GetMapping("/lesson/{lessonId}")
    public QuizResponse getQuizByLesson(@PathVariable("lessonId") UUID lessonId) {
        return quizService.getQuizByLesson(lessonId);
    }

    @GetMapping("/course/{courseId}")
    public List<QuizResponse> getQuizzesByCourse(@PathVariable("courseId") UUID courseId) {
        return quizService.getQuizzesByCourse(courseId);
    }
}
