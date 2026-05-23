package com.swipelab.collection.api;

import com.swipelab.collection.application.CollectionService;
import com.swipelab.collection.dto.CollectionEntryResponse;
import com.swipelab.collection.dto.CollectionStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collection")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * Returns all YES-tagged images in the authenticated user's collection,
     * ordered newest first.
     */
    @GetMapping
    public ResponseEntity<List<CollectionEntryResponse>> getMyCollection(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(collectionService.getCollection(userDetails.getUsername()));
    }

    /**
     * Returns aggregate stats for the authenticated user's collection.
     */
    @GetMapping("/stats")
    public ResponseEntity<CollectionStatsResponse> getMyCollectionStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(collectionService.getStats(userDetails.getUsername()));
    }
}
