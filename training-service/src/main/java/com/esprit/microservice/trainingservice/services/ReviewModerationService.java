package com.esprit.microservice.trainingservice.services;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ReviewModerationService {

    private final Set<String> forbiddenWords = new HashSet<>();

    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getResourceAsStream("/Terms-to-Block.csv")) {
            if (is == null) {
                System.err.println("Terms-to-Block.csv not found in classpath!");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Remove quotes and commas, e.g. "word," -> word
                    String cleaned = line.replace("\"", "").replace(",", "").trim();
                    if (!cleaned.isEmpty()) {
                        forbiddenWords.add(cleaned);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load Terms-to-Block.csv");
            e.printStackTrace();
        }
    }

    /**
     * Checks if the text contains any forbidden words as whole words.
     * Uses case-insensitive regex pattern matching.
     */
    public boolean containsBadWords(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        for (String word : forbiddenWords) {
            // \b ensures we match whole words only (e.g., 'bad' doesn't match 'badge')
            String regex = "\\b" + Pattern.quote(word) + "\\b";
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }
}
