package com.englishway.course.controller;

import com.englishway.course.dto.QuizSubmissionGradeRequest;
import com.englishway.course.dto.QuizSubmissionRequest;
import com.englishway.course.dto.QuizSubmissionResponse;
import com.englishway.course.service.AccessControlService;
import com.englishway.course.service.QuizSubmissionService;
import com.englishway.course.util.RequestContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuizSubmissionController {
    private final QuizSubmissionService submissionService;
    private final AccessControlService accessControlService;

    public QuizSubmissionController(QuizSubmissionService submissionService, AccessControlService accessControlService) {
        this.submissionService = submissionService;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/quiz/{quizId}/submit")
    public QuizSubmissionResponse submitQuiz(
        @PathVariable("quizId") UUID quizId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody QuizSubmissionRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return submissionService.submitQuiz(context, quizId, request);
    }

    @GetMapping("/quiz/{quizId}/submissions")
    public List<QuizSubmissionResponse> listSubmissions(
        @PathVariable("quizId") UUID quizId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return submissionService.listSubmissions(context, quizId);
    }

    @GetMapping("/quiz/{quizId}/leaderboard")
    public List<QuizSubmissionResponse> getLeaderboard(
        @PathVariable("quizId") UUID quizId
    ) {
        return submissionService.getLeaderboard(quizId);
    }

    @PutMapping("/quiz/submission/{submissionId}/grade")
    public QuizSubmissionResponse gradeSubmission(
        @PathVariable("submissionId") UUID submissionId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role,
        @Valid @RequestBody QuizSubmissionGradeRequest request
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        accessControlService.requireContentWrite(context.getRole());
        return submissionService.gradeSubmission(context, submissionId, request);
    }

    @GetMapping("/student/my-results")
    public List<QuizSubmissionResponse> getMyResults(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return submissionService.getMyResults(context);
    }

    @GetMapping("/quiz/{lessonId}/my-submission")
    public QuizSubmissionResponse getMySubmissionByLesson(
        @PathVariable("lessonId") UUID lessonId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        return submissionService.getMySubmissionByLesson(context, lessonId);
    }

    @DeleteMapping("/quiz/submission/{submissionId}")
    public void deleteSubmission(
        @PathVariable("submissionId") UUID submissionId,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-Role", required = false) String role
    ) {
        RequestContext context = RequestContext.fromHeaders(userId, role);
        submissionService.deleteSubmission(context, submissionId);
    }
}
