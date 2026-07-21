package io.github.nidhivgandhi.wordsmith.novel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNovelRequest(
        @NotNull(message = "structureId is required") Long structureId,
        @NotBlank(message = "title is required") String title,
        String premise) {}
