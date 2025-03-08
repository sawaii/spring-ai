package com.springai.mobile.automation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing a user test instruction
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Instruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Instruction text cannot be blank")
    private String text;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime processedAt;
    
    private TestStatus status;
    
    @Column(length = 4000)
    private String result;
    
    /**
     * Enum representing the status of test execution
     */
    public enum TestStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
} 