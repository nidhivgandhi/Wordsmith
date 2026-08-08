package io.github.nidhivgandhi.wordsmith.group;

/**
 * A Spring Data interface projection: the radius query returns rows shaped like this
 * rather than full entities, so the computed distance can ride along with each row
 * instead of being recomputed in Java.
 *
 * Each getter binds to the matching column alias in the native query.
 */
public interface GroupDistanceView {
    Long getId();
    String getName();
    String getDescription();
    String getCity();
    String getMeetingFormat();
    Double getLatitude();
    Double getLongitude();
    Double getDistanceMeters();
}
