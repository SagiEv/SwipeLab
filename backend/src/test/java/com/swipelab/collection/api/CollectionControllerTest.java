package com.swipelab.collection.api;

import com.swipelab.collection.application.CollectionService;
import com.swipelab.collection.dto.CollectionEntryResponse;
import com.swipelab.collection.dto.CollectionStatsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CollectionService collectionService;

    @InjectMocks
    private CollectionController collectionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(collectionController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        UserDetails userDetails = new User("alice", "password",
                Collections.singletonList(new SimpleGrantedAuthority("USER")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ── Happy flow ────────────────────────────────────────────────────────────

    @Test
    void getMyCollection_ShouldReturnCollectionEntries() throws Exception {
        CollectionEntryResponse entry = CollectionEntryResponse.builder()
                .id(1L).imageId(42L).species("Bumblebee")
                .imageUrl("https://example.com/img.jpg").taskId(7L)
                .taggedAt(LocalDateTime.now())
                .build();

        when(collectionService.getCollection("alice")).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/collection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].species").value("Bumblebee"))
                .andExpect(jsonPath("$[0].imageId").value(42));
    }

    @Test
    void getMyCollectionStats_ShouldReturnTotal() throws Exception {
        when(collectionService.getStats("alice"))
                .thenReturn(CollectionStatsResponse.builder().total(7L).build());

        mockMvc.perform(get("/api/v1/collection/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(7));
    }

    // ── Edge case ─────────────────────────────────────────────────────────────

    @Test
    void getMyCollection_ShouldReturnEmptyList_WhenNoEntries() throws Exception {
        when(collectionService.getCollection("alice")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/collection"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
