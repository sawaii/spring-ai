package com.springai.mobile.automation.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.springai.mobile.automation.model.Instruction;
import com.springai.mobile.automation.model.TestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for JSON processing
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Pattern to match JSON arrays in AI responses
    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[.*?\\]", Pattern.DOTALL);
    
    // Pattern to match JSON objects in AI responses
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);
    
    /**
     * Parse a JSON array of test actions
     * @param json the JSON string
     * @param instruction the parent instruction
     * @return list of TestAction objects
     */
    public static List<TestAction> parseTestActions(String json, Instruction instruction) {
        List<TestAction> actions = new ArrayList<>();
        
        try {
            // Extract JSON array if it's embedded in text
            String cleanedJson = extractJsonArray(json);
            
            JsonNode rootNode = objectMapper.readTree(cleanedJson);
            
            if (rootNode.isArray()) {
                ArrayNode actionsArray = (ArrayNode) rootNode;
                
                for (JsonNode actionNode : actionsArray) {
                    String actionType = actionNode.get("actionType").asText();
                    String elementDescription = actionNode.has("elementDescription") ? 
                            actionNode.get("elementDescription").asText() : null;
                    String value = actionNode.has("value") ? 
                            actionNode.get("value").asText() : null;
                    int sequence = actionNode.has("sequence") ? 
                            actionNode.get("sequence").asInt() : actions.size() + 1;
                    
                    TestAction action = TestAction.builder()
                            .instruction(instruction)
                            .actionType(TestAction.ActionType.valueOf(actionType))
                            .elementDescription(elementDescription)
                            .value(value)
                            .sequence(sequence)
                            .build();
                    
                    actions.add(action);
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid action type: {}", e.getMessage());
        }
        
        return actions;
    }
    
    /**
     * Convert an object to JSON string
     * @param object the object to convert
     * @return JSON string representation
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Error converting to JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * Parse a JSON string to an object
     * @param json the JSON string
     * @param valueType the class of the object
     * @param <T> the type of the object
     * @return the parsed object
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse a JSON string to a Map
     * @param json the JSON string
     * @return the parsed Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            // Extract JSON object if it's embedded in text
            String cleanedJson = extractJsonObject(json);
            
            return objectMapper.readValue(cleanedJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON to Map: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Extract a JSON array from text that might contain other content
     * @param text the text containing a JSON array
     * @return the extracted JSON array
     */
    public static String extractJsonArray(String text) {
        if (text == null || text.isEmpty()) {
            return "[]";
        }
        
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        return "[]";
    }
    
    /**
     * Extract a JSON object from text that might contain other content
     * @param text the text containing a JSON object
     * @return the extracted JSON object
     */
    public static String extractJsonObject(String text) {
        if (text == null || text.isEmpty()) {
            return "{}";
        }
        
        Matcher matcher = JSON_OBJECT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        return "{}";
    }
    
    /**
     * Check if a string is valid JSON
     * @param text the text to check
     * @return true if the text is valid JSON
     */
    public static boolean isValidJson(String text) {
        try {
            objectMapper.readTree(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 