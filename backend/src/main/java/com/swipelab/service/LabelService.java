package com.swipelab.service;

import com.swipelab.model.entity.Label;
import com.swipelab.repository.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    /**
     * Retrieve all available labels.
     *
     * @return List of all Label entities.
     */
    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }
}
