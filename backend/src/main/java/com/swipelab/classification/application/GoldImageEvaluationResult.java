package com.swipelab.classification.application;

/**
 * Result returned by GoldImageEvaluatorService after evaluating a user's response
 * against a gold-standard image.
 */
public record GoldImageEvaluationResult(
        boolean isCorrect,
        String species
) {}
