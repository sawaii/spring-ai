package com.springai.mobile.automation.config;

import com.google.cloud.vertexai.VertexAI;
import java.io.IOException;
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

/**
 * Configuration for enhanced Vertex AI client with additional logging
 * TEMPORARILY DISABLED due to conflicts with other beans
 */
@Configuration
@Profile("custom-disabled-profile")
public class VertexAiClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(VertexAiClientConfig.class);
    
    @Value("${spring.ai.vertex.ai.gemini.projectId:***REMOVED***}")
    private String projectId;
    
    @Value("${spring.ai.vertex.ai.gemini.location:us-central1}")
    private String location;
    
    @Value("${spring.ai.vertex.ai.gemini.modelName:***REMOVED***}")
    private String modelName;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.model:***REMOVED***}")
    private String chatModelName;
    
    @Value("${spring.ai.vertex.ai.gemini.apiKey:***REMOVED***}")
    private String apiKey;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.temperature:0.7}")
    private Float temperature;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.maxTokens:8192}")
    private Integer maxTokens;
    
    /**
     * Creates a Vertex AI Gemini chat client with enhanced logging
     * Prioritizes API key auth with fallback to service account
     */
    @Bean
    @Primary
    public ChatClient enhancedVertexAiGeminiChatClient() {
        logger.info("=========================================================");
        logger.info("Creating enhanced VertexAI Gemini chat client");
        logger.info("Project ID: {}", projectId);
        logger.info("Location: {}", location);
        logger.info("Model Name: {}", modelName);
        logger.info("Chat Model: {}", chatModelName);
        logger.info("API Key present: {}", StringUtils.hasText(apiKey) ? "YES" : "NO");
        logger.info("Service Account present: {}", 
                    StringUtils.hasText(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")) ? "YES" : "NO");
        logger.info("=========================================================");
        
        try {
            VertexAI vertexAi;
            VertexAiGeminiChatOptions options = VertexAiGeminiChatOptions.builder()
                .withModel(chatModelName)
                .withTemperature(temperature)
                .build();
                
            // Prioritize API key authentication if available
            if (StringUtils.hasText(apiKey)) {
                logger.info("Using API key authentication for Vertex AI");
                vertexAi = new VertexAI(projectId, location, apiKey);
                logger.info("Successfully created VertexAI instance with API key");
            } 
            // Fall back to service account authentication
            else {
                String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
                if (StringUtils.hasText(credentialsPath)) {
                    logger.info("Using service account authentication from: {}", credentialsPath);
                } else {
                    logger.warn("GOOGLE_APPLICATION_CREDENTIALS not set. Will try application default credentials");
                }
                
                vertexAi = new VertexAI(projectId, location);
                logger.info("Successfully created VertexAI instance with service account");
            }
            
            logger.info("Created VertexAI options with model: {}", options.getModel());
            logger.info("Created VertexAI options with temperature: {}", options.getTemperature());
                
            VertexAiGeminiChatClient client = new VertexAiGeminiChatClient(vertexAi, options);
            logger.info("Successfully created VertexAiGeminiChatClient");
            return client;
        }
        catch (IOException e) {
            logger.error("Error creating VertexAI Gemini chat client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Vertex AI client", e);
        }
        catch (Exception e) {
            logger.error("Unexpected error creating VertexAI Gemini chat client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Vertex AI client", e);
        }
    }
} 