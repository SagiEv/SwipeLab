package com.swipelab.classification.domain;

import com.swipelab.classification.dto.api.*;
import com.swipelab.classification.infrastructure.GoldImageRepository;
import com.swipelab.dto.request.ImageUploadRequest;
import com.swipelab.dto.response.ImageBatchResponse;
import com.swipelab.dto.response.ImageResponse;
import com.swipelab.exception.ResourceNotFoundException;

import com.swipelab.classification.application.port.out.TaskProvider;
import com.swipelab.classification.infrastructure.ImageRepository;
import com.swipelab.classification.infrastructure.LabelRepository;
import com.swipelab.classification.infrastructure.ClassificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

@Service
@RequiredArgsConstructor
public class ImageService {

        private final ImageRepository imageRepository;
        private final TaskProvider taskProvider;
        private final LabelRepository labelRepository;
        private final ClassificationRepository classificationRepository;
        private final GoldImageRepository goldImageRepository;
        private final TaskDistributionService taskDistributionService;

        @Transactional(readOnly = true)
        public NextBatchResponse getNextBatchForApi(Long taskId, String username, int count) {
                TaskProvider.TaskInfo taskInfo = taskProvider.getTaskInfo(taskId);
                List<String> taskSpecies = taskInfo.targetSpeciesNames();

                List<BatchImageDto> batchImages = new ArrayList<>();
                int attempt = 0;
                int found = 0;

                while (found < count && attempt < count * 3) {
                        Optional<TaskDistributionService.ImageSpeciesPair> pairOpt =
                                taskDistributionService.getNextImageForUser(username, taskId, taskSpecies);
                        if (pairOpt.isEmpty()) break;

                        TaskDistributionService.ImageSpeciesPair pair = pairOpt.get();

                        // Deduplicate within this batch on image+species
                        boolean alreadyInBatch = batchImages.stream()
                                .anyMatch(b -> b.getImageId().equals(pair.image().getId())
                                        && b.getQuestion() != null
                                        && b.getQuestion().contains(pair.species() != null ? pair.species() : ""));
                        if (!alreadyInBatch) {
                                batchImages.add(mapToBatchDto(pair.image(), taskInfo, pair.species()));
                                found++;
                        }
                        attempt++;
                }

                return NextBatchResponse.builder().images(batchImages).build();
        }


        private BatchImageDto mapToBatchDto(Image image, TaskProvider.TaskInfo taskInfo, String species) {
                String src = getProvidedImagePath(image.getSrcPath());
                String contentType = "image/jpeg";

                // Build question from the explicitly selected species for this image
                String question;
                if (taskInfo.question() != null && !taskInfo.question().isBlank()) {
                        question = taskInfo.question();
                } else if (species != null && !species.isBlank()) {
                        question = "Is this a " + species + "?";
                } else {
                        question = "Classify this image";
                }

                return BatchImageDto.builder()
                                .imageId(image.getId())
                                .taskId(taskInfo.id())
                                .question(question)
                                .image(ImageDataDto.builder()
                                                .contentType(contentType)
                                                .data(src)
                                                .build())
                                .referenceImages(List.of())
                                .build();
        }

        public String getProvidedImagePath(String path) {
                if (path == null) {
                        return getFallbackImage();
                }

                // Already a full HTTP URL — return as-is
                if (path.startsWith("http")) {
                        return path;
                }

                // Local file path — read from disk and return as base64
                try {
                        java.io.File file = new java.io.File(path);
                        if (file.exists() && file.isFile()) {
                                byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                                return java.util.Base64.getEncoder().encodeToString(bytes);
                        }
                } catch (Exception e) {
                        // log and fall through to fallback
                        org.slf4j.LoggerFactory.getLogger(ImageService.class)
                                .warn("Could not read image from path: {}", path, e);
                }

                // Already raw base64 or data URI
                if (path.startsWith("data:image") || path.startsWith("/9j") || path.startsWith("iVBOR")) {
                        return path;
                }

                return getFallbackImage();
        }

        private String getFallbackImage() {
                // 1x1 white JPEG
                return "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=";
        }

        @Transactional
        public ImageResponse uploadImage(ImageUploadRequest request) {
                TaskProvider.TaskInfo taskInfo = taskProvider.getTaskInfo(request.getTaskId());

                Image image = Image.builder()
                                .srcPath(request.getImageUrl())
                                .caption(request.getCaption())
                                .taskId(taskInfo.id())
                                .priority(request.getPriority())
                                .build();

                Image savedImage = imageRepository.save(image);

                if (Boolean.TRUE.equals(request.getIsGoldStandard())) {
                        if (request.getCorrectLabelId() != null) {
                                Label label = labelRepository.findById(request.getCorrectLabelId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Label not found: " + request.getCorrectLabelId()));

                                GoldImage goldImage = GoldImage.builder()
                                                .image(savedImage)
                                                .species(label.getName())
                                                .correctAnswer(GoldImage.UserResponse.YES)
                                                .build();
                                goldImageRepository.save(goldImage);
                        }
                }

                return mapToResponse(savedImage);
        }

        @Transactional(readOnly = true)
        public ImageBatchResponse getImageBatch(Long taskId, String username) {
                if (taskId == null) {
                        throw new IllegalArgumentException("Task ID cannot be null");
                }

                List<Image> allImages = imageRepository.findByTaskId(taskId);
                List<Image> unclassifiedImages;

                if (username != null && !username.isEmpty()) {
                        unclassifiedImages = allImages.stream()
                                        .filter(image -> !classificationRepository
                                                        .existsByUsernameAndImageId(username, image.getId()))
                                        .collect(Collectors.toList());
                } else {
                        unclassifiedImages = allImages;
                }

                Collections.shuffle(unclassifiedImages);

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
                boolean isGold = goldImageRepository.existsByImageId(image.getId());
                return ImageResponse.builder()
                                .id(image.getId())
                                .imageUrl(image.getSrcPath())
                                .thumbnailUrl(image.getThumbnailUrl())
                                .caption(image.getCaption())
                                .taskId(image.getTaskId())
                                .priority(image.getPriority())
                                .isGoldStandard(isGold)
                                .build();
        }

        public ResponseEntity<byte[]> getImageContent(Long id) {
                Image image = imageRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + id));
                String path = image.getSrcPath();
                if (path == null) {
                        return ResponseEntity.notFound().build();
                }

                if (path.startsWith("http") || path.startsWith("data:image") || path.startsWith("/9j") || path.startsWith("iVBOR")) {
                        // Not a local file, we can't easily serve it as a raw byte array endpoint
                        // Frontend should use the original URL/base64 directly
                        return ResponseEntity.status(HttpStatus.SEE_OTHER).header(HttpHeaders.LOCATION, path).build();
                }

                try {
                        java.io.File file = new java.io.File(path);
                        if (file.exists() && file.isFile()) {
                                byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
                                return ResponseEntity.ok()
                                                .contentType(MediaType.IMAGE_JPEG)
                                                .body(bytes);
                        }
                } catch (Exception e) {
                        org.slf4j.LoggerFactory.getLogger(ImageService.class)
                                        .warn("Could not read image from path: {}", path, e);
                }
                return ResponseEntity.notFound().build();
        }
}
