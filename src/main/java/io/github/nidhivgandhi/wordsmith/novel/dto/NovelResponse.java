package io.github.nidhivgandhi.wordsmith.novel.dto;

import io.github.nidhivgandhi.wordsmith.novel.Novel;
import java.util.List;

public record NovelResponse(
        Long id, String title, String premise, String structureName, List<BeatView> beats) {

    public static NovelResponse from(Novel n) {
        var beats = n.getBeats().stream()
            .map(b -> new BeatView(
                b.getId(),
                b.getStructureBeat().getName(),
                b.getStructureBeat().getDescription(),
                b.getStructureBeat().getPosition(),
                b.getNotes(),
                b.getStatus()))
            .sorted((a, c) -> Integer.compare(a.position(), c.position()))
            .toList();
        String structureName = n.getStructure() != null ? n.getStructure().getName() : null;
        return new NovelResponse(n.getId(), n.getTitle(), n.getPremise(), structureName, beats);
    }

    public record BeatView(Long id, String name, String description,
                           int position, String notes, String status) {}
}