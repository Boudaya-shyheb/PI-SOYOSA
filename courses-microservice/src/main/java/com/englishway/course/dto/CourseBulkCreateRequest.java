package com.englishway.course.dto;

import com.englishway.course.enums.Level;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class CourseBulkCreateRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Level level;

    @NotNull
    @Min(1)
    private Integer capacity;

    private Boolean active;
    private Boolean isPaid;

    @DecimalMin(value = "0.00")
    private BigDecimal price;

    private String imageUrl;

    @Valid
    private List<ChapterBulkRequest> chapters;

    public static class ChapterBulkRequest {
        @NotBlank
        private String title;
        
        @NotNull
        private Integer orderIndex;

        @Valid
        private List<LessonBulkRequest> lessons;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public List<LessonBulkRequest> getLessons() { return lessons; }
        public void setLessons(List<LessonBulkRequest> lessons) { this.lessons = lessons; }
    }

    public static class LessonBulkRequest {
        @NotBlank
        private String title;
        
        @NotNull
        private Integer orderIndex;
        
        private Integer xpReward;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
        public Integer getXpReward() { return xpReward; }
        public void setXpReward(Integer xpReward) { this.xpReward = xpReward; }
    }

    // Getters and Setters for top level
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getIsPaid() { return isPaid; }
    public void setIsPaid(Boolean isPaid) { this.isPaid = isPaid; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public List<ChapterBulkRequest> getChapters() { return chapters; }
    public void setChapters(List<ChapterBulkRequest> chapters) { this.chapters = chapters; }
}
