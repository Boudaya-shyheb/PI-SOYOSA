package com.englishway.course.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.kafka")
public class AppKafkaProperties {
    private boolean enabled;
    private Topics topics = new Topics();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public static class Topics {
        private String courseCreated;
        private String courseUpdated;
        private String studentEnrolled;
        private String lessonCompleted;
        private String courseProgressUpdated;
        private String courseCompleted;
        private String paymentConfirmed;

        public String getCourseCreated() {
            return courseCreated;
        }

        public void setCourseCreated(String courseCreated) {
            this.courseCreated = courseCreated;
        }

        public String getCourseUpdated() {
            return courseUpdated;
        }

        public void setCourseUpdated(String courseUpdated) {
            this.courseUpdated = courseUpdated;
        }

        public String getStudentEnrolled() {
            return studentEnrolled;
        }

        public void setStudentEnrolled(String studentEnrolled) {
            this.studentEnrolled = studentEnrolled;
        }

        public String getLessonCompleted() {
            return lessonCompleted;
        }

        public void setLessonCompleted(String lessonCompleted) {
            this.lessonCompleted = lessonCompleted;
        }

        public String getCourseProgressUpdated() {
            return courseProgressUpdated;
        }

        public void setCourseProgressUpdated(String courseProgressUpdated) {
            this.courseProgressUpdated = courseProgressUpdated;
        }

        public String getCourseCompleted() {
            return courseCompleted;
        }

        public void setCourseCompleted(String courseCompleted) {
            this.courseCompleted = courseCompleted;
        }

        public String getPaymentConfirmed() {
            return paymentConfirmed;
        }

        public void setPaymentConfirmed(String paymentConfirmed) {
            this.paymentConfirmed = paymentConfirmed;
        }
    }
}
