package io.github.nidhivgandhi.wordsmith.group;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;

@Entity
@Table(name = "writing_groups")
public class WritingGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String city;

    @Column(name = "meeting_format")
    private String meetingFormat = "in_person";

    /**
     * hibernate-spatial maps JTS geometries to PostGIS columns. Left alone it would
     * pick GEOMETRY; @JdbcTypeCode(GEOGRAPHY) tells it this column is GEOGRAPHY, so
     * the type matches the migration and `ddl-auto: validate` stays happy.
     */
    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(name = "location", columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getMeetingFormat() { return meetingFormat; }
    public void setMeetingFormat(String meetingFormat) { this.meetingFormat = meetingFormat; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
}
