package com.springai.mobile.automation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Model class representing a specific test action to be performed
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAction {

    // Set of reserved H2 keywords to be avoided in SQL statements
    private static final Set<String> RESERVED_KEYWORDS = new HashSet<>();
    
    static {
        // Initialize common H2 reserved keywords
        RESERVED_KEYWORDS.add("VALUE");
        RESERVED_KEYWORDS.add("KEY");
        RESERVED_KEYWORDS.add("INDEX");
        RESERVED_KEYWORDS.add("ORDER");
        RESERVED_KEYWORDS.add("PRIMARY");
        RESERVED_KEYWORDS.add("TABLE");
        RESERVED_KEYWORDS.add("COLUMN");
        // Add more as needed
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instruction_id")
    private Instruction instruction;
    
    private ActionType actionType;
    
    @Column(length = 1000)
    private String elementLocator;
    
    @Column(length = 1000)
    private String elementDescription;
    
    // Rename 'value' to 'input_value' to avoid H2 reserved keyword conflict
    @Column(name = "input_value", length = 1000)
    private String value;
    
    private int sequence;
    
    private boolean successful;
    
    @Column(length = 4000)
    private String errorMessage;
    
    @Column(length = 1000)
    private String screenshot;
    
    private LocalDateTime executedAt;
    
    /**
     * Utility method to validate field values against reserved keywords
     * @param fieldName Name of the field to check
     * @param value Value to validate
     * @return Sanitized value safe for database operations
     */
    public static String validateFieldValue(String fieldName, String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        
        String upperValue = value.toUpperCase();
        if (RESERVED_KEYWORDS.contains(upperValue)) {
            return value + "_safe"; // Append suffix to avoid keyword conflicts
        }
        
        return value;
    }
    
    /**
     * Enum representing the type of action to perform
     */
    public enum ActionType {
        TAP,
        LONG_PRESS,
        TYPE,
        CLEAR,
        SWIPE,
        SCROLL,
        BACK,
        VERIFY_TEXT,
        VERIFY_ELEMENT,
        WAIT,
        LAUNCH_APP,
        CLOSE_APP,
        TAKE_SCREENSHOT
    }
} 