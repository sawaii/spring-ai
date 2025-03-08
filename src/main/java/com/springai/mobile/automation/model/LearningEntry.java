package com.springai.mobile.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Model class for storing learned information from past test runs
 * to avoid repeating mistakes
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 1000)
    private String context;
    
    @Column(length = 1000)
    private String action;
    
    private boolean successful;
    
    @Column(length = 2000)
    private String errorDetails;
    
    @Column(length = 2000)
    private String correction;
    
    @Column(length = 2000)
    private String elementIdentifiers;
    
    @Column(length = 1000)
    private String screenDescription;
    
    private LocalDateTime createdAt;
    
    private int useCount;
    
    private float confidenceScore;
    
    /**
     * Increase the use count and recalculate confidence score
     */
    public void incrementUseCount() {
        this.useCount++;
        // Simple confidence score calculation based on usage
        this.confidenceScore = Math.min(0.95f, this.confidenceScore + (0.05f * (this.successful ? 1 : -1)));
    }
} 