package com.springai.mobile.automation.service.learning;

import com.springai.mobile.automation.model.LearningEntry;
import com.springai.mobile.automation.model.TestAction;
import com.springai.mobile.automation.repository.LearningEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for learning from past test actions and applying that knowledge
 * to avoid repeating mistakes
 */
@Service
public class LearningService {

    private final LearningEntryRepository learningEntryRepository;
    
    @Value("${app.automation.learning.enabled:true}")
    private boolean learningEnabled;
    
    @Value("${app.automation.learning.confidence.threshold:0.7}")
    private float confidenceThreshold;

    @Autowired
    public LearningService(LearningEntryRepository learningEntryRepository) {
        this.learningEntryRepository = learningEntryRepository;
    }

    /**
     * Store the result of a test action for future learning
     * @param testAction the executed test action
     * @param screenDescription description of the screen
     * @param elementIdentifiers identified element data
     * @param errorDetails details of any error
     * @param correction correction applied if there was an error
     */
    public void learnFromAction(TestAction testAction, String screenDescription, 
            String elementIdentifiers, String errorDetails, String correction) {
        
        if (!learningEnabled) {
            return;
        }
        
        // Create context from action and screen description
        String context = String.format("%s on screen '%s'", 
                testAction.getElementDescription(), screenDescription);
        
        // Check if we have a similar learning entry
        List<LearningEntry> similarEntries = learningEntryRepository
                .findSimilarContext(context);
        
        if (!similarEntries.isEmpty()) {
            // Update existing entry
            LearningEntry entry = similarEntries.get(0);
            entry.setSuccessful(testAction.isSuccessful());
            
            if (!testAction.isSuccessful()) {
                entry.setErrorDetails(errorDetails);
                entry.setCorrection(correction);
            }
            
            entry.setElementIdentifiers(elementIdentifiers);
            entry.incrementUseCount();
            
            learningEntryRepository.save(entry);
        } else {
            // Create new entry
            LearningEntry newEntry = LearningEntry.builder()
                    .context(context)
                    .action(testAction.getActionType().toString())
                    .successful(testAction.isSuccessful())
                    .errorDetails(errorDetails)
                    .correction(correction)
                    .elementIdentifiers(elementIdentifiers)
                    .screenDescription(screenDescription)
                    .createdAt(LocalDateTime.now())
                    .useCount(1)
                    .confidenceScore(testAction.isSuccessful() ? 0.7f : 0.3f)
                    .build();
            
            learningEntryRepository.save(newEntry);
        }
    }
    
    /**
     * Get learned information for a test action based on screen context
     * @param testAction the test action to check
     * @param screenDescription description of the current screen
     * @return optional containing matching learning entry if found
     */
    public Optional<LearningEntry> getLearnedAction(TestAction testAction, String screenDescription) {
        if (!learningEnabled) {
            return Optional.empty();
        }
        
        String context = String.format("%s on screen '%s'", 
                testAction.getElementDescription(), screenDescription);
        
        List<LearningEntry> entries = learningEntryRepository
                .findHighConfidenceEntries(context, confidenceThreshold);
        
        if (!entries.isEmpty()) {
            return Optional.of(entries.get(0));
        }
        
        return Optional.empty();
    }
    
    /**
     * Check if there are any known issues or past failures for a specific action
     * @param testAction the test action to check
     * @param screenDescription description of the current screen
     * @return true if the action has failed in the past
     */
    public boolean hasPastFailures(TestAction testAction, String screenDescription) {
        if (!learningEnabled) {
            return false;
        }
        
        String context = String.format("%s on screen '%s'", 
                testAction.getElementDescription(), screenDescription);
        
        List<LearningEntry> entries = learningEntryRepository.findSimilarContext(context);
        
        return entries.stream()
                .anyMatch(entry -> !entry.isSuccessful());
    }
    
    /**
     * Get the best correction for a failed action based on past learning
     * @param testAction the failed test action
     * @param screenDescription description of the current screen
     * @return optional containing correction if found
     */
    public Optional<String> getPastCorrection(TestAction testAction, String screenDescription) {
        if (!learningEnabled) {
            return Optional.empty();
        }
        
        String context = String.format("%s on screen '%s'", 
                testAction.getElementDescription(), screenDescription);
        
        List<LearningEntry> entries = learningEntryRepository.findSimilarContext(context);
        
        return entries.stream()
                .filter(entry -> !entry.isSuccessful() && entry.getCorrection() != null && !entry.getCorrection().isEmpty())
                .max((e1, e2) -> Float.compare(e1.getConfidenceScore(), e2.getConfidenceScore()))
                .map(LearningEntry::getCorrection);
    }
} 