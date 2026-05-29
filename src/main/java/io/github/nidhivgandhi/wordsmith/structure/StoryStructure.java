package io.github.nidhivgandhi.wordsmith.structure;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "story_structures")
public class StoryStructure {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String slug;
    @Column(columnDefinition = "text")
    private String description;
    @Column(name = "is_system")
    private boolean system;

    @OneToMany(mappedBy = "structure", fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<StructureBeat> beats = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getDescription() { return description; }
    public boolean isSystem() { return system; }
    public List<StructureBeat> getBeats() { return beats; }
}