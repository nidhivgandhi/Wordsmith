package io.github.nidhivgandhi.wordsmith.group.dto;

import jakarta.validation.constraints.*;

/**
 * Query parameters for the radius search, bound as a @ModelAttribute so the same
 * Bean Validation annotations (and the same 400-with-fieldErrors response from
 * GlobalExceptionHandler) apply to query strings as to JSON bodies.
 *
 * The radius is capped: an uncapped one lets a caller ask for "every group within
 * 20,000 miles", which is a table scan wearing a search endpoint's clothes.
 */
public record NearbySearchRequest(
        @NotNull(message = "lat is required")
        @DecimalMin(value = "-90.0", message = "lat must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "lat must be between -90 and 90")
        Double lat,

        @NotNull(message = "lon is required")
        @DecimalMin(value = "-180.0", message = "lon must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "lon must be between -180 and 180")
        Double lon,

        @NotNull(message = "radiusMiles is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "radiusMiles must be greater than 0")
        @DecimalMax(value = "500.0", message = "radiusMiles must be at most 500")
        Double radiusMiles) {}
