package com.springai.mobile.automation.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;

/**
 * Mock AI configuration for development and testing
 * Only active when the 'mock' profile is enabled
 */
@Configuration
@Profile("mock")
public class MockAiConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MockAiConfig.class);
    
    /**
     * Create a mock ChatClient for testing without real AI integration
     * @return mock ChatClient
     */
    @Bean
    @Primary
    public ChatClient mockChatClient() {
        logger.info("Creating mock ChatClient for testing");
        return new MockChatClient();
    }
    
    /**
     * Implementation of a mock ChatClient for testing
     */
    private static class MockChatClient implements ChatClient, StreamingChatClient {
        
        @Override
        public ChatResponse call(Prompt prompt) {
            List<Message> inputMessages = prompt.getInstructions();
            String userMessage = "";
            
            // Extract user message text if available
            for (Message message : inputMessages) {
                if (message instanceof UserMessage) {
                    userMessage = ((UserMessage) message).getContent();
                    break;
                }
            }
            
            // Generate mock response based on user message
            String response;
            if (userMessage.contains("login")) {
                response = """
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
                      }
                    ]
                    """;
            } else if (userMessage.contains("screen")) {
                response = """
                    {
                      "screenDescription": "Login Screen",
                      "matchedElement": {
                        "description": "Username input field",
                        "type": "EditText",
                        "text": "",
                        "confidence": 0.95,
                        "bounds": {
                          "x": 100,
                          "y": 200,
                          "width": 300,
                          "height": 50
                        },
                        "suggestedLocators": {
                          "xpath": "//android.widget.EditText[@resource-id='usernameInput']",
                          "accessibilityId": "username_input",
                          "id": "com.example.app:id/usernameInput"
                        }
                      }
                    }
                    """;
            } else {
                response = """
                    [
                      {
                        "actionType": "TAP",
                        "elementDescription": "Menu button",
                        "value": "",
                        "sequence": 1
                      },
                      {
                        "actionType": "TAP",
                        "elementDescription": "Settings option",
                        "value": "",
                        "sequence": 2
                      }
                    ]
                    """;
            }
            
            // Create mock generation
            Generation generation = new Generation(response);
            ChatResponse chatResponse = new ChatResponse(List.of(generation));
            
            return chatResponse;
        }
        
        @Override
        public Flux<ChatResponse> stream(Prompt prompt) {
            // For testing, just return a single response as a Flux
            return Flux.just(call(prompt));
        }
    }
} 