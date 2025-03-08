package com.springai.mobile.automation.repository;

import com.springai.mobile.automation.model.Instruction;
import com.springai.mobile.automation.model.TestAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing and manipulating TestAction entities
 */
@Repository
public interface TestActionRepository extends JpaRepository<TestAction, Long> {
    
    /**
     * Find all test actions for a specific instruction, ordered by sequence
     * @param instruction the instruction to find actions for
     * @return ordered list of test actions
     */
    List<TestAction> findByInstructionOrderBySequence(Instruction instruction);
    
    /**
     * Find all successful test actions
     * @return list of successful test actions
     */
    List<TestAction> findBySuccessful(boolean successful);
    
    /**
     * Count the number of actions for a given instruction
     * @param instruction the instruction to count actions for
     * @return the count of actions
     */
    long countByInstruction(Instruction instruction);
} 