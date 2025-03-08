package com.springai.mobile.automation.controller;

import com.springai.mobile.automation.model.Instruction;
import com.springai.mobile.automation.model.TestAction;
import com.springai.mobile.automation.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for test execution and management
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestExecutionService testExecutionService;
    
    @Autowired
    public TestController(TestExecutionService testExecutionService) {
        this.testExecutionService = testExecutionService;
    }
    
    /**
     * Submit a new test instruction
     * @param request map containing the instruction text
     * @return the created instruction
     */
    @PostMapping("/instructions")
    public ResponseEntity<Instruction> submitInstruction(@RequestBody Map<String, String> request) {
        String instructionText = request.get("instruction");
        if (instructionText == null || instructionText.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Instruction instruction = testExecutionService.submitInstruction(instructionText);
        return ResponseEntity.ok(instruction);
    }
    
    /**
     * Execute a test instruction
     * @param id the instruction ID
     * @param async whether to execute asynchronously
     * @return the instruction or a message if async
     */
    @PostMapping("/instructions/{id}/execute")
    public ResponseEntity<?> executeInstruction(
            @PathVariable Long id, 
            @RequestParam(defaultValue = "false") boolean async) {
        
        Instruction instruction = testExecutionService.getAllInstructions().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (instruction == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (async) {
            CompletableFuture<Instruction> future = testExecutionService.processInstructionAsync(instruction);
            return ResponseEntity.accepted().body(Map.of("message", "Instruction execution started"));
        } else {
            Instruction result = testExecutionService.processInstruction(instruction);
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Get all instructions
     * @return list of all instructions
     */
    @GetMapping("/instructions")
    public ResponseEntity<List<Instruction>> getAllInstructions() {
        return ResponseEntity.ok(testExecutionService.getAllInstructions());
    }
    
    /**
     * Get instructions by status
     * @param status the status to filter by
     * @return list of matching instructions
     */
    @GetMapping("/instructions/status/{status}")
    public ResponseEntity<List<Instruction>> getInstructionsByStatus(@PathVariable String status) {
        try {
            Instruction.TestStatus testStatus = Instruction.TestStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(testExecutionService.getInstructionsByStatus(testStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get test actions for an instruction
     * @param id the instruction ID
     * @return list of test actions
     */
    @GetMapping("/instructions/{id}/actions")
    public ResponseEntity<List<TestAction>> getActionsForInstruction(@PathVariable Long id) {
        Instruction instruction = testExecutionService.getAllInstructions().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (instruction == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(testExecutionService.getActionsForInstruction(instruction));
    }
} 