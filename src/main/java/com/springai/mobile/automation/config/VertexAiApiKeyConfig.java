package com.springai.mobile.automation.config;

import com.google.cloud.vertexai.VertexAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;
import java.io.IOException;

/**
 * Configuration class specifically for API key authentication with Vertex AI
 */
@Configuration
@Primary
@Profile("!oauth") // Active when oauth profile is not active
public class VertexAiApiKeyConfig {

    private static final Logger logger = LoggerFactory.getLogger(VertexAiApiKeyConfig.class);

    @Value("${spring.ai.vertex.ai.gemini.apiKey}")
    private String apiKey;

    @Value("${spring.ai.vertex.ai.gemini.projectId}")
    private String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location}")
    private String location;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model:gemini-1.5-pro}")
    private String model;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.temperature:0.7}")
    private Float temperature;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.maxTokens:8192}")
    private Integer maxTokens;
    
    /**
     * Creates a VertexAiGeminiChatClient using API key authentication
     * @throws IOException if there's an error creating the VertexAI instance
     */
    @Bean
    @Primary
    public VertexAiGeminiChatClient vertexAiGeminiChatClient() throws IOException {
        // Log configuration details
        logger.info("Creating VertexAiGeminiChatClient with API Key");
        logger.info("Project ID: {}", projectId);
        logger.info("Location: {}", location);
        logger.info("API Key status: {}", StringUtils.hasText(apiKey) ? "PROVIDED" : "MISSING");
        logger.info("Model: {}", model);
        
        if (!StringUtils.hasText(apiKey)) {
            logger.error("API Key is missing. Authentication will fail!");
            throw new IllegalArgumentException("API Key is required but not provided");
        }

        try {
            // Set environment variables to ensure API key auth is used
            // This needs to happen before VertexAI instance is created
            logger.info("Setting up environment for API key authentication");
            
            // Force disabling Application Default Credentials
            System.setProperty("google.cloud.auth.disable.adc", "true");
            
            // Force using API key
            System.setProperty("VERTEX_AI_API_KEY", apiKey);
            
            // Create VertexAI with API key in constructor
            logger.info("Creating VertexAI instance with API key");
            VertexAI vertexAi = new VertexAI(projectId, location, apiKey);
            
            logger.info("Created VertexAI instance with API key successfully");
            
            // Configure chat options
            VertexAiGeminiChatOptions chatOptions = VertexAiGeminiChatOptions.builder()
                    .withModel(model)
                    .withTemperature(temperature)
                    .withMaxOutputTokens(maxTokens)
                    .build();
            
            logger.info("Created chat options with model: {}, temperature: {}, maxTokens: {}", 
                    model, temperature, maxTokens);
            
            // Create and return chat client
            logger.info("Creating VertexAiGeminiChatClient - this will trigger the actual authentication");
            VertexAiGeminiChatClient chatClient = new VertexAiGeminiChatClient(vertexAi, chatOptions);
            logger.info("Successfully created VertexAiGeminiChatClient");
            
            return chatClient;
        } catch (Exception e) {
            logger.error("Failed to create VertexAI instance with API key", e);
            throw e;
        }
    }
} 