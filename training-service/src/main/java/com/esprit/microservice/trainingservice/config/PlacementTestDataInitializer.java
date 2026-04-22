package com.esprit.microservice.trainingservice.config;

import com.esprit.microservice.trainingservice.entities.Level;
import com.esprit.microservice.trainingservice.entities.PlacementTestQuestion;
import com.esprit.microservice.trainingservice.repositories.PlacementTestQuestionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class PlacementTestDataInitializer {

    @Bean
    CommandLineRunner initPlacementTests(PlacementTestQuestionRepository repository) {
        return args -> {
            if (repository.count() > 0) return;

            repository.saveAll(Arrays.asList(
                // A1 Level
                new PlacementTestQuestion(null, "Which of these is a greeting?", Arrays.asList("Goodbye", "Hello", "Pardon", "Please"), 1, Level.A1),
                new PlacementTestQuestion(null, "What color is the sky on a clear day?", Arrays.asList("Green", "Red", "Blue", "Yellow"), 2, Level.A1),
                
                // A2 Level
                new PlacementTestQuestion(null, "I ____ to the cinema yesterday.", Arrays.asList("go", "went", "gone", "going"), 1, Level.A2),
                new PlacementTestQuestion(null, "Which is the comparative form of 'Good'?", Arrays.asList("Gooder", "Best", "Better", "More good"), 2, Level.A2),
                
                // B1 Level
                new PlacementTestQuestion(null, "If I ____ rich, I would travel the world.", Arrays.asList("am", "was", "were", "be"), 2, Level.B1),
                new PlacementTestQuestion(null, "She has been working here ____ three years.", Arrays.asList("for", "since", "during", "ago"), 0, Level.B1),
                
                // B2 Level
                new PlacementTestQuestion(null, "By the time he arrived, the meeting ____.", Arrays.asList("already started", "had already started", "was starting", "has started"), 1, Level.B2),
                
                // C1 Level
                new PlacementTestQuestion(null, "The word 'Meticulous' most nearly means:", Arrays.asList("Careless", "Quick", "Very detailed", "Aggressive"), 2, Level.C1),
                
                // C2 Level
                new PlacementTestQuestion(null, "Had it not been for his help, I ____.", Arrays.asList("would fail", "might fail", "would have failed", "failed"), 2, Level.C2),
                new PlacementTestQuestion(null, "Regardless of the outcome, we must remain ____.", Arrays.asList("indifferent", "steadfast", "fickle", "unreliable"), 1, Level.C2)
            ));
        };
    }
}
