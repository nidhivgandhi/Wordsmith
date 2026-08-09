package io.github.nidhivgandhi.wordsmith.group;

import io.github.nidhivgandhi.wordsmith.config.SecurityConfig;
import io.github.nidhivgandhi.wordsmith.group.dto.NearbyGroupResponse;
import io.github.nidhivgandhi.wordsmith.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static io.github.nidhivgandhi.wordsmith.support.TestAuth.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer slice test for WritingGroupController. Service and repository are mocked,
 * so no PostGIS is involved -- these cover request binding, validation and response
 * shape. The actual ST_DWithin behaviour needs a real database and is covered when
 * Testcontainers lands (see ROADMAP).
 */
@WebMvcTest(WritingGroupController.class)
@Import(SecurityConfig.class)
class WritingGroupControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WritingGroupService service;

    @MockitoBean
    WritingGroupRepository repo;

    /**
     * Needed because @WebMvcTest includes Filter beans, which pulls in
     * JwtAuthenticationFilter and therefore its JwtService dependency. Requests here
     * authenticate via TestAuth rather than a real token.
     */
    @MockitoBean
    JwtService jwtService;

    private static WritingGroup stubGroup(String name, double lat, double lon) {
        WritingGroup g = new WritingGroup();
        g.setName(name);
        g.setCity("Brooklyn, NY");
        g.setMeetingFormat("in_person");
        g.setLocation(GeoUtils.point(lat, lon));
        return g;
    }

    @Test
    void createReturnsGroupWithCoordinatesEchoedBack() throws Exception {
        when(service.createGroup(any(), eq(7L)))
                .thenReturn(stubGroup("Brooklyn Writers Collective", 40.6782, -73.9442));

        mockMvc.perform(post("/api/groups").with(user(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Brooklyn Writers Collective","city":"Brooklyn, NY",
                                 "meetingFormat":"in_person","latitude":40.6782,"longitude":-73.9442}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Brooklyn Writers Collective"))
                // If the entity->response mapping ever swapped X and Y, these two flip.
                .andExpect(jsonPath("$.latitude").value(40.6782))
                .andExpect(jsonPath("$.longitude").value(-73.9442));
    }

    @Test
    void createReturns400WhenCoordinatesOutOfRange() throws Exception {
        mockMvc.perform(post("/api/groups").with(user(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nowhere\",\"latitude\":95.0,\"longitude\":-200.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.latitude").value("latitude must be between -90 and 90"))
                .andExpect(jsonPath("$.fieldErrors.longitude").value("longitude must be between -180 and 180"));

        verify(service, never()).createGroup(any(), any());
    }

    @Test
    void createReturns400WhenMeetingFormatUnknown() throws Exception {
        mockMvc.perform(post("/api/groups").with(user(7L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Group","meetingFormat":"telepathic",
                                 "latitude":40.0,"longitude":-73.0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.meetingFormat")
                        .value("meetingFormat must be one of: in_person, online, hybrid"));
    }

    @Test
    void searchBindsQueryParamsAndReturnsGroupsNearestFirst() throws Exception {
        when(service.findNearby(any())).thenReturn(List.of(
                new NearbyGroupResponse(1L, "Brooklyn Writers Collective", null,
                        "Brooklyn, NY", "in_person", 40.6782, -73.9442, 0.0),
                new NearbyGroupResponse(2L, "Manhattan Novel Lab", null,
                        "New York, NY", "in_person", 40.7831, -73.9712, 7.3)));

        mockMvc.perform(get("/api/groups/search")
                        .param("lat", "40.6782")
                        .param("lon", "-73.9442")
                        .param("radiusMiles", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Brooklyn Writers Collective"))
                .andExpect(jsonPath("$[1].distanceMiles").value(7.3));

        // The query string must reach the service intact -- a mis-bound lat/lon would
        // still return 200 while searching the wrong place.
        var captor = forClass(io.github.nidhivgandhi.wordsmith.group.dto.NearbySearchRequest.class);
        verify(service).findNearby(captor.capture());
        assertThat(captor.getValue().lat()).isEqualTo(40.6782);
        assertThat(captor.getValue().lon()).isEqualTo(-73.9442);
        assertThat(captor.getValue().radiusMiles()).isEqualTo(25.0);
    }

    @Test
    void searchReturns400WhenRadiusExceedsCap() throws Exception {
        mockMvc.perform(get("/api/groups/search")
                        .param("lat", "40.6782")
                        .param("lon", "-73.9442")
                        .param("radiusMiles", "5000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.radiusMiles").value("radiusMiles must be at most 500"));

        verify(service, never()).findNearby(any());
    }

    @Test
    void searchReturns400WhenRadiusMissing() throws Exception {
        mockMvc.perform(get("/api/groups/search")
                        .param("lat", "40.6782")
                        .param("lon", "-73.9442"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.radiusMiles").value("radiusMiles is required"));
    }

    /**
     * Discovery is public by design — you can find a writing group before you have an
     * account. This test exists so that stays a decision rather than an accident: it
     * fails loudly if the security rules ever close these off.
     */
    @Test
    void searchAndReadStayPublicButCreatingRequiresAToken() throws Exception {
        when(service.findNearby(any())).thenReturn(List.of());
        when(repo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/groups/search")
                        .param("lat", "40.6782").param("lon", "-73.9442").param("radiusMiles", "25"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/groups")).andExpect(status().isOk());

        // ...but writing does not.
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"G\",\"latitude\":40.0,\"longitude\":-73.0}"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).createGroup(any(), any());
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/groups/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No group found with id 99"));
    }
}
