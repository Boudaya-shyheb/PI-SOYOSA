package com.englishway.course.event;

import com.englishway.course.config.AppKafkaProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AppKafkaProperties properties;

    public EventPublisher(ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider, AppKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        this.properties = properties;
    }

    public void publishCourseCreated(CourseCreatedEvent event) {
        safeSend(properties.getTopics().getCourseCreated(), event);
    }

    public void publishCourseUpdated(CourseUpdatedEvent event) {
        safeSend(properties.getTopics().getCourseUpdated(), event);
    }

    public void publishStudentEnrolled(StudentEnrolledEvent event) {
        safeSend(properties.getTopics().getStudentEnrolled(), event);
    }

    public void publishLessonCompleted(LessonCompletedEvent event) {
        safeSend(properties.getTopics().getLessonCompleted(), event);
    }

    public void publishCourseProgressUpdated(CourseProgressUpdatedEvent event) {
        safeSend(properties.getTopics().getCourseProgressUpdated(), event);
    }

    public void publishCourseCompleted(CourseCompletedEvent event) {
        safeSend(properties.getTopics().getCourseCompleted(), event);
    }

    private void safeSend(String topic, Object event) {
        if (!properties.isEnabled() || kafkaTemplate == null) {
            return;
        }
        try {
            kafkaTemplate.send(topic, event);
        } catch (Exception ex) {
            LOGGER.warn("Kafka publish failed for topic {}", topic, ex);
        }
    }
}
