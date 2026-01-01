package com.swipelab.controller;

import com.swipelab.model.entity.Label;
import com.swipelab.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelRepository labelRepository;

    @GetMapping
    public ResponseEntity<List<Label>> getAllLabels() {
        return ResponseEntity.ok(labelRepository.findAll());
    }
}
