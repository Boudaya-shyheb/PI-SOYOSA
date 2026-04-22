package com.englishway.course.service;

import com.englishway.course.enums.Role;
import com.englishway.course.exception.AccessDeniedException;
import com.englishway.course.util.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccessControlService Tests")
class AccessControlServiceTest {

    private AccessControlService accessControlService;

    @BeforeEach
    void setUp() {
        accessControlService = new AccessControlService();
    }

    // ─── requireAuthenticated ────────────────────────────────────────────────────

    @Test
    @DisplayName("requireAuthenticated: passes when userId is present")
    void requireAuthenticated_valid() {
        RequestContext context = RequestContext.fromHeaders("user-1", "STUDENT");
        assertThatCode(() -> accessControlService.requireAuthenticated(context))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireAuthenticated: throws when userId is null")
    void requireAuthenticated_noUser_throws() {
        RequestContext context = RequestContext.fromHeaders(null, "STUDENT");
        assertThatThrownBy(() -> accessControlService.requireAuthenticated(context))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Authentication required");
    }

    @Test
    @DisplayName("requireAuthenticated: throws when userId is blank")
    void requireAuthenticated_blankUser_throws() {
        RequestContext context = RequestContext.fromHeaders("", "STUDENT");
        assertThatThrownBy(() -> accessControlService.requireAuthenticated(context))
            .isInstanceOf(AccessDeniedException.class);
    }

    // ─── requireStudent ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("requireStudent: passes for STUDENT role")
    void requireStudent_studentRole_passes() {
        RequestContext context = RequestContext.fromHeaders("student-1", "STUDENT");
        assertThatCode(() -> accessControlService.requireStudent(context))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireStudent: throws for TUTOR role")
    void requireStudent_tutorRole_throws() {
        RequestContext context = RequestContext.fromHeaders("tutor-1", "TUTOR");
        assertThatThrownBy(() -> accessControlService.requireStudent(context))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("STUDENT");
    }

    @Test
    @DisplayName("requireStudent: throws for ADMIN role")
    void requireStudent_adminRole_throws() {
        RequestContext context = RequestContext.fromHeaders("admin-1", "ADMIN");
        assertThatThrownBy(() -> accessControlService.requireStudent(context))
            .isInstanceOf(AccessDeniedException.class);
    }

    // ─── requireContentWrite ─────────────────────────────────────────────────────

    @Test
    @DisplayName("requireContentWrite: passes for TUTOR role")
    void requireContentWrite_tutorRole_passes() {
        assertThatCode(() -> accessControlService.requireContentWrite(Role.TUTOR))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireContentWrite: passes for ADMIN role")
    void requireContentWrite_adminRole_passes() {
        assertThatCode(() -> accessControlService.requireContentWrite(Role.ADMIN))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireContentWrite: passes for TEACHER role")
    void requireContentWrite_teacherRole_passes() {
        assertThatCode(() -> accessControlService.requireContentWrite(Role.TEACHER))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireContentWrite: throws for STUDENT role")
    void requireContentWrite_studentRole_throws() {
        assertThatThrownBy(() -> accessControlService.requireContentWrite(Role.STUDENT))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("ADMIN or TUTOR");
    }

    // ─── requireCourseOwnership ──────────────────────────────────────────────────

    @Test
    @DisplayName("requireCourseOwnership: ADMIN bypasses ownership check")
    void requireCourseOwnership_adminBypasses() {
        RequestContext adminContext = RequestContext.fromHeaders("admin-1", "ADMIN");
        assertThatCode(() -> accessControlService.requireCourseOwnership(adminContext, "other-tutor"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireCourseOwnership: TUTOR passes when they own the course")
    void requireCourseOwnership_ownCourse_passes() {
        RequestContext tutorContext = RequestContext.fromHeaders("tutor-1", "TUTOR");
        assertThatCode(() -> accessControlService.requireCourseOwnership(tutorContext, "tutor-1"))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("requireCourseOwnership: TUTOR fails when they don't own the course")
    void requireCourseOwnership_notOwner_throws() {
        RequestContext tutorContext = RequestContext.fromHeaders("tutor-1", "TUTOR");
        assertThatThrownBy(() -> accessControlService.requireCourseOwnership(tutorContext, "other-tutor"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("your own courses");
    }

    @Test
    @DisplayName("requireCourseOwnership: throws for STUDENT role")
    void requireCourseOwnership_studentRole_throws() {
        RequestContext studentContext = RequestContext.fromHeaders("student-1", "STUDENT");
        assertThatThrownBy(() -> accessControlService.requireCourseOwnership(studentContext, "student-1"))
            .isInstanceOf(AccessDeniedException.class);
    }
}

