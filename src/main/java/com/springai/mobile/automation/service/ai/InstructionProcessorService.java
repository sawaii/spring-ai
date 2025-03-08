package com.springai.mobile.automation.service.ai;

import com.springai.mobile.automation.model.Instruction;
import com.springai.mobile.automation.model.TestAction;
import com.springai.mobile.automation.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for processing user instructions using Gemini AI
 */
@Service
public class InstructionProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(InstructionProcessorService.class);
    
    private static final String SYSTEM_PROMPT = """
        You are an expert mobile testing assistant.
        Your task is to analyze the user's test instruction and break it down into a sequence of specific actions to perform on a mobile app.
        
        For each instruction, provide a JSON array of test actions in the following format:
        [
          {
            "actionType": "ACTION_TYPE",
            "elementDescription": "Detailed description of the element",
            "value": "Value to input (if applicable)",
            "sequence": number
          }
        ]
        
        ACTION_TYPE must be one of: TAP, LONG_PRESS, TYPE, CLEAR, SWIPE, SCROLL, BACK, VERIFY_TEXT, VERIFY_ELEMENT, WAIT, LAUNCH_APP, CLOSE_APP, TAKE_SCREENSHOT
        
        Example:
        For "Login with username 'testuser' and password 'password123'", the output would be:
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
          },
          {
            "actionType": "TAP",
            "elementDescription": "Password input field",
            "value": "",
            "sequence": 3
          },
          {
            "actionType": "TYPE",
            "elementDescription": "Password input field",
            "value": "password123",
            "sequence": 4
          },
          {
            "actionType": "TAP",
            "elementDescription": "Login button",
            "value": "",
            "sequence": 5
          },
          {
            "actionType": "VERIFY_TEXT",
            "elementDescription": "Welcome message or home screen indicator",
            "value": "",
            "sequence": 6
          }
        ]
        
        Always be thorough and think step by step about what actions a user would need to perform to complete the instruction.
        Return ONLY the JSON array without any additional text or explanation.
        """;

    private final ChatClient chatClient;

    @Autowired
    public InstructionProcessorService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Process a user instruction and generate a list of test actions
     * @param instruction the user instruction to process
     * @return list of generated test actions
     */
    public List<TestAction> processInstruction(Instruction instruction) {
        logger.info("Processing instruction: {}", instruction.getText());
        
        Message systemMessage = new SystemMessage(SYSTEM_PROMPT);
        UserMessage userMessage = new UserMessage(instruction.getText());
        
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        
        try {
            ChatResponse response = chatClient.call(prompt);
            
            String content = response.getResult().getOutput().getContent();
            logger.debug("Received response from Gemini AI: {}", content);
            
            List<TestAction> actions = parseTestActions(content, instruction);
            logger.info("Generated {} test actions", actions.size());
            
            return actions;
        } catch (Exception e) {
            logger.error("Error processing instruction with Gemini AI", e);
            // Return an empty list in case of error
            return new ArrayList<>();
        }
    }
    
    /**
     * Parse the JSON response from Gemini AI into TestAction objects
     * @param jsonResponse the JSON response from the AI
     * @param instruction the original instruction
     * @return list of TestAction objects
     */
    private List<TestAction> parseTestActions(String jsonResponse, Instruction instruction) {
        // Use JsonUtils to parse the JSON array into TestAction objects
        List<TestAction> actions = JsonUtils.parseTestActions(jsonResponse, instruction);
        
        // If parsing failed, create a dummy action for demonstration
        if (actions.isEmpty()) {
            logger.warn("Failed to parse test actions, creating a dummy action");
            TestAction dummyAction = TestAction.builder()
                    .instruction(instruction)
                    .actionType(TestAction.ActionType.TAP)
                    .elementDescription("Sample element")
                    .sequence(1)
                    .build();
            
            actions.add(dummyAction);
        }
        
        return actions;
    }
} 