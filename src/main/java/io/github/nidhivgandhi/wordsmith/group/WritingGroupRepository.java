package io.github.nidhivgandhi.wordsmith.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WritingGroupRepository extends JpaRepository<WritingGroup, Long> {

    /**
     * Groups whose meeting point lies within {@code radiusMeters} of the origin,
     * nearest first, each row carrying its own distance.
     *
     * Why the query looks like this:
     *
     * - ST_DWithin does the filtering, not `WHERE ST_Distance(...) < x`. Both give the
     *   same answer, but ST_DWithin can use the GiST index on `location` to throw away
     *   most rows on bounding boxes alone. A ST_Distance predicate has to compute an
     *   exact spherical distance for every row in the table before it can compare.
     *
     * - ST_Distance still appears in the SELECT, but there it only runs for rows that
     *   already survived the filter -- and we need the number anyway, to sort and to
     *   show the user "4.2 miles away".
     *
     * - The origin is built in SQL from two doubles instead of bound as a JTS Point.
     *   Binding a geometry parameter into a *native* query needs Hibernate type hints
     *   and fails in confusing ways; ST_MakePoint is explicit and portable across
     *   Postgres drivers. Note its argument order: (longitude, latitude).
     *
     * - CAST(... AS geography), not the shorter `::geography`. A doubled colon can be
     *   misread as a named parameter by JPA's query parser, and CAST is standard SQL.
     *
     * - ST_X / ST_Y read the coordinates back out for the response. They are defined on
     *   geometry rather than geography, hence the cast; the cast is free, since both
     *   types share the same internal representation.
     */
    @Query(value = """
            SELECT g.id                                   AS "id",
                   g.name                                 AS "name",
                   g.description                          AS "description",
                   g.city                                 AS "city",
                   g.meeting_format                       AS "meetingFormat",
                   ST_Y(CAST(g.location AS geometry))     AS "latitude",
                   ST_X(CAST(g.location AS geometry))     AS "longitude",
                   ST_Distance(
                       g.location,
                       CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
                   )                                      AS "distanceMeters"
            FROM writing_groups g
            WHERE ST_DWithin(
                      g.location,
                      CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
                      :radiusMeters
                  )
            ORDER BY "distanceMeters"
            """, nativeQuery = true)
    List<GroupDistanceView> findWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters);
}
