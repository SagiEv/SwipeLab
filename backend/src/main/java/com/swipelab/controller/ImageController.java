package com.swipelab.controller;

import com.swipelab.dto.request.ImageUploadRequest;
import com.swipelab.dto.response.ImageBatchResponse;
import com.swipelab.dto.response.ImageResponse;
import com.swipelab.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageResponse> uploadImage(@Valid @RequestBody ImageUploadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imageService.uploadImage(request));
    }

    @GetMapping("/batch")
    public ResponseEntity<ImageBatchResponse> getImageBatch(@RequestParam Long taskId) {
        // TODO: Add user context to filter already labeled images
        return ResponseEntity.ok(imageService.getImageBatch(taskId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageResponse> getImageById(@PathVariable Long id) {
        return ResponseEntity.ok(imageService.getImageById(id));
    }
}
