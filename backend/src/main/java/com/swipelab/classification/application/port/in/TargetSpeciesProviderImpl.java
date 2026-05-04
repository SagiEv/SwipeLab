package com.swipelab.classification.application.port.in;

import com.swipelab.classification.domain.Label;
import com.swipelab.classification.infrastructure.LabelRepository;
import com.swipelab.dto.response.TargetSpeciesResponse;
import com.swipelab.tasks.application.port.out.TargetSpeciesProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TargetSpeciesProviderImpl implements TargetSpeciesProvider {

    private final LabelRepository labelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TargetSpeciesResponse> getSpeciesByIds(List<Long> speciesIds) {
        if (speciesIds == null || speciesIds.isEmpty()) {
            return Collections.emptyList();
        }
        return labelRepository.findAllById(speciesIds).stream()
                .map(label -> TargetSpeciesResponse.builder()
                        .name(label.getName())
                        .commonName(label.getCommonName())
                        .referenceImages(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Long> getOrCreateSpeciesIds(List<String> speciesNames) {
        if (speciesNames == null || speciesNames.isEmpty()) {
            return Collections.emptyList();
        }
        return speciesNames.stream().map(name -> {
            Label label = labelRepository.findByName(name)
                    .orElseGet(() -> labelRepository.save(Label.builder().name(name).build()));
            return label.getId();
        }).collect(Collectors.toList());
    }
}
