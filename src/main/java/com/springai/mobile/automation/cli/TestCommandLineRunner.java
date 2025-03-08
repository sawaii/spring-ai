package com.springai.mobile.automation.cli;

import com.springai.mobile.automation.model.Instruction;
import com.springai.mobile.automation.model.TestAction;
import com.springai.mobile.automation.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface for the mobile automation framework
 * Only active when the 'cli' profile is enabled
 */
@Component
@Profile("cli")
public class TestCommandLineRunner implements CommandLineRunner {

    private final TestExecutionService testExecutionService;
    private final Scanner scanner = new Scanner(System.in);
    
    @Autowired
    public TestCommandLineRunner(TestExecutionService testExecutionService) {
        this.testExecutionService = testExecutionService;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("=================================================");
        System.out.println("  Spring AI Mobile Automation Framework CLI");
        System.out.println("=================================================");
        System.out.println();
        
        boolean running = true;
        while (running) {
            System.out.println("\nAvailable commands:");
            System.out.println("1. Execute test instruction");
            System.out.println("2. List recent instructions");
            System.out.println("3. View instruction details");
            System.out.println("4. Exit");
            System.out.print("\nEnter command number: ");
            
            String command = scanner.nextLine();
            
            switch (command) {
                case "1":
                    executeInstruction();
                    break;
                case "2":
                    listInstructions();
                    break;
                case "3":
                    viewInstructionDetails();
                    break;
                case "4":
                    running = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
            }
        }
    }
    
    /**
     * Execute a new test instruction
     */
    private void executeInstruction() {
        System.out.println("\n=== Execute Test Instruction ===");
        System.out.println("Enter your test instruction (e.g., 'Login with username testuser and password test123'):");
        String instructionText = scanner.nextLine();
        
        if (instructionText.isEmpty()) {
            System.out.println("Instruction cannot be empty. Returning to main menu.");
            return;
        }
        
        System.out.println("Processing instruction...");
        Instruction instruction = testExecutionService.submitInstruction(instructionText);
        
        System.out.println("Executing test actions...");
        instruction = testExecutionService.processInstruction(instruction);
        
        System.out.println("\nExecution completed with status: " + instruction.getStatus());
        if (instruction.getResult() != null) {
            System.out.println("Result: " + instruction.getResult());
        }
    }
    
    /**
     * List recent instructions
     */
    private void listInstructions() {
        System.out.println("\n=== Recent Instructions ===");
        List<Instruction> instructions = testExecutionService.getAllInstructions();
        
        if (instructions.isEmpty()) {
            System.out.println("No instructions found.");
            return;
        }
        
        System.out.println("ID | Status | Created At | Instruction");
        System.out.println("-------------------------------------------");
        
        for (Instruction instruction : instructions) {
            System.out.printf("%d | %s | %s | %s%n", 
                    instruction.getId(), 
                    instruction.getStatus(), 
                    instruction.getCreatedAt(), 
                    instruction.getText());
        }
    }
    
    /**
     * View details of a specific instruction
     */
    private void viewInstructionDetails() {
        System.out.println("\n=== View Instruction Details ===");
        System.out.print("Enter instruction ID: ");
        
        try {
            long id = Long.parseLong(scanner.nextLine());
            
            Instruction instruction = testExecutionService.getAllInstructions().stream()
                    .filter(i -> i.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (instruction == null) {
                System.out.println("Instruction not found with ID: " + id);
                return;
            }
            
            System.out.println("\nInstruction Details:");
            System.out.println("ID: " + instruction.getId());
            System.out.println("Text: " + instruction.getText());
            System.out.println("Status: " + instruction.getStatus());
            System.out.println("Created At: " + instruction.getCreatedAt());
            System.out.println("Processed At: " + instruction.getProcessedAt());
            System.out.println("Result: " + instruction.getResult());
            
            List<TestAction> actions = testExecutionService.getActionsForInstruction(instruction);
            
            System.out.println("\nTest Actions (" + actions.size() + "):");
            System.out.println("Seq | Type | Element | Value | Success | Error");
            System.out.println("-------------------------------------------");
            
            for (TestAction action : actions) {
                System.out.printf("%d | %s | %s | %s | %s | %s%n", 
                        action.getSequence(), 
                        action.getActionType(), 
                        action.getElementDescription(), 
                        action.getValue() != null ? action.getValue() : "", 
                        action.isSuccessful(), 
                        action.getErrorMessage() != null ? action.getErrorMessage() : "");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format. Please enter a number.");
        }
    }
} 