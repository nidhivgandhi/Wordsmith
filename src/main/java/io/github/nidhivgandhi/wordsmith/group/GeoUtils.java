package io.github.nidhivgandhi.wordsmith.group;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * The single place where latitude/longitude becomes a geometry, and the single
 * place where miles become metres.
 *
 * Both conversions are famous sources of silent bugs:
 *
 * 1. JTS and PostGIS store a point as (x, y), and for lat/lon that means
 *    (longitude, latitude) -- the reverse of how humans say it. Swapping them
 *    produces a valid point in the wrong hemisphere rather than an error, so the
 *    swap is confined to {@link #point(double, double)} and done exactly once.
 *
 * 2. PostGIS GEOGRAPHY answers in metres; our API speaks miles. Converting at
 *    one boundary keeps every distance below the service layer metric.
 */
public final class GeoUtils {

    /** WGS84 -- the lat/lon coordinate system used by GPS and mapping APIs. */
    public static final int SRID_WGS84 = 4326;

    /** International mile, exactly. */
    public static final double METERS_PER_MILE = 1609.344;

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    private GeoUtils() {}

    /**
     * Builds a WGS84 point from human-ordered coordinates.
     *
     * @param latitude  degrees north, -90..90
     * @param longitude degrees east, -180..180
     */
    public static Point point(double latitude, double longitude) {
        // (x, y) == (longitude, latitude). This line is the whole reason this class exists.
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public static double milesToMeters(double miles) {
        return miles * METERS_PER_MILE;
    }

    public static double metersToMiles(double meters) {
        return meters / METERS_PER_MILE;
    }
}
