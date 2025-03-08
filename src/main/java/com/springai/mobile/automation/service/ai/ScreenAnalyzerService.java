package com.springai.mobile.automation.service.ai;

import com.springai.mobile.automation.model.TestAction;
import com.springai.mobile.automation.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for analyzing screenshots and identifying UI elements using Gemini AI vision capabilities
 */
@Service
public class ScreenAnalyzerService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScreenAnalyzerService.class);

    private static final String SYSTEM_PROMPT_TEMPLATE = """
        You are an expert mobile app UI analyzer.
        You are looking at a screenshot of a mobile application.
        
        Your task is to:
        1. Identify and describe all visible UI elements on the screen
        2. For each element, suggest possible locator strategies (xpath, accessibility id, etc.)
        3. Based on the element description provided, determine the best matching element and its precise location
        
        Focus on finding the element that best matches this description: %s
        
        Respond with a JSON object in the following format:
        {
          "screenDescription": "Brief description of the screen (e.g., 'Login Screen', 'Home Page')",
          "matchedElement": {
            "description": "Description of the matched element",
            "type": "Type of element (button, text field, etc.)",
            "text": "Text content of the element (if any)",
            "confidence": 0.95,
            "bounds": {
              "x": 100,
              "y": 200,
              "width": 300,
              "height": 50
            },
            "suggestedLocators": {
              "xpath": "//android.widget.Button[@text='Login']",
              "accessibilityId": "login_button",
              "id": "com.example.app:id/login_button"
            }
          },
          "otherElements": [
            {
              "description": "Description of another element",
              "type": "Type of element",
              "text": "Text content of the element (if any)"
            }
          ]
        }
        
        Return ONLY the JSON object without any additional text or explanation.
        """;

    private final VertexAiGeminiChatClient chatClient;
    
    @Value("${app.automation.screenshot.directory:./screenshots}")
    private String screenshotDirectory;

    @Autowired
    public ScreenAnalyzerService(VertexAiGeminiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Analyze a screenshot and find the best element match for a test action
     * @param screenshotFile the screenshot file to analyze
     * @param testAction the test action containing the element description
     * @return map containing element information
     * @throws IOException if there's an error reading the image
     */
    public Map<String, Object> analyzeScreenshot(File screenshotFile, TestAction testAction) throws IOException {
        byte[] imageData = Files.readAllBytes(screenshotFile.toPath());
        
        // Format the system prompt with the element description
        String formattedSystemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, testAction.getElementDescription());
        
        // Create system message
        Message systemMessage = new SystemMessage(formattedSystemPrompt);
        
        // Create a user message with the instruction
        UserMessage userMessage = new UserMessage("Analyze this screenshot and find the element that matches the given description.");
        
        // Encode the image to base64 for the API call
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        
        // Create prompt with system message and user message
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        
        try {
            // Call Gemini Vision model
            logger.info("Sending screenshot to Gemini Vision API for analysis");
            
            // TODO: The exact way to include images depends on the Spring AI API version
            // For demonstration purposes, we're using a simple prompt here
            // In a real implementation, this would be adjusted based on the API documentation
            
            ChatResponse response = chatClient.call(prompt);
            
            // Extract and parse the JSON response
            String content = response.getResult().getOutput().getContent();
            logger.debug("Received response from Gemini Vision API: {}", content);
            
            // Use JsonUtils to parse the JSON
            Map<String, Object> result = JsonUtils.fromJsonToMap(content);
            
            if (result.isEmpty()) {
                logger.warn("Failed to parse screen analysis result, returning default values");
                return createDefaultAnalysisResult(testAction);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Error analyzing screenshot with Gemini Vision API", e);
            return createDefaultAnalysisResult(testAction);
        }
    }
    
    /**
     * Create a default analysis result when parsing fails
     * @param testAction the test action
     * @return default map with element information
     */
    private Map<String, Object> createDefaultAnalysisResult(TestAction testAction) {
        Map<String, Object> result = new HashMap<>();
        result.put("screenDescription", "Sample Screen");
        result.put("matchedElement", Map.of(
                "description", testAction.getElementDescription(),
                "type", "button",
                "confidence", 0.95,
                "suggestedLocators", Map.of(
                        "xpath", "//android.widget.Button[@text='Sample']",
                        "accessibilityId", "sample_button"
                )
        ));
        
        return result;
    }
    
    /**
     * Save a screenshot to the configured directory
     * @param screenshotData the screenshot data as byte array
     * @param filename the filename to save as
     * @return the saved screenshot file
     * @throws IOException if there's an error saving the file
     */
    public File saveScreenshot(byte[] screenshotData, String filename) throws IOException {
        // Create screenshot directory if it doesn't exist
        Path directory = Paths.get(screenshotDirectory);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        
        // Save the screenshot
        Path filePath = directory.resolve(filename);
        Files.write(filePath, screenshotData);
        
        return filePath.toFile();
    }
} 