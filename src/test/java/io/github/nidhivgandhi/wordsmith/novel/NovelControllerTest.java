package io.github.nidhivgandhi.wordsmith.novel;

import io.github.nidhivgandhi.wordsmith.config.SecurityConfig;
import io.github.nidhivgandhi.wordsmith.security.JwtService;
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

import static io.github.nidhivgandhi.wordsmith.support.TestAuth.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test for NovelController. The service is mocked; no database is
 * involved. These tests double as coverage for the global exception handler
 * (validation -> 400, not found -> 404) and for the security rules (no token -> 401).
 *
 * JwtService is mocked because @WebMvcTest picks up Filter beans, which pulls in
 * JwtAuthenticationFilter; the token itself is never exercised here, since requests
 * authenticate via TestAuth instead.
 */
@WebMvcTest(NovelController.class)
@Import(SecurityConfig.class)
class NovelControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NovelService service;

    @MockitoBean
    JwtService jwtService;

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
        when(service.createNovel(any(), eq(7L))).thenReturn(novel);

        mockMvc.perform(post("/api/novels").with(user(7L))
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
        mockMvc.perform(post("/api/novels").with(user(7L))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.fieldErrors.title").value("title is required"))
            .andExpect(jsonPath("$.fieldErrors.structureId").value("structureId is required"));

        // validation fails before the controller body runs, so the service is never touched
        verify(service, never()).createNovel(any(), any());
    }

    @Test
    void getByIdReturns404WhenMissingOrNotOwned() throws Exception {
        // The service makes no distinction between the two, and neither does this test —
        // that indistinguishability is the point.
        when(service.findOwned(99L, 7L))
            .thenThrow(new ResourceNotFoundException("No novel found with id 99"));

        mockMvc.perform(get("/api/novels/99").with(user(7L)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No novel found with id 99"));
    }

    @Test
    void listPassesTheCallersIdSoUsersOnlySeeTheirOwnNovels() throws Exception {
        // Again: build the stub before when(...), never nested inside it.
        Novel mine = stubNovel(1L, "Mine");
        when(service.findAllOwnedBy(7L)).thenReturn(List.of(mine));

        mockMvc.perform(get("/api/novels").with(user(7L)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Mine"));

        verify(service).findAllOwnedBy(7L);
    }

    @Test
    void updateBeatReturnsNovelWhenSuccessful() throws Exception {
        Novel novel = stubNovel(1L, "My Novel");
        when(service.updateBeat(eq(1L), eq(5L), any(), eq(7L))).thenReturn(null);
        when(service.findOwned(1L, 7L)).thenReturn(novel);

        mockMvc.perform(patch("/api/novels/1/beats/5").with(user(7L))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"notes\":\"opening scene\",\"status\":\"drafted\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateBeatReturns404WhenBeatMissing() throws Exception {
        when(service.updateBeat(eq(1L), eq(99L), any(), eq(7L)))
            .thenThrow(new ResourceNotFoundException("No beat found with id 99"));

        mockMvc.perform(patch("/api/novels/1/beats/99").with(user(7L))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"notes\":\"x\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("No beat found with id 99"));
    }

    @Test
    void everyNovelEndpointRequiresAToken() throws Exception {
        // No .with(user(...)): these should never reach the controller at all.
        mockMvc.perform(get("/api/novels")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/novels/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/novels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"structureId\":2,\"title\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/novels/1/beats/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"x\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void unauthorizedResponseUsesTheStandardErrorShape() throws Exception {
        mockMvc.perform(get("/api/novels"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Unauthorized"))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}
