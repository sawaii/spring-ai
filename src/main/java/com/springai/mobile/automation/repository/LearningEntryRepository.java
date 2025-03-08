package com.springai.mobile.automation.repository;

import com.springai.mobile.automation.model.LearningEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing and manipulating LearningEntry entities
 */
@Repository
public interface LearningEntryRepository extends JpaRepository<LearningEntry, Long> {
    
    /**
     * Find learning entries similar to the given context
     * @param context the context to match
     * @return list of matching learning entries
     */
    @Query("SELECT l FROM LearningEntry l WHERE l.context LIKE %:context%")
    List<LearningEntry> findSimilarContext(@Param("context") String context);
    
    /**
     * Find learning entries with high confidence scores for a similar context
     * @param context the context to match
     * @param minConfidence the minimum confidence score
     * @return list of high-confidence learning entries
     */
    @Query("SELECT l FROM LearningEntry l WHERE l.context LIKE %:context% AND l.confidenceScore >= :minConfidence ORDER BY l.confidenceScore DESC")
    List<LearningEntry> findHighConfidenceEntries(@Param("context") String context, @Param("minConfidence") float minConfidence);
    
    /**
     * Find learning entries for a specific screen description
     * @param screenDescription the screen description to match
     * @return list of learning entries for the screen
     */
    List<LearningEntry> findByScreenDescriptionContaining(String screenDescription);
    
    /**
     * Find learning entries that contain specific element identifiers
     * @param elementIdentifiers the element identifiers to match
     * @return list of learning entries with matching elements
     */
    List<LearningEntry> findByElementIdentifiersContaining(String elementIdentifiers);
} 