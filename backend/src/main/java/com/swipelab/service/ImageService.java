package com.swipelab.service;

import com.swipelab.dto.request.ImageUploadRequest;
import com.swipelab.dto.response.ImageBatchResponse;
import com.swipelab.dto.response.ImageResponse;
import com.swipelab.exception.ResourceNotFoundException;
import com.swipelab.model.entity.Image;
import com.swipelab.model.entity.Label;
import com.swipelab.model.entity.Task;
import com.swipelab.repository.ImageRepository;
import com.swipelab.repository.LabelRepository;
import com.swipelab.repository.TaskRepository;
import com.swipelab.repository.ClassificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

        private final ImageRepository imageRepository;
        private final TaskRepository taskRepository;
        private final LabelRepository labelRepository;
        private final ClassificationRepository classificationRepository;

        @Transactional
        public ImageResponse uploadImage(ImageUploadRequest request) {
                Task task = taskRepository.findById(request.getTaskId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Task not found with id: " + request.getTaskId()));

                Label correctLabel = null;
                if (Boolean.TRUE.equals(request.getIsGoldStandard())) {
                        if (request.getCorrectLabelId() == null) {
                                throw new IllegalArgumentException(
                                                "Correct Label ID is required for Gold Standard images");
                        }
                        correctLabel = labelRepository.findById(request.getCorrectLabelId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Label not found with id: " + request.getCorrectLabelId()));

                }

                Image image = Image.builder()
                                .imageUrl(request.getImageUrl())
                                .caption(request.getCaption())
                                .task(task)
                                .priority(request.getPriority())
                                .isGoldStandard(request.getIsGoldStandard())
                                .correctLabel(correctLabel)
                                .build();

                Image savedImage = imageRepository.save(image);
                return mapToResponse(savedImage);
        }

        @Transactional(readOnly = true)
        public ImageBatchResponse getImageBatch(Long taskId, String username) {
                if (taskId == null) {
                        throw new IllegalArgumentException("Task ID cannot be null");
                }

                // Get all images for the task
                List<Image> allImages = imageRepository.findByTaskId(taskId);

                // Filter out images already classified by this user (if username provided)
                List<Image> unclassifiedImages;
                if (username != null && !username.isEmpty()) {
                        unclassifiedImages = allImages.stream()
                                        .filter(image -> !classificationRepository
                                                        .existsByUser_UsernameAndImage_Id(username, image.getId()))
                                        .collect(Collectors.toList());
                } else {
                        unclassifiedImages = allImages;
                }

                // Shuffle to randomize order
                Collections.shuffle(unclassifiedImages);

                // Limit to reasonable batch size, e.g., 20
                List<ImageResponse> batch = unclassifiedImages.stream()
                                .limit(20)
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return ImageBatchResponse.builder()
                                .images(batch)
                                .build();
        }

        public ImageResponse getImageById(Long id) {
                Image image = imageRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));
                return mapToResponse(image);
        }

        private ImageResponse mapToResponse(Image image) {
                return ImageResponse.builder()
                                .id(image.getId())
                                .imageUrl(image.getImageUrl())
                                .thumbnailUrl(image.getThumbnailUrl())
                                .caption(image.getCaption())
                                .taskId(image.getTask().getId())
                                .priority(image.getPriority())
                                .isGoldStandard(image.getIsGoldStandard())
                                .build();
        }
}
