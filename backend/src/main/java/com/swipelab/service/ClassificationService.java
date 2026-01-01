package com.swipelab.service;

import com.swipelab.dto.request.ClassificationRequest;
import com.swipelab.dto.response.ClassificationResponse;
import com.swipelab.exception.EntityNotFoundException;
import com.swipelab.model.entity.Classification;
import com.swipelab.model.entity.Image;
import com.swipelab.model.entity.Label;
import com.swipelab.model.entity.User;
import com.swipelab.repository.ClassificationRepository;
import com.swipelab.repository.ImageRepository;
import com.swipelab.repository.LabelRepository;
import com.swipelab.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final ClassificationRepository classificationRepository;
    private final ImageRepository imageRepository;
    private final LabelRepository labelRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClassificationResponse submitClassification(String username, ClassificationRequest request) {
        User user = userRepository.findByEmail(username) // Assuming username is email
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        Image image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + request.getImageId()));

        Label label = labelRepository.findById(request.getLabelId())
                .orElseThrow(() -> new EntityNotFoundException("Label not found with id: " + request.getLabelId()));

        // Check if already classified
        if (classificationRepository.existsByUser_UsernameAndImage_Id(username, request.getImageId())) {
            throw new IllegalArgumentException("User has already classified this image");
        }

        Classification classification = Classification.builder()
                .user(user)
                .image(image)
                .label(label)
                .build();

        Classification saved = classificationRepository.save(classification);

        // Determine correctness if it's a gold standard image
        Boolean isCorrect = null;
        if (Boolean.TRUE.equals(image.getIsGoldStandard()) && image.getCorrectLabel() != null) {
            isCorrect = image.getCorrectLabel().getId().equals(label.getId());
            // TODO: Update user credibility score based on isCorrect
        }

        return mapToResponse(saved, isCorrect);
    }

    private ClassificationResponse mapToResponse(Classification classification, Boolean isCorrect) {
        return ClassificationResponse.builder()
                .id(classification.getId())
                .userId(classification.getUser().getId())
                .imageId(classification.getImage().getId())
                .labelId(classification.getLabel().getId())
                .isCorrect(isCorrect)
                .createdAt(classification.getCreatedAt())
                .build();
    }
}
