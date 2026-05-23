package com.swipelab.collection.application;

import com.swipelab.collection.domain.UserCollectionEntry;
import com.swipelab.collection.dto.CollectionEntryResponse;
import com.swipelab.collection.dto.CollectionStatsResponse;
import com.swipelab.collection.infrastructure.UserCollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private UserCollectionRepository collectionRepository;

    @InjectMocks
    private CollectionService collectionService;

    // ── recordYesTag ─────────────────────────────────────────────────────────

    @Test
    void recordYesTag_ShouldPersistEntry_AndReturnResponse() {
        UserCollectionEntry saved = UserCollectionEntry.builder()
                .id(1L)
                .username("alice")
                .imageId(42L)
                .species("Bumblebee")
                .imageUrl("https://example.com/img.jpg")
                .taskId(7L)
                .taggedAt(LocalDateTime.now())
                .build();

        when(collectionRepository.save(any(UserCollectionEntry.class))).thenReturn(saved);

        CollectionEntryResponse response =
                collectionService.recordYesTag("alice", 42L, "Bumblebee", "https://example.com/img.jpg", 7L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getSpecies()).isEqualTo("Bumblebee");
        assertThat(response.getImageId()).isEqualTo(42L);

        ArgumentCaptor<UserCollectionEntry> captor = ArgumentCaptor.forClass(UserCollectionEntry.class);
        verify(collectionRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("alice");
    }

    @Test
    void recordYesTag_CalledTwice_ShouldInsertTwoSeparateRows() {
        // Intentional: no dedup at service level — each YES swipe is its own entry.
        UserCollectionEntry entry = UserCollectionEntry.builder()
                .id(1L).username("alice").imageId(42L).species("Bumblebee")
                .taggedAt(LocalDateTime.now()).build();

        when(collectionRepository.save(any())).thenReturn(entry);

        collectionService.recordYesTag("alice", 42L, "Bumblebee", null, 7L);
        collectionService.recordYesTag("alice", 42L, "Bumblebee", null, 7L);

        verify(collectionRepository, times(2)).save(any(UserCollectionEntry.class));
    }

    // ── getCollection ────────────────────────────────────────────────────────

    @Test
    void getCollection_ShouldReturnAllEntriesForUser() {
        UserCollectionEntry e1 = UserCollectionEntry.builder()
                .id(1L).username("alice").imageId(1L).species("Ant").taggedAt(LocalDateTime.now()).build();
        UserCollectionEntry e2 = UserCollectionEntry.builder()
                .id(2L).username("alice").imageId(2L).species("Bee").taggedAt(LocalDateTime.now()).build();

        when(collectionRepository.findByUsernameOrderByTaggedAtDesc("alice")).thenReturn(List.of(e1, e2));

        List<CollectionEntryResponse> result = collectionService.getCollection("alice");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSpecies()).isEqualTo("Ant");
    }

    // ── getStats ─────────────────────────────────────────────────────────────

    @Test
    void getStats_ShouldReturnCorrectCount() {
        when(collectionRepository.countByUsername("alice")).thenReturn(5L);

        CollectionStatsResponse stats = collectionService.getStats("alice");

        assertThat(stats.getTotal()).isEqualTo(5L);
    }
}
