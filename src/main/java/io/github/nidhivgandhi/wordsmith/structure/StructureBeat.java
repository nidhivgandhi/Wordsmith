package io.github.nidhivgandhi.wordsmith.structure;

import jakarta.persistence.*;

@Entity
@Table(name = "structure_beats")
public class StructureBeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id")
    private StoryStructure structure;

    private String name;
    @Column(columnDefinition = "text")
    private String description;
    private int position;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPosition() { return position; }
}