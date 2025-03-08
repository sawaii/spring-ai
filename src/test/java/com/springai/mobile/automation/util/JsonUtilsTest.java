package com.springai.mobile.automation.util;

import com.springai.mobile.automation.model.Instruction;
import com.springai.mobile.automation.model.TestAction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonUtils
 */
public class JsonUtilsTest {

    @Test
    public void testParseTestActions() {
        // Create a test instruction
        Instruction instruction = Instruction.builder()
                .id(1L)
                .text("Login to the app")
                .status(Instruction.TestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Sample JSON response from the AI model
        String jsonResponse = """
            [
              {
                "actionType": "TAP",
                "elementDescription": "Username input field",
                "value": "",
                "sequence": 1
              },
              {
                "actionType": "TYPE",
                "elementDescription": "Username input field",
                "value": "testuser",
                "sequence": 2
              }
            ]
            """;
        
        // Parse the JSON response
        List<TestAction> actions = JsonUtils.parseTestActions(jsonResponse, instruction);
        
        // Verify the results
        assertEquals(2, actions.size());
        
        TestAction action1 = actions.get(0);
        assertEquals(TestAction.ActionType.TAP, action1.getActionType());
        assertEquals("Username input field", action1.getElementDescription());
        assertEquals("", action1.getValue());
        assertEquals(1, action1.getSequence());
        
        TestAction action2 = actions.get(1);
        assertEquals(TestAction.ActionType.TYPE, action2.getActionType());
        assertEquals("Username input field", action2.getElementDescription());
        assertEquals("testuser", action2.getValue());
        assertEquals(2, action2.getSequence());
    }
    
    @Test
    public void testParseTestActionsWithExtraText() {
        // Create a test instruction
        Instruction instruction = Instruction.builder()
                .id(1L)
                .text("Login to the app")
                .status(Instruction.TestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        // Sample JSON response with extra text
        String jsonResponse = """
            Here's the sequence of actions to login to the app:
            
            [
              {
                "actionType": "TAP",
                "elementDescription": "Username input field",
                "value": "",
                "sequence": 1
              },
              {
                "actionType": "TYPE",
                "elementDescription": "Username input field",
                "value": "testuser",
                "sequence": 2
              }
            ]
            
            This will tap on the username field and enter 'testuser'.
            """;
        
        // Parse the JSON response
        List<TestAction> actions = JsonUtils.parseTestActions(jsonResponse, instruction);
        
        // Verify the results
        assertEquals(2, actions.size());
    }
    
    @Test
    public void testExtractJsonArray() {
        String text = """
            Here's a JSON array:
            [
              {"name": "John", "age": 30},
              {"name": "Jane", "age": 25}
            ]
            And some more text.
            """;
        
        String extracted = JsonUtils.extractJsonArray(text);
        
        assertTrue(extracted.startsWith("["));
        assertTrue(extracted.endsWith("]"));
        assertTrue(extracted.contains("John"));
        assertTrue(extracted.contains("Jane"));
    }
    
    @Test
    public void testExtractJsonObject() {
        String text = """
            Here's a JSON object:
            {
              "name": "John",
              "age": 30,
              "address": {
                "city": "New York",
                "country": "USA"
              }
            }
            And some more text.
            """;
        
        String extracted = JsonUtils.extractJsonObject(text);
        
        assertTrue(extracted.startsWith("{"));
        assertTrue(extracted.endsWith("}"));
        assertTrue(extracted.contains("John"));
        assertTrue(extracted.contains("New York"));
    }
    
    @Test
    public void testFromJsonToMap() {
        String json = """
            {
              "name": "John",
              "age": 30,
              "address": {
                "city": "New York",
                "country": "USA"
              }
            }
            """;
        
        Map<String, Object> result = JsonUtils.fromJsonToMap(json);
        
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
        assertTrue(result.get("address") instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> address = (Map<String, Object>) result.get("address");
        assertEquals("New York", address.get("city"));
        assertEquals("USA", address.get("country"));
    }
    
    @Test
    public void testIsValidJson() {
        assertTrue(JsonUtils.isValidJson("{\"name\": \"John\"}"));
        assertTrue(JsonUtils.isValidJson("[{\"name\": \"John\"}, {\"name\": \"Jane\"}]"));
        assertFalse(JsonUtils.isValidJson("This is not JSON"));
        assertFalse(JsonUtils.isValidJson("{name: John}")); // Missing quotes
    }
} 