package io.github.nidhivgandhi.wordsmith.structure.dto;


import io.github.nidhivgandhi.wordsmith.structure.StoryStructure;
import java.util.List;

public record StructureResponse(
        Long id, String name, String slug, String description, List<BeatResponse> beats) {

    public static StructureResponse from(StoryStructure s) {
        var beats = s.getBeats().stream()
            .map(b -> new BeatResponse(b.getId(), b.getName(), b.getDescription(), b.getPosition()))
            .toList();
        return new StructureResponse(s.getId(), s.getName(), s.getSlug(), s.getDescription(), beats);
    }

    public record BeatResponse(Long id, String name, String description, int position) {}
}