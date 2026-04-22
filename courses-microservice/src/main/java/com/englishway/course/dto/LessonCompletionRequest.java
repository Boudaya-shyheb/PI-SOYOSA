package com.englishway.course.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class LessonCompletionRequest {
    @NotNull
    private UUID lessonId;

    public UUID getLessonId() {
        return lessonId;
    }

    public void setLessonId(UUID lessonId) {
        this.lessonId = lessonId;
    }
}
