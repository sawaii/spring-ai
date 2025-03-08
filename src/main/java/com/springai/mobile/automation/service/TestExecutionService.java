package com.springai.mobile.automation.service;

import com.springai.mobile.automation.model.Instruction;
import com.springai.mobile.automation.model.TestAction;
import com.springai.mobile.automation.repository.InstructionRepository;
import com.springai.mobile.automation.repository.TestActionRepository;
import com.springai.mobile.automation.service.ai.InstructionProcessorService;
import com.springai.mobile.automation.service.mobile.MobileAutomationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for orchestrating the entire test execution process
 */
@Service
public class TestExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(TestExecutionService.class);
    
    private final InstructionRepository instructionRepository;
    private final TestActionRepository testActionRepository;
    private final InstructionProcessorService instructionProcessorService;
    private final MobileAutomationService mobileAutomationService;
    
    @Autowired
    public TestExecutionService(
            InstructionRepository instructionRepository,
            TestActionRepository testActionRepository,
            InstructionProcessorService instructionProcessorService,
            MobileAutomationService mobileAutomationService) {
        this.instructionRepository = instructionRepository;
        this.testActionRepository = testActionRepository;
        this.instructionProcessorService = instructionProcessorService;
        this.mobileAutomationService = mobileAutomationService;
    }
    
    /**
     * Submit a new test instruction for processing
     * @param instructionText the instruction text
     * @return the created instruction
     */
    public Instruction submitInstruction(String instructionText) {
        Instruction instruction = Instruction.builder()
                .text(instructionText)
                .status(Instruction.TestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        
        return instructionRepository.save(instruction);
    }
    
    /**
     * Process a pending instruction asynchronously
     * @param instruction the instruction to process
     * @return CompletableFuture for the processed instruction
     */
    public CompletableFuture<Instruction> processInstructionAsync(Instruction instruction) {
        return CompletableFuture.supplyAsync(() -> processInstruction(instruction));
    }
    
    /**
     * Process an instruction and execute the resulting test actions
     * @param instruction the instruction to process
     * @return the updated instruction
     */
    public Instruction processInstruction(Instruction instruction) {
        try {
            logger.info("Processing instruction: {}", instruction.getText());
            
            // Update status to in progress
            instruction.setStatus(Instruction.TestStatus.IN_PROGRESS);
            instructionRepository.save(instruction);
            
            // Process instruction to generate test actions
            List<TestAction> actions = instructionProcessorService.processInstruction(instruction);
            
            // Save generated actions
            for (TestAction action : actions) {
                testActionRepository.save(action);
            }
            
            // Initialize the driver
            mobileAutomationService.initializeDriver();
            
            // Execute each action in sequence
            boolean allSuccessful = true;
            StringBuilder resultBuilder = new StringBuilder();
            
            for (TestAction action : actions) {
                logger.info("Executing action: {} on {}", 
                        action.getActionType(), action.getElementDescription());
                
                boolean success = mobileAutomationService.executeAction(action);
                testActionRepository.save(action);
                
                if (!success) {
                    allSuccessful = false;
                    resultBuilder.append("Failed at step ").append(action.getSequence())
                            .append(": ").append(action.getActionType())
                            .append(" on ").append(action.getElementDescription())
                            .append(" - ").append(action.getErrorMessage())
                            .append("\n");
                }
            }
            
            // Update instruction status based on execution results
            if (allSuccessful) {
                instruction.setStatus(Instruction.TestStatus.COMPLETED);
                instruction.setResult("All actions completed successfully");
            } else {
                instruction.setStatus(Instruction.TestStatus.FAILED);
                instruction.setResult(resultBuilder.toString());
            }
            
            instruction.setProcessedAt(LocalDateTime.now());
            return instructionRepository.save(instruction);
            
        } catch (Exception e) {
            logger.error("Error processing instruction", e);
            
            // Update instruction status to failed
            instruction.setStatus(Instruction.TestStatus.FAILED);
            instruction.setResult("Error: " + e.getMessage());
            instruction.setProcessedAt(LocalDateTime.now());
            return instructionRepository.save(instruction);
        } finally {
            // Cleanup resources
            mobileAutomationService.cleanup();
        }
    }
    
    /**
     * Get all instructions
     * @return list of all instructions
     */
    public List<Instruction> getAllInstructions() {
        return instructionRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * Get instructions by status
     * @param status the status to filter by
     * @return list of matching instructions
     */
    public List<Instruction> getInstructionsByStatus(Instruction.TestStatus status) {
        return instructionRepository.findByStatus(status);
    }
    
    /**
     * Get test actions for an instruction
     * @param instruction the instruction
     * @return list of test actions
     */
    public List<TestAction> getActionsForInstruction(Instruction instruction) {
        return testActionRepository.findByInstructionOrderBySequence(instruction);
    }
} 