package com.englishway.course.config;

import com.englishway.course.event.PaymentConfirmedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties, SslBundles sslBundles) {
        Map<String, Object> config = new HashMap<>(properties.buildProducerProperties(sslBundles));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, PaymentConfirmedEvent> paymentConsumerFactory(
        KafkaProperties properties,
        SslBundles sslBundles
    ) {
        Map<String, Object> config = new HashMap<>(properties.buildConsumerProperties(sslBundles));
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.englishway.course.event");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, PaymentConfirmedEvent.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        JsonDeserializer<PaymentConfirmedEvent> deserializer = new JsonDeserializer<>();
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentConfirmedEvent> paymentKafkaListenerContainerFactory(
        ConsumerFactory<String, PaymentConfirmedEvent> paymentConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentConfirmedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory);
        factory.setAutoStartup(false);
        return factory;
    }

    @Bean
    public NewTopic courseCreatedTopic(AppKafkaProperties properties) {
        return new NewTopic(properties.getTopics().getCourseCreated(), 1, (short) 1);
    }

    @Bean
    public NewTopic courseUpdatedTopic(AppKafkaProperties properties) {
        return new NewTopic(properties.getTopics().getCourseUpdated(), 1, (short) 1);
    }

    @Bean
    public NewTopic studentEnrolledTopic(AppKafkaProperties properties) {
        return new NewTopic(properties.getTopics().getStudentEnrolled(), 1, (short) 1);
    }

    @Bean
    public NewTopic lessonCompletedTopic(AppKafkaProperties properties) {
        return new NewTopic(properties.getTopics().getLessonCompleted(), 1, (short) 1);
    }

    @Bean
    public NewTopic courseProgressUpdatedTopic(AppKafkaProperties properties) {
        return new NewTopic(properties.getTopics().getCourseProgressUpdated(), 1, (short) 1);
    }

    @Bean
    public NewTopic courseCompletedTopic(AppKafkaProperties properties) {
        return new NewTopic(properties.getTopics().getCourseCompleted(), 1, (short) 1);
    }

    @Bean
    public NewTopic paymentConfirmedTopic(AppKafkaProperties properties) {
        return new NewTopic(properties.getTopics().getPaymentConfirmed(), 1, (short) 1);
    }
}
