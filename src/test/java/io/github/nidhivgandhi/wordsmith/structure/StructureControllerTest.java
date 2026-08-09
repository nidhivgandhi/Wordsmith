package io.github.nidhivgandhi.wordsmith.structure;

import io.github.nidhivgandhi.wordsmith.config.SecurityConfig;
import io.github.nidhivgandhi.wordsmith.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test for StructureController.
 *
 * @WebMvcTest loads only the web layer (this controller + the global
 * exception handler + JSON serialization) - no service, no repository, no
 * database. The repository is supplied as a mock, so we control exactly what
 * the controller sees and assert how it responds.
 *
 * We @Import the real SecurityConfig so the slice enforces the same rules as
 * production. Structures are reference data and stay public, so no token is sent
 * here — that these requests still return 200 is itself part of what is tested.
 */
@WebMvcTest(StructureController.class)
@Import(SecurityConfig.class)
class StructureControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StoryStructureRepository repo;

    /**
     * @WebMvcTest includes Filter beans, which pulls in JwtAuthenticationFilter and so
     * its JwtService dependency. Nothing here uses a token; this just satisfies the graph.
     */
    @MockitoBean
    JwtService jwtService;

    /** Build a stubbed StoryStructure. The entity has no setters, so we mock its getters. */
    private static StoryStructure stubStructure(long id, String name, String slug) {
        StoryStructure s = mock(StoryStructure.class);
        when(s.getId()).thenReturn(id);
        when(s.getName()).thenReturn(name);
        when(s.getSlug()).thenReturn(slug);
        when(s.getDescription()).thenReturn("desc");
        when(s.getBeats()).thenReturn(List.of());
        return s;
    }

    @Test
    void listReturnsAllStructures() throws Exception {
        // Build the mocks BEFORE when(...): creating a mock stubs its getters, and
        // Mockito's stubbing state is global, so nesting stub-building inside another
        // when(...).thenReturn(...) corrupts it ("unfinished stubbing").
        StoryStructure threeAct = stubStructure(1L, "Three-Act Structure", "three-act");
        StoryStructure saveTheCat = stubStructure(2L, "Save the Cat", "save-the-cat");
        when(repo.findAll()).thenReturn(List.of(threeAct, saveTheCat));

        mockMvc.perform(get("/api/structures"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Three-Act Structure"))
            .andExpect(jsonPath("$[1].slug").value("save-the-cat"));
    }

    @Test
    void getByIdReturnsStructureWhenFound() throws Exception {
        StoryStructure saveTheCat = stubStructure(2L, "Save the Cat", "save-the-cat");
        when(repo.findById(2L)).thenReturn(Optional.of(saveTheCat));

        mockMvc.perform(get("/api/structures/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("Save the Cat"));
    }

    @Test
    void getByIdReturns404WithErrorBodyWhenMissing() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/structures/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.message").value("No structure found with id 99"));
    }
}
