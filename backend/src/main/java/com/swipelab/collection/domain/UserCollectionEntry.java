package com.swipelab.collection.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * One row per YES-tagged image; no uniqueness constraint on (username, image_id)
 * because two separate YES swipes on the same image are two valid collection entries.
 */
@Entity
@Table(name = "user_collection",
        indexes = {
                @Index(name = "idx_user_collection_username", columnList = "username"),
                @Index(name = "idx_user_collection_image",    columnList = "image_id")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCollectionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    /** FK to images.id — stored as plain Long to keep modules decoupled. */
    @Column(name = "image_id", nullable = false)
    private Long imageId;

    /** Species/category label copied from querySpecies at tag time. */
    @Column
    private String species;

    /** Snapshot of the image URL for display — denormalized to avoid cross-module joins. */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "task_id")
    private Long taskId;

    @CreationTimestamp
    @Column(name = "tagged_at", nullable = false, updatable = false)
    private LocalDateTime taggedAt;
}
