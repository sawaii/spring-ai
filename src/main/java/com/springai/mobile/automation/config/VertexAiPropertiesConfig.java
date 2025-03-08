package com.springai.mobile.automation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Vertex AI properties
 * This explicitly configures the required properties for Spring AI
 */
@Configuration
@EnableConfigurationProperties
@Profile("!mock")
public class VertexAiPropertiesConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(VertexAiPropertiesConfig.class);
    
    @Value("${spring.ai.vertex.ai.gemini.projectId:}")
    private String projectId;
    
    @Value("${spring.ai.vertex.ai.gemini.location:us-central1}")
    private String location;
    
    @Value("${spring.ai.vertex.ai.gemini.modelName:gemini-pro}")
    private String modelName;
    
    @Value("${spring.ai.vertex.ai.gemini.apiKey:#{null}}")
    private String apiKey;
    
    @Value("${spring.ai.vertex.ai.gemini.chat.options.model:gemini-pro}")
    private String chatModelName;
    
    /**
     * Creates a properties map with Vertex AI Gemini specific properties
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.ai.vertex.ai.gemini")
    public Map<String, Object> vertexAiGeminiProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        logger.info("Setting up explicit Vertex AI Gemini properties:");
        logger.info("projectId: {}", projectId);
        logger.info("location: {}", location);
        logger.info("modelName: {}", modelName);
        logger.info("chatModelName: {}", chatModelName);
        
        properties.put("projectId", projectId);
        properties.put("location", location);
        properties.put("modelName", modelName);
        
        // Chat-specific configuration
        Map<String, Object> chatOptions = new HashMap<>();
        chatOptions.put("model", chatModelName);
        chatOptions.put("temperature", 0.7);
        chatOptions.put("maxTokens", 8192);
        
        properties.put("chat", Map.of("options", chatOptions));
        
        if (apiKey != null && !apiKey.isEmpty()) {
            logger.info("Using API key authentication");
            properties.put("apiKey", apiKey);
        }
        
        return properties;
    }
} 