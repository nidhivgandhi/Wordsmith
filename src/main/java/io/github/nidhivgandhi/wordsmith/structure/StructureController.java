package io.github.nidhivgandhi.wordsmith.structure;


import io.github.nidhivgandhi.wordsmith.structure.dto.StructureResponse;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<StructureResponse> one(@PathVariable Long id) {
        return repo.findById(id)
            .map(StructureResponse::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}