package com.englishway.course.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.local-assistant")
public class AppAssistantProperties {
    private boolean enabled = true;
    private boolean launchLocalServer = true;
    private String baseUrl = "http://127.0.0.1:8001";
    private String pythonExecutable = "..\\.venv\\Scripts\\python.exe";
    private String localModelScript = "..\\ai-tutor-service\\local-model\\api_server.py";
    private String model = "english-tutor-local";
    private double temperature = 0.4;
    private int maxTokens = 350;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLaunchLocalServer() {
        return launchLocalServer;
    }

    public void setLaunchLocalServer(boolean launchLocalServer) {
        this.launchLocalServer = launchLocalServer;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPythonExecutable() {
        return pythonExecutable;
    }

    public void setPythonExecutable(String pythonExecutable) {
        this.pythonExecutable = pythonExecutable;
    }

    public String getLocalModelScript() {
        return localModelScript;
    }

    public void setLocalModelScript(String localModelScript) {
        this.localModelScript = localModelScript;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
}