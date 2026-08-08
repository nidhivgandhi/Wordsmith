package io.github.nidhivgandhi.wordsmith.novel;

import io.github.nidhivgandhi.wordsmith.config.SecurityConfig;
import io.github.nidhivgandhi.wordsmith.structure.StoryStructure;
import io.github.nidhivgandhi.wordsmith.web.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test for NovelController. The service and repository are
 * mocked; no database is involved. These tests double as coverage for the
 * global exception handler (validation -> 400, not found -> 404).
 */
@WebMvcTest(NovelController.class)
@Import(SecurityConfig.class)
class NovelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NovelService service;

    @MockitoBean
    NovelRepository repo;

    /** A stubbed Novel with no beats and one structure, enough for NovelResponse.from(). */
    private static Novel stubNovel(long id, String title) {
        StoryStructure structure = mock(StoryStructure.class);
        when(structure.getName()).thenReturn("Save the Cat");

        Novel novel = mock(Novel.class);
        when(novel.getId()).thenReturn(id);
        when(novel.getTitle()).thenReturn(title);
        when(novel.getPremise()).thenReturn("a premise");
        when(novel.getStructure()).thenReturn(structure);
        when(novel.getBeats()).thenReturn(List.of());
        return novel;
    }

    @Test
    void createReturnsNovelWhenRequestIsValid() throws Exception {
        // Build the mock before when(...): stub-building calls when() internally, and
        // nesting that inside another stubbing corrupts Mockito's global state.
        Novel novel = stubNovel(1L, "My Novel");
        when(service.createNovel(any())).thenReturn(novel);

        mockMvc.perform(post("/api/novels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"structureId\":2,\"title\":\"My Novel\",\"premise\":\"p\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("My Novel"))
            .andExpect(jsonPath("$.structureName").value("Save the Cat"));
    }

    @Test
    void createReturns400WithFieldErrorsWhenInvalid() throws Exception {
        // blank title violates @NotBlank; missing structureId violates @NotNull
        mockMvc.perform(post("/api/novels")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors.title").value("title is required"))
            .andExpect(jsonPath("$.fieldErrors.structureId").value("structureId is required"));

        // validation fails before the controller body runs, so the service is never touched
        verify(service, never()).createNovel(any());
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/novels/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No novel found with id 99"));
    }

    @Test
    void updateBeatReturnsNovelWhenSuccessful() throws Exception {
        Novel novel = stubNovel(1L, "My Novel");
        when(service.updateBeat(eq(1L), eq(5L), any())).thenReturn(null);
        when(repo.findById(1L)).thenReturn(Optional.of(novel));

        mockMvc.perform(patch("/api/novels/1/beats/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"notes\":\"opening scene\",\"status\":\"drafted\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateBeatReturns404WhenBeatMissing() throws Exception {
        when(service.updateBeat(eq(1L), eq(99L), any()))
            .thenThrow(new ResourceNotFoundException("No beat found with id 99"));

        mockMvc.perform(patch("/api/novels/1/beats/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"notes\":\"x\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No beat found with id 99"));
    }
}
