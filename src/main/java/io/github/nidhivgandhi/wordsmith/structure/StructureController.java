package io.github.nidhivgandhi.wordsmith.structure;


import io.github.nidhivgandhi.wordsmith.structure.dto.StructureResponse;
import io.github.nidhivgandhi.wordsmith.web.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/structures")
public class StructureController {
    private final StoryStructureRepository repo;
    public StructureController(StoryStructureRepository repo) { this.repo = repo; }

    @GetMapping
    public List<StructureResponse> all() {
        return repo.findAll().stream().map(StructureResponse::from).toList();
    }

    @GetMapping("/{id}")
    public StructureResponse one(@PathVariable Long id) {
        return repo.findById(id)
            .map(StructureResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("No structure found with id " + id));
    }
}