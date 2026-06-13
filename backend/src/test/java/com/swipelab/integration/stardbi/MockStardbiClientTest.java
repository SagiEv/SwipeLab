package com.swipelab.integration.stardbi;

import com.swipelab.integration.stardbi.dto.ExternalLabelDto;
import com.swipelab.integration.stardbi.dto.ExternalTaxonomyDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockStardbiClientTest {

    private MockStardbiClient mockStardbiClient;

    @BeforeEach
    void setUp() {
        mockStardbiClient = new MockStardbiClient();
    }

    @Test
    void testGetTaxonomyReturnsValidSpeciesIds() {
        List<ExternalTaxonomyDto> taxonomy = mockStardbiClient.getTaxonomy();
        
        assertNotNull(taxonomy);
        assertFalse(taxonomy.isEmpty());
        
        for (ExternalTaxonomyDto dto : taxonomy) {
            assertNotNull(dto.getSpeciesId(), "Species ID should not be null in mock taxonomy");
        }
    }

    @Test
    void testPostLabelTracksLabels() {
        ExternalLabelDto label = ExternalLabelDto.builder()
                .boxId(101L)
                .imageId(201L)
                .speciesId(1L)
                .swipeLabUserId("e2e_user")
                .userGrade(3)
                .build();

        mockStardbiClient.postLabel(label);

        List<ExternalLabelDto> postedLabels = mockStardbiClient.getPostedLabels();
        assertEquals(1, postedLabels.size());
        assertEquals(101L, postedLabels.get(0).getBoxId());
        assertEquals("e2e_user", postedLabels.get(0).getSwipeLabUserId());
    }
}
