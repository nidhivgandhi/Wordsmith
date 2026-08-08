package io.github.nidhivgandhi.wordsmith.group.dto;

import jakarta.validation.constraints.*;

public record CreateGroupRequest(
        @NotBlank(message = "name is required")
        @Size(max = 160, message = "name must be at most 160 characters")
        String name,

        String description,

        @Size(max = 120, message = "city must be at most 120 characters")
        String city,

        @Pattern(regexp = "in_person|online|hybrid",
                 message = "meetingFormat must be one of: in_person, online, hybrid")
        String meetingFormat,

        @NotNull(message = "latitude is required")
        @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "latitude must be between -90 and 90")
        Double latitude,

        @NotNull(message = "longitude is required")
        @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "longitude must be between -180 and 180")
        Double longitude) {}
