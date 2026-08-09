package io.github.nidhivgandhi.wordsmith.novel;

import io.github.nidhivgandhi.wordsmith.novel.dto.CreateNovelRequest;
import io.github.nidhivgandhi.wordsmith.novel.dto.NovelResponse;
import io.github.nidhivgandhi.wordsmith.novel.dto.UpdateBeatRequest;
import io.github.nidhivgandhi.wordsmith.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every method here is reachable only with a valid token (SecurityConfig's catch-all),
 * so @AuthenticationPrincipal is never null. The controller passes the caller's id down
 * and does no access checking of its own — that lives in NovelService.
 */
@RestController
@RequestMapping("/api/novels")
public class NovelController {
    private final NovelService service;

    public NovelController(NovelService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NovelResponse> create(@Valid @RequestBody CreateNovelRequest req,
                                                @AuthenticationPrincipal AuthenticatedUser user) {
        Novel novel = service.createNovel(req, user.id());
        return ResponseEntity.ok(NovelResponse.from(novel));
    }

    @GetMapping
    public List<NovelResponse> all(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.findAllOwnedBy(user.id()).stream().map(NovelResponse::from).toList();
    }

    @GetMapping("/{id}")
    public NovelResponse one(@PathVariable Long id,
                             @AuthenticationPrincipal AuthenticatedUser user) {
        return NovelResponse.from(service.findOwned(id, user.id()));
    }

    @PatchMapping("/{novelId}/beats/{beatId}")
    public NovelResponse updateBeat(@PathVariable Long novelId,
                                    @PathVariable Long beatId,
                                    @RequestBody UpdateBeatRequest req,
                                    @AuthenticationPrincipal AuthenticatedUser user) {
        service.updateBeat(novelId, beatId, req, user.id());
        return NovelResponse.from(service.findOwned(novelId, user.id()));
    }
}
