package com.swipelab.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class TargetSpeciesRequest {

    /**
     * Scientific name (e.g. "Vespa mandarinia")
     */
    private String name;

    /**
     * Reference images for admin task creation
     */
    private List<ReferenceImageRequest> referenceImages;
}
