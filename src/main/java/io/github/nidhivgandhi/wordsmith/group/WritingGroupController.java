package io.github.nidhivgandhi.wordsmith.group;

import io.github.nidhivgandhi.wordsmith.group.dto.CreateGroupRequest;
import io.github.nidhivgandhi.wordsmith.group.dto.GroupResponse;
import io.github.nidhivgandhi.wordsmith.group.dto.NearbyGroupResponse;
import io.github.nidhivgandhi.wordsmith.group.dto.NearbySearchRequest;
import io.github.nidhivgandhi.wordsmith.web.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class WritingGroupController {

    private final WritingGroupService service;
    private final WritingGroupRepository repo;

    public WritingGroupController(WritingGroupService service, WritingGroupRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest req) {
        WritingGroup group = service.createGroup(req);
        return ResponseEntity.ok(GroupResponse.from(group));
    }

    @GetMapping
    public List<GroupResponse> all() {
        return repo.findAll().stream().map(GroupResponse::from).toList();
    }

    /**
     * GET /api/groups/search?lat=40.68&lon=-73.94&radiusMiles=25
     *
     * @ModelAttribute binds the query string into the record, and @Valid runs the same
     * Bean Validation the JSON endpoints use -- so a bad radius comes back as the usual
     * 400 with fieldErrors rather than an ad-hoc error shape.
     */
    @GetMapping("/search")
    public List<NearbyGroupResponse> search(@Valid @ModelAttribute NearbySearchRequest req) {
        return service.findNearby(req);
    }

    @GetMapping("/{id}")
    public GroupResponse one(@PathVariable Long id) {
        return repo.findById(id)
                .map(GroupResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No group found with id " + id));
    }
}
