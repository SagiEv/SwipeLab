package com.swipelab.classification.domain;

import com.swipelab.classification.infrastructure.ClassificationRepository;
import com.swipelab.classification.infrastructure.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskDistributionService {

    private final ImageRepository imageRepository;
    private final ClassificationRepository classificationRepository;

    // Gold image insertion ratio: 1 gold image per 15 regular images
    private static final int GOLD_IMAGE_FREQUENCY = 15;

    /**
     * Get the next image for a user to classify.
     * Implements:
     * - Gold image insertion (1 in 15)
     * - Duplicate prevention
     * - Intelligent assignment (prioritize under-classified images)
     */
    @Transactional(readOnly = true)
    public Optional<Image> getNextImageForUser(String username, Long taskId) {
        // Get user's classification count using database to avoid stateful memory leaks
        Long count = classificationRepository.countByUsernameAndTaskId(username, taskId);
        if (count == null) {
            count = 0L;
        }

        // Determine if this should be a gold image (every 15th image)
        boolean shouldBeGold = (count > 0) && (count % GOLD_IMAGE_FREQUENCY == 0);

        if (shouldBeGold) {
            Optional<Image> nextImage = getNextGoldImage(username, taskId);
            // If gold images are available, return it. Otherwise fall through to regular images.
            if (nextImage.isPresent()) {
                return nextImage;
            }
        }
        
        return getNextRegularImage(username, taskId);
    }

    /**
     * Get next gold standard image that user hasn't classified yet
     */
    private Optional<Image> getNextGoldImage(String username, Long taskId) {
        List<Image> unclassifiedGold = imageRepository.findUnclassifiedGoldImages(username, taskId);

        if (unclassifiedGold.isEmpty()) {
            return Optional.empty();
        }

        // Randomly select from available unclassified gold images
        Collections.shuffle(unclassifiedGold);
        return Optional.of(unclassifiedGold.get(0));
    }

    /**
     * Get next regular image with intelligent assignment.
     * Prioritizes images with fewer classifications, resolved optimally via database query.
     */
    private Optional<Image> getNextRegularImage(String username, Long taskId) {
        List<Image> unclassifiedRegular = imageRepository.findNextRegularImageCandidates(
                username, taskId, PageRequest.of(0, 1));

        if (unclassifiedRegular.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(unclassifiedRegular.get(0));
    }

    /**
     * Reset classification count for a user session
     * (No-op now since we compute classifications directly from the database)
     */
    public void resetUserSession(String username, Long taskId) {
        // No-op because state is no longer kept in memory map
        // Method kept to maintain API contract
    }
}
