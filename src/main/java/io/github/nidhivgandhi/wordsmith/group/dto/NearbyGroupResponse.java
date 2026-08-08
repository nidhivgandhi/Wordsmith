package io.github.nidhivgandhi.wordsmith.group.dto;

import io.github.nidhivgandhi.wordsmith.group.GeoUtils;
import io.github.nidhivgandhi.wordsmith.group.GroupDistanceView;

public record NearbyGroupResponse(
        Long id, String name, String description, String city,
        String meetingFormat, double latitude, double longitude, double distanceMiles) {

    public static NearbyGroupResponse from(GroupDistanceView v) {
        // PostGIS answered in metres; the API speaks miles. One rounded decimal is as
        // much precision as "how far away is it" ever needs.
        double miles = GeoUtils.metersToMiles(v.getDistanceMeters());
        return new NearbyGroupResponse(
                v.getId(), v.getName(), v.getDescription(), v.getCity(), v.getMeetingFormat(),
                v.getLatitude(), v.getLongitude(), Math.round(miles * 10.0) / 10.0);
    }
}
