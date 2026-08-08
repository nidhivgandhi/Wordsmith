package io.github.nidhivgandhi.wordsmith.group.dto;

import io.github.nidhivgandhi.wordsmith.group.WritingGroup;

public record GroupResponse(
        Long id, String name, String description, String city,
        String meetingFormat, double latitude, double longitude) {

    public static GroupResponse from(WritingGroup g) {
        // Reading back out of JTS, so unswap: getY() is latitude, getX() is longitude.
        return new GroupResponse(
                g.getId(), g.getName(), g.getDescription(), g.getCity(), g.getMeetingFormat(),
                g.getLocation().getY(), g.getLocation().getX());
    }
}
