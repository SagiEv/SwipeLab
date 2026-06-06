package com.swipelab.tasks.application.port.out;

import com.swipelab.dto.response.TargetSpeciesResponse;
import java.util.List;

public interface TargetSpeciesProvider {
    List<TargetSpeciesResponse> getSpeciesByIds(List<Long> speciesIds);
    List<TargetSpeciesResponse> getSpeciesByIdsAndRefImages(List<Long> speciesIds, List<Long> refImageIds);
    List<Long> getOrCreateSpeciesIds(List<String> speciesNames);
}
