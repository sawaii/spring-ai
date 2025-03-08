package com.springai.mobile.automation.config;

import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for Gemini AI model settings
 */
@Configuration
@Profile("custom-disabled-profile") // Temporarily disabled to avoid conflicts with VertexAiApiKeyConfig
public class GeminiAiConfig {

    @Value("${spring.ai.vertex.ai.gemini.options.max-tokens:8192}")
    private int maxTokens;

    @Value("${spring.ai.vertex.ai.gemini.options.temperature:0.7}")
    private float temperature;

    /**
     * Configure the Gemini AI chat options
     * @return VertexAiGeminiChatOptions with configured parameters
     */
    @Bean
    public VertexAiGeminiChatOptions geminiChatOptions() {
        return VertexAiGeminiChatOptions.builder()
                .withMaxOutputTokens(maxTokens)
                .withTemperature(temperature)
                .build();
    }
} 