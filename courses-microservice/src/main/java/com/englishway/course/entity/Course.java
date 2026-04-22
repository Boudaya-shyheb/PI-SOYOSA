package com.englishway.course.entity;

import com.englishway.course.enums.Level;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "course")
public class Course {
    @Id
    private UUID id;

        @Column(nullable = false, length = 200)
        private String title;

        @Column(nullable = false, length = 1000)
        private String description;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 5)
        private Level level;

        @Column(nullable = false)
        private Integer capacity;

        @Column(nullable = false)
        private boolean active;

        @Column(name = "is_paid", nullable = false)
        private boolean paid;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal price;

        @Column(name = "tutor_id", nullable = false, length = 100)
        private String tutorId;

        @Column(name = "image_url", columnDefinition = "TEXT")
        private String imageUrl;

        @Column(nullable = false)
        private Instant createdAt;

        @Column(nullable = false)
        private Instant updatedAt;

        @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Chapter> chapters = new ArrayList<>();

        @PrePersist
        void onCreate() {
            if (id == null) {
                id = UUID.randomUUID();
            }
            Instant now = Instant.now();
            createdAt = now;
            updatedAt = now;
        }

        @PreUpdate
        void onUpdate() {
            updatedAt = Instant.now();
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Level getLevel() {
            return level;
        }

        public void setLevel(Level level) {
            this.level = level;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isPaid() {
            return paid;
        }

        public void setPaid(boolean paid) {
            this.paid = paid;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getTutorId() {
            return tutorId;
        }

        public void setTutorId(String tutorId) {
            this.tutorId = tutorId;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public List<Chapter> getChapters() {
            return chapters;
        }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }
}
