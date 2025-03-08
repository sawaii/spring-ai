package com.springai.mobile.automation.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration for Vertex AI integration
 */
@Configuration
@Profile("!mock")
public class VertexAiConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(VertexAiConfig.class);
    
    @Value("${spring.ai.vertex.ai.gemini.projectId:}")
    private String projectId;
    
    @Value("${spring.ai.vertex.ai.gemini.location:us-central1}")
    private String location;
    
    @Value("${spring.ai.vertex.ai.gemini.apiKey:#{null}}")
    private String apiKey;
    
    @Value("${spring.ai.vertex.ai.gemini.modelName:gemini-pro}")
    private String modelName;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.model:gemini-pro}")
    private String chatModelName;
    
    /**
     * Constructor - just logs that initialization is starting
     */
    public VertexAiConfig() {
        logger.info("Creating Vertex AI configuration bean");
    }
    
    /**
     * Initialize Vertex AI settings after dependency injection is complete
     */
    @PostConstruct
    public void init() {
        logger.info("Initializing Vertex AI configuration");
        logger.info("Project ID: {}", projectId);
        logger.info("Location: {}", location);
        logger.info("Base Model: {}", modelName);
        logger.info("Chat Model: {}", chatModelName);
        
        try {
            // Set system properties for Spring AI auto-configuration
            if (projectId != null) {
                System.setProperty("spring.ai.vertex.ai.gemini.projectId", projectId);
            }
            
            if (location != null) {
                System.setProperty("spring.ai.vertex.ai.gemini.location", location);
            }
            
            if (modelName != null) {
                System.setProperty("spring.ai.vertex.ai.gemini.modelName", modelName);
            }
            
            // Set chat-specific properties
            if (chatModelName != null) {
                System.setProperty("spring.ai.vertex.ai.gemini.chat.options.model", chatModelName);
            }
            
            if (apiKey != null && !apiKey.isEmpty()) {
                logger.info("Using API key authentication");
                System.setProperty("spring.ai.vertex.ai.gemini.apiKey", apiKey);
            }
        } catch (Exception e) {
            logger.error("Error setting Vertex AI system properties", e);
        }
    }
} 