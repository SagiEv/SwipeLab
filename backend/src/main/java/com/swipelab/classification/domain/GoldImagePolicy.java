package com.swipelab.classification.domain;

/**
 * Policy interface that decides whether the next image served to a user
 * should be a gold-standard (quality control) image.
 *
 * Separates the distribution policy from both the data access layer and
 * the TaskDistributionService orchestration logic.
 */
public interface GoldImagePolicy {

    /**
     * Returns true if the next image served to this user for this task
     * should be a gold-standard image.
     *
     * @param username the user being served
     * @param taskId   the task context
     */
    boolean shouldServeGoldImage(String username, Long taskId);
}
