package io.github.nidhivgandhi.wordsmith.group;

import io.github.nidhivgandhi.wordsmith.support.PostgisContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * The geospatial query against a real PostGIS — the one thing the slice tests cannot
 * cover, since they mock the repository and ST_DWithin only exists inside the database.
 *
 * Fixtures come from the V5 migration's seed data, so these assertions also prove the
 * migration inserted its points with longitude and latitude the right way round.
 */
@SpringBootTest
@Import(PostgisContainerConfig.class)
class WritingGroupRepositoryIT {

    @Autowired
    WritingGroupRepository repo;

    // Brooklyn Writers Collective's own coordinates, from V5.
    private static final double BROOKLYN_LAT = 40.6782;
    private static final double BROOKLYN_LON = -73.9442;

    private static double miles(double m) {
        return GeoUtils.milesToMeters(m);
    }

    @Test
    void findsOnlyGroupsInsideTheRadius() {
        List<GroupDistanceView> nearby =
                repo.findWithinRadius(BROOKLYN_LAT, BROOKLYN_LON, miles(25));

        assertThat(nearby).extracting(GroupDistanceView::getName)
                .containsExactly(                       // exactly == the ordering is asserted too
                        "Brooklyn Writers Collective",  // 0.0 mi
                        "Manhattan Novel Lab",          // ~7.4 mi
                        "Jersey City Drafters")         // ~7.8 mi
                // Philadelphia (~82 mi) and San Francisco (~2,570 mi) are outside.
                .doesNotContain("Philly Speculative Fiction Guild", "Bay Area Story Structure Club");
    }

    @Test
    void wideningTheRadiusPullsInFurtherGroups() {
        List<GroupDistanceView> nearby =
                repo.findWithinRadius(BROOKLYN_LAT, BROOKLYN_LON, miles(100));

        assertThat(nearby).extracting(GroupDistanceView::getName)
                .contains("Philly Speculative Fiction Guild")
                .doesNotContain("Bay Area Story Structure Club");
    }

    @Test
    void returnsGroupsNearestFirst() {
        List<GroupDistanceView> nearby =
                repo.findWithinRadius(BROOKLYN_LAT, BROOKLYN_LON, miles(3000));

        assertThat(nearby).hasSize(5);
        assertThat(nearby).extracting(GroupDistanceView::getDistanceMeters).isSorted();
    }

    @Test
    void computesDistanceInMetersOnTheSphere() {
        GroupDistanceView philly =
                repo.findWithinRadius(BROOKLYN_LAT, BROOKLYN_LON, miles(100)).stream()
                        .filter(g -> g.getName().startsWith("Philly"))
                        .findFirst().orElseThrow();

        // Brooklyn to Philadelphia is ~81 miles great-circle. A GEOMETRY column would
        // have answered in degrees (~1.3), so this number is what proves we are doing
        // spherical math in metres.
        assertThat(GeoUtils.metersToMiles(philly.getDistanceMeters())).isCloseTo(81.6, within(1.0));
    }

    @Test
    void searchingFromElsewhereFindsTheGroupThere() {
        // San Francisco: confirms the search is genuinely coordinate-driven rather than
        // accidentally returning whatever happens to be first in the table.
        List<GroupDistanceView> nearby = repo.findWithinRadius(37.7749, -122.4194, miles(25));

        assertThat(nearby).extracting(GroupDistanceView::getName)
                .containsExactly("Bay Area Story Structure Club");
    }

    @Test
    void projectionReadsCoordinatesBackInTheRightOrder() {
        GroupDistanceView brooklyn =
                repo.findWithinRadius(BROOKLYN_LAT, BROOKLYN_LON, miles(1)).getFirst();

        // ST_Y is latitude, ST_X is longitude. Swapped, Brooklyn would report itself as
        // being at -73.9 latitude, in the Southern Ocean.
        assertThat(brooklyn.getLatitude()).isCloseTo(BROOKLYN_LAT, within(0.0001));
        assertThat(brooklyn.getLongitude()).isCloseTo(BROOKLYN_LON, within(0.0001));
        assertThat(brooklyn.getDistanceMeters()).isCloseTo(0.0, within(1.0));
    }
}
