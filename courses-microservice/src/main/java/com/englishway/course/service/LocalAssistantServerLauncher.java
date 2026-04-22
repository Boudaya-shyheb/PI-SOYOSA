package com.englishway.course.service;

import com.englishway.course.config.AppAssistantProperties;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LocalAssistantServerLauncher {
    private final AppAssistantProperties properties;
    private final RestTemplate restTemplate;
    private Process process;

    public LocalAssistantServerLauncher(AppAssistantProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(2))
            .build();
        startIfNeeded();
    }

    private synchronized void startIfNeeded() {
        if (!properties.isLaunchLocalServer() || process != null) {
            return;
        }

        String pythonExecutable = properties.getPythonExecutable();
        String scriptPath = properties.getLocalModelScript();

        if (pythonExecutable == null || pythonExecutable.isBlank() || scriptPath == null || scriptPath.isBlank()) {
            return;
        }

        Path pythonPath = Path.of(pythonExecutable).normalize();
        Path modelScript = Path.of(scriptPath).normalize();

        if (!Files.exists(pythonPath) || !Files.exists(modelScript)) {
            return;
        }

        try {
            process = new ProcessBuilder(
                pythonPath.toString(),
                modelScript.toString()
            )
                .redirectErrorStream(true)
                .inheritIO()
                .start();

            waitForHealth();
        } catch (IOException ignored) {
            process = null;
        }
    }

    private void waitForHealth() {
        String healthUrl = normalizeBaseUrl(properties.getBaseUrl()) + "/health";
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 30000) {
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return;
                }
            } catch (Exception ignored) {
                // keep waiting until the local model server is ready
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @PreDestroy
    public void stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
