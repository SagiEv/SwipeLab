package com.swipelab.classification.application;

import com.swipelab.classification.domain.SpeciesReferenceImage;
import com.swipelab.classification.dto.api.SpeciesReferenceImageDto;
import com.swipelab.classification.infrastructure.SpeciesReferenceImageRepository;
import com.swipelab.exception.ResourceNotFoundException;
import com.swipelab.infrastructure.ImageProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeciesReferenceImageServiceTest {

    @Mock
    private SpeciesReferenceImageRepository repository;

    @Mock
    private ImageProcessingService imageProcessingService;

    @InjectMocks
    private SpeciesReferenceImageService service;

    private SpeciesReferenceImage sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = SpeciesReferenceImage.builder()
                .id(1L)
                .labelId(42L)
                .imagePath("/uploads/ref/abc.jpg")
                .thumbnailPath("/uploads/ref/thumb/abc.jpg")
                .fileSizeBytes(102400L)
                .caption("Test caption")
                .uploadedBy("researcher1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Happy flows ────────────────────────────────────────────────────────────

    @Test
    void uploadImages_happyFlow_savesAndReturnsDto() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "files", "bee.jpg", "image/jpeg", new byte[1024]);

        var processed = new ImageProcessingService.ProcessedImageResult(
                "/uploads/ref/uuid1.jpg", "/uploads/ref/thumb/uuid1.jpg", 80000L);

        when(repository.countByLabelId(42L)).thenReturn(0L);
        when(imageProcessingService.processAndStore(file)).thenReturn(processed);
        when(repository.save(any())).thenReturn(sampleEntity);

        List<SpeciesReferenceImageDto> result = service.uploadImages(42L, List.of(file), "researcher1", "caption");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLabelId()).isEqualTo(42L);
        assertThat(result.get(0).getImageUrl()).contains("/image");
        assertThat(result.get(0).getThumbnailUrl()).contains("/thumbnail");

        verify(repository, times(1)).save(any(SpeciesReferenceImage.class));
    }

    @Test
    void getImagesForSpecies_returnsAllPoolImages() {
        when(repository.findByLabelId(42L)).thenReturn(List.of(sampleEntity));

        List<SpeciesReferenceImageDto> result = service.getImagesForSpecies(42L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getImagesForSpeciesBatch_groupsByLabelId() {
        SpeciesReferenceImage second = SpeciesReferenceImage.builder()
                .id(2L).labelId(99L)
                .imagePath("/uploads/ref/x.jpg").thumbnailPath("/uploads/ref/thumb/x.jpg")
                .uploadedBy("researcher1").createdAt(LocalDateTime.now()).build();

        when(repository.findByLabelIdIn(List.of(42L, 99L)))
                .thenReturn(List.of(sampleEntity, second));

        Map<Long, List<SpeciesReferenceImageDto>> result =
                service.getImagesForSpeciesBatch(List.of(42L, 99L));

        assertThat(result).containsKeys(42L, 99L);
        assertThat(result.get(42L)).hasSize(1);
        assertThat(result.get(99L)).hasSize(1);
    }

    @Test
    void deleteImage_byUploader_deletesSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        service.deleteImage(1L, "researcher1", false);

        verify(repository, times(1)).delete(sampleEntity);
    }

    @Test
    void deleteImage_bySuperAdmin_deletesSuccessfully() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        service.deleteImage(1L, "admin", true);

        verify(repository, times(1)).delete(sampleEntity);
    }

    // ── Edge / failure cases ───────────────────────────────────────────────────

    @Test
    void uploadImages_rejectsEmptyFileList() {
        assertThatThrownBy(() -> service.uploadImages(42L, List.of(), "researcher1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one");
    }

    @Test
    void uploadImages_rejectsMoreThanThreeFiles() {
        MockMultipartFile file = new MockMultipartFile("f", "a.jpg", "image/jpeg", new byte[1]);
        List<org.springframework.web.multipart.MultipartFile> files =
                List.of(file, file, file, file); // 4 files

        assertThatThrownBy(() -> service.uploadImages(42L, files, "researcher1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum 3");
    }

    @Test
    void uploadImages_rejectsWhenPoolFull() {
        var file = new MockMultipartFile("f", "a.jpg", "image/jpeg", new byte[1]);
        when(repository.countByLabelId(42L)).thenReturn(10L); // already at max

        assertThatThrownBy(() -> service.uploadImages(42L, List.of(file), "researcher1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Max pool size");
    }

    @Test
    void deleteImage_byNonUploaderNonAdmin_throwsAccessDenied() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        assertThatThrownBy(() -> service.deleteImage(1L, "other_user", false))
                .isInstanceOf(AccessDeniedException.class);

        verify(repository, never()).delete(any());
    }

    @Test
    void getEntityById_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntityById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
