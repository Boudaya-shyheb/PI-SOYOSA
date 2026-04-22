package com.englishway.course.service;

import com.englishway.course.config.AppAssistantProperties;
import com.englishway.course.dto.AssistantChatMessage;
import com.englishway.course.dto.AssistantChatRequest;
import com.englishway.course.dto.AssistantChatResponse;
import com.englishway.course.enums.Role;
import com.englishway.course.exception.AccessDeniedException;
import com.englishway.course.exception.BadRequestException;
import com.englishway.course.util.RequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;

@Service
public class AssistantChatService {
    private static final Logger log = LoggerFactory.getLogger(AssistantChatService.class);
    private final AppAssistantProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public AssistantChatService(
        AppAssistantProperties properties,
        ObjectMapper objectMapper,
        RestTemplateBuilder restTemplateBuilder
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(90))
            .build();
    }

    public AssistantChatResponse chat(RequestContext context, AssistantChatRequest request) {
        requireStudentOnly(context);

        String userMessage = extractLastUserMessage(request)
            .orElseThrow(() -> new BadRequestException("A user message is required"));

        if (containsNonLatinScript(userMessage)) {
            return new AssistantChatResponse("Please write your question in English. I only answer in English so you can practice while learning.");
        }

        if (!properties.isEnabled() || isBlank(properties.getBaseUrl())) {
            return new AssistantChatResponse(buildFallbackReply(userMessage));
        }

        try {
            java.util.Map<String, Object> payload = buildAiPayload(userMessage);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String payloadJson = objectMapper.writeValueAsString(payload);

            ResponseEntity<String> response = restTemplate.postForEntity(
                normalizeBaseUrl(properties.getBaseUrl()) + "/chat",
                new HttpEntity<>(payloadJson, headers),
                String.class
            );

            String reply = extractAiReply(parseBody(response.getBody()));
            if (!isBlank(reply)) {
                log.debug("AssistantChatService: local model reply received");
                return new AssistantChatResponse(reply.trim());
            }
            log.warn("AssistantChatService: local model response was empty, using fallback");
        } catch (RestClientResponseException ex) {
            log.warn("AssistantChatService: local model HTTP error, using fallback: status={} body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
            return new AssistantChatResponse(buildFallbackReply(userMessage));
        } catch (Exception ex) {
            log.warn("AssistantChatService: local model call failed, using fallback", ex);
            return new AssistantChatResponse(buildFallbackReply(userMessage));
        }

        return new AssistantChatResponse(buildFallbackReply(userMessage));
    }

    private void requireStudentOnly(RequestContext context) {
        if (!context.isAuthenticated() || context.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("This assistant is available for students only");
        }
    }

    private Optional<String> extractLastUserMessage(AssistantChatRequest request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
            return Optional.empty();
        }

        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            AssistantChatMessage message = request.getMessages().get(i);
            if (message != null && "user".equalsIgnoreCase(message.getRole()) && !isBlank(message.getContent())) {
                return Optional.of(message.getContent().trim());
            }
        }

        return Optional.ofNullable(request.getMessages().get(request.getMessages().size() - 1).getContent())
            .map(String::trim)
            .filter(value -> !value.isBlank());
    }

    private java.util.Map<String, Object> buildAiPayload(String userMessage) {
        List<java.util.Map<String, String>> messages = new ArrayList<>();
        messages.add(messageMap("system", buildSystemMessage().getContent()));
        messages.add(messageMap("user", userMessage));

        java.util.LinkedHashMap<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("messages", messages);
        return payload;
    }

    private java.util.Map<String, String> messageMap(String role, String content) {
        java.util.LinkedHashMap<String, String> message = new java.util.LinkedHashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private AssistantChatMessage buildSystemMessage() {
        AssistantChatMessage system = new AssistantChatMessage();
        system.setRole("system");
        system.setContent(String.join(" ",
            "You are an English-speaking teacher helping a student practice English on a course platform.",
            "Answer only in English.",
            "Keep replies concise, natural, and useful.",
            "Correct mistakes gently and ask a short follow-up question when helpful."
        ));
        return system;
    }

    private AssistantChatMessage buildUserMessage(String content) {
        AssistantChatMessage user = new AssistantChatMessage();
        user.setRole("user");
        user.setContent(content);
        return user;
    }

    private String extractAiReply(JsonNode responseBody) {
        if (responseBody == null) {
            return null;
        }
        JsonNode reply = responseBody.path("choices").path(0).path("message").path("content");
        if (reply.isTextual()) {
            return reply.asText();
        }

        JsonNode directReply = responseBody.path("reply");
        return directReply.isTextual() ? directReply.asText() : null;
    }

    private JsonNode parseBody(String body) {
        if (isBlank(body)) {
            return null;
        }
        try {
            return objectMapper.readTree(body);
        } catch (Exception ex) {
            return null;
        }
    }

    private String buildFallbackReply(String message) {
        String lower = message.toLowerCase(Locale.ROOT);

        if (matchesAny(lower, "correct this", "fix this", "grammar check", "correct my sentence")) {
            String target = extractCorrectionTarget(message);
            if (!isBlank(target)) {
                String corrected = quickCorrectCommonMistakes(target);
                if (!target.trim().equals(corrected.trim())) {
                    return String.join("\n\n",
                        "Good try. Here is a better sentence:",
                        "\"" + corrected + "\"",
                        "Would you like me to explain the grammar rule in one short step?"
                    );
                }
            }
        }

        if (matchesAny(lower, "hello", "hi", "hey")) {
            return String.join("\n\n",
                "Hello. I can help you choose a course, understand pricing, continue your progress, and prepare for quizzes.",
                "You can ask me about grammar, vocabulary, speaking, writing, reading, listening, or how to move through your lessons.",
                "If you want a quick reply, ask a simple English question like: 'Which course should I start with?' or 'How do I improve my grammar?'."
            );
        }

        if (matchesAny(lower, "start", "beginner", "a1", "a2", "which course", "recommend")) {
            return String.join("\n\n",
                "If you are starting, choose an A1 or A2 course first. That level usually gives you the clearest path because the grammar, vocabulary, and lesson pace are easier to follow.",
                "When you compare courses, look for three things: active status, clear lesson structure, and a description that matches your goal. If you want to improve communication, choose a speaking-focused course; if you want to pass exams, choose a grammar and practice-focused course.",
                "A good strategy is to start with one course, complete the lessons in order, and move to the next level only after you feel comfortable with the current vocabulary and sentence patterns."
            );
        }

        if (matchesAny(lower, "enroll", "join", "register")) {
            return String.join("\n\n",
                "To enroll, open the course card and click Enroll Now. If the course is free, the process is immediate. If the course is paid, you will be redirected to payment before the course becomes active.",
                "After enrollment, return to the course page and open your first lesson. Progress is saved as you continue, so you can come back later without losing your place.",
                "If you want, I can also explain the difference between free enrollment, paid access, and continuing a course you already started."
            );
        }

        if (matchesAny(lower, "free", "paid", "price", "cost")) {
            return String.join("\n\n",
                "Use the course filters to separate free courses from paid ones. Free courses are a good option if you want to explore the platform first, while paid courses often provide a more structured learning path.",
                "A helpful rule is to match the course type with your goal: choose a beginner course for basics, a revision course for grammar practice, and a quiz-heavy course if you want assessment practice.",
                "If you want, I can suggest a study plan based on your level and whether you prefer speaking, grammar, or vocabulary practice."
            );
        }

        if (matchesAny(lower, "resume", "continue", "progress", "stopped")) {
            return String.join("\n\n",
                "If you already enrolled, click Continue or Resume on the course card. That takes you back to the place where you stopped or opens the next lesson in your learning path.",
                "A good study rhythm is: review the last lesson for 2 minutes, complete the next lesson, answer any quiz questions carefully, and then mark the lesson complete.",
                "That way, you build consistency instead of studying in a random order."
            );
        }

        if (matchesAny(lower, "quiz", "exam", "test")) {
            return String.join("\n\n",
                "For quizzes, answer every question carefully, watch the timer, and read the feedback after submission.",
                "If you are unsure, first eliminate the obviously wrong options, then compare the remaining choices with the grammar or meaning of the sentence. For fill-in-the-blank questions, read the whole sentence before choosing your answer.",
                "A strong quiz strategy is to practice one topic at a time, review the incorrect answers, and repeat the same topic after a short break."
            );
        }

        if (matchesAny(lower, "grammar", "sentence", "correct", "word", "vocabulary", "pronunciation", "speaking", "listening", "reading", "writing", "essay")) {
            return String.join("\n\n",
                "I can help with English practice as well. If your question is about grammar, think about the tense, subject, verb form, and sentence order. If it is about vocabulary, focus on the meaning, collocation, and example sentence.",
                "If you are writing, keep sentences short and clear. If you are speaking, use simple structures first, then gradually add longer sentences. If you are reading or listening, look for the main idea first and the details second.",
                "You can also send me a sentence and I can help you understand whether it sounds natural, what grammar rule it uses, and how to improve it."
            );
        }

        return String.join("\n\n",
            "Here is a practical way to think about your question: if it is about the course platform, I can help you choose a course, enroll, continue, and manage quizzes. If it is about English, I can help with grammar, vocabulary, speaking, writing, and sentence correction.",
            "When a question is broad, the best approach is to break it into one main goal, one problem, and one next step. For example: 'I want to improve my grammar', 'I do not understand this sentence', or 'Which course should I start with?'.",
            "Send me a more specific sentence if you want a sharper answer, but I can still keep helping even when the question is general."
        );
    }

    private boolean matchesAny(String source, String... tokens) {
        for (String token : tokens) {
            String normalizedToken = token.toLowerCase(Locale.ROOT).trim();
            if (normalizedToken.contains(" ")) {
                if (source.contains(normalizedToken)) {
                    return true;
                }
                continue;
            }

            if (source.matches(".*\\b" + java.util.regex.Pattern.quote(normalizedToken) + "\\b.*")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsNonLatinScript(String value) {
        return value != null && value.matches(".*[\\u0400-\\u04FF\\u0600-\\u06FF\\u4E00-\\u9FFF].*");
    }

    private String extractCorrectionTarget(String message) {
        if (isBlank(message)) {
            return "";
        }

        int colon = message.indexOf(':');
        if (colon >= 0 && colon + 1 < message.length()) {
            return message.substring(colon + 1).trim();
        }

        return message.trim();
    }

    private String quickCorrectCommonMistakes(String sentence) {
        String corrected = sentence;

        corrected = corrected.replaceAll("(?i)\\bi goed\\b", "I went");
        corrected = corrected.replaceAll("(?i)\\bhe don't\\b", "he doesn't");
        corrected = corrected.replaceAll("(?i)\\bshe don't\\b", "she doesn't");
        corrected = corrected.replaceAll("(?i)\\bi am interesting in\\b", "I am interested in");
        corrected = corrected.replaceAll("(?i)\\bi have (\\d+) years old\\b", "I am $1 years old");
        corrected = corrected.replaceAll("\\s+", " ").trim();

        if (!corrected.endsWith(".") && !corrected.endsWith("?") && !corrected.endsWith("!")) {
            corrected = corrected + ".";
        }

        if (!corrected.isEmpty()) {
            corrected = Character.toUpperCase(corrected.charAt(0)) + corrected.substring(1);
        }

        return corrected;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}