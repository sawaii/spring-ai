package com.springai.mobile.automation.repository;

import com.springai.mobile.automation.model.Instruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing and manipulating Instruction entities
 */
@Repository
public interface InstructionRepository extends JpaRepository<Instruction, Long> {
    
    /**
     * Find all instructions with the given status
     * @param status the status to filter by
     * @return list of instructions with the specified status
     */
    List<Instruction> findByStatus(Instruction.TestStatus status);
    
    /**
     * Find all instructions ordered by creation date (most recent first)
     * @return list of instructions
     */
    List<Instruction> findAllByOrderByCreatedAtDesc();
} 