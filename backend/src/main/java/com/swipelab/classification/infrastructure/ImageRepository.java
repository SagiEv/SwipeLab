package com.swipelab.classification.infrastructure;

import com.swipelab.classification.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByTaskId(Long taskId);

    // Find all unclassified gold standard images for a specific task and user
    @Query("SELECT i FROM Image i WHERE i.task.id = :taskId " +
            "AND i.id IN (SELECT g.image.id FROM GoldImage g) " +
            "AND NOT EXISTS (SELECT c FROM Classification c WHERE c.image.id = i.id AND c.username = :username)")
    List<Image> findUnclassifiedGoldImages(@Param("username") String username, @Param("taskId") Long taskId);

    // Find next regular (non-gold) image with intelligent assignment.
    // Prioritizes images with fewer classifications, and non-classified by the user.
    @Query("SELECT i FROM Image i " +
            "LEFT JOIN Classification c ON c.image.id = i.id " +
            "WHERE i.task.id = :taskId " +
            "AND i.id NOT IN (SELECT g.image.id FROM GoldImage g) " +
            "AND NOT EXISTS (SELECT c2 FROM Classification c2 WHERE c2.image.id = i.id AND c2.username = :username) " +
            "GROUP BY i.id " +
            "ORDER BY i.priority DESC, COUNT(c.id) ASC")
    List<Image> findNextRegularImageCandidates(@Param("username") String username, @Param("taskId") Long taskId, Pageable pageable);
}
