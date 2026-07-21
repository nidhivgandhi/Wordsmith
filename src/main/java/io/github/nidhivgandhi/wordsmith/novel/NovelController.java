package io.github.nidhivgandhi.wordsmith.novel;
import io.github.nidhivgandhi.wordsmith.novel.dto.CreateNovelRequest;
import io.github.nidhivgandhi.wordsmith.novel.dto.NovelResponse;
import io.github.nidhivgandhi.wordsmith.novel.dto.UpdateBeatRequest;
import io.github.nidhivgandhi.wordsmith.web.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/novels")
public class NovelController {
    private final NovelService service;
    private final NovelRepository repo;

    public NovelController(NovelService service, NovelRepository repo) {
        this.service = service;
        this.repo = repo;

    }

    @PostMapping
    public ResponseEntity<NovelResponse> create(@Valid @RequestBody CreateNovelRequest req) {
        Novel novel = service.createNovel(req);
        return ResponseEntity.ok(NovelResponse.from(novel));

    }

    @GetMapping
    public List<NovelResponse> all() {
        return repo.findAll().stream().map(NovelResponse::from).toList();
    }

    @GetMapping("/{id}")
    public NovelResponse one(@PathVariable Long id) {
        return repo.findById(id)
            .map(NovelResponse::from)
            .orElseThrow(() -> new ResourceNotFoundException("No novel found with id " + id));
    }

    @PatchMapping("/{novelId}/beats/{beatId}")
    public NovelResponse updateBeat(
        @PathVariable Long novelId,
        @PathVariable Long beatId,
        @RequestBody UpdateBeatRequest req) {
            service.updateBeat(novelId, beatId, req);
            return repo.findById(novelId)
                .map(NovelResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No novel found with id " + novelId));
        }

}
