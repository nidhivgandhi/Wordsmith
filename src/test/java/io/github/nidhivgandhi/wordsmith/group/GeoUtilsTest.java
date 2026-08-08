package io.github.nidhivgandhi.wordsmith.group;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the two conversions that fail silently rather than loudly: the lat/lon ->
 * (x, y) swap, and miles <-> metres. Neither needs a database.
 */
class GeoUtilsTest {

    @Test
    void pointPutsLongitudeInXAndLatitudeInY() {
        // Brooklyn: 40.68 N, 73.94 W. Swapping these lands you in the Arabian Sea,
        // which is a perfectly valid point -- hence the test.
        Point p = GeoUtils.point(40.6782, -73.9442);

        assertThat(p.getX()).isEqualTo(-73.9442);   // longitude
        assertThat(p.getY()).isEqualTo(40.6782);    // latitude
    }

    @Test
    void pointCarriesWgs84Srid() {
        // Without SRID 4326 PostGIS rejects the insert into a GEOGRAPHY(POINT, 4326)
        // column, so this is what keeps writes working.
        assertThat(GeoUtils.point(40.6782, -73.9442).getSRID()).isEqualTo(4326);
    }

    @Test
    void milesAndMetersRoundTrip() {
        assertThat(GeoUtils.milesToMeters(1)).isEqualTo(1609.344);
        assertThat(GeoUtils.metersToMiles(1609.344)).isEqualTo(1.0);
        assertThat(GeoUtils.metersToMiles(GeoUtils.milesToMeters(25))).isCloseTo(25.0, org.assertj.core.data.Offset.offset(1e-9));
    }
}
