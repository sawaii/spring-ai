package com.springai.mobile.automation.config;

import com.google.cloud.vertexai.VertexAI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
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
 * Configuration class that explicitly creates a VertexAiGeminiChatClient bean
 * ENHANCED: Now with improved logging and explicit API key support
 */
@Configuration
@Profile("custom-disabled-profile")
public class VertexAiGeminiChatClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(VertexAiGeminiChatClientConfig.class);
    
    @Value("${spring.ai.vertex.ai.gemini.projectId:***REMOVED***}")
    private String projectId;
    
    @Value("${spring.ai.vertex.ai.gemini.location:us-central1}")
    private String location;
    
    @Value("${spring.ai.vertex.ai.gemini.apiKey:***REMOVED***}")
    private String apiKey;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.model:***REMOVED***}")
    private String modelName;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.maxTokens:8192}")
    private Integer maxTokens;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.temperature:0.7}")
    private Float temperature;
    
    /**
     * Creates a VertexAiGeminiChatClient configured with the provided properties
     * Enhanced with better logging and explicit API key handling
     */
    @Bean
    @Primary
    public ChatClient vertexAiGeminiChatClient() throws IOException {
        logger.info("=========================================================");
        logger.info("Creating VertexAiGeminiChatClient with explicit configuration");
        logger.info("Project ID: {}", projectId);
        logger.info("Location: {}", location);
        logger.info("Model: {}", modelName);
        logger.info("API Key present: {}", StringUtils.hasText(apiKey) ? "YES" : "NO");
        logger.info("Service Account present: {}", 
                  StringUtils.hasText(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")) ? "YES" : "NO");
        logger.info("=========================================================");
        
        VertexAI vertexAi;
        if (StringUtils.hasText(apiKey)) {
            logger.info("Using API key authentication for Vertex AI");
            vertexAi = new VertexAI(projectId, location, apiKey);
            logger.info("Successfully created VertexAI instance with API key");
        } else {
            String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (StringUtils.hasText(credentialsPath)) {
                logger.info("Using service account authentication from: {}", credentialsPath);
            } else {
                logger.warn("GOOGLE_APPLICATION_CREDENTIALS not set. Will try application default credentials");
            }
            
            logger.info("Using service account authentication");
            vertexAi = new VertexAI(projectId, location);
            logger.info("Successfully created VertexAI instance with service account");
        }
        
        VertexAiGeminiChatOptions options = VertexAiGeminiChatOptions.builder()
            .withModel(modelName)
            .withTemperature(temperature)
            .build();
            
        logger.info("Created VertexAiGeminiChatOptions with model: {}", options.getModel());
        logger.info("Created VertexAiGeminiChatOptions with temperature: {}", options.getTemperature());
        
        VertexAiGeminiChatClient client = new VertexAiGeminiChatClient(vertexAi, options);
        logger.info("Successfully created VertexAiGeminiChatClient");
        return client;
    }
} 