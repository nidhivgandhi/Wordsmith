package io.github.nidhivgandhi.wordsmith.novel;
import io.github.nidhivgandhi.wordsmith.structure.StoryStructure;
import io.github.nidhivgandhi.wordsmith.user.User;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "novels")
public class Novel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "structure_id")
    private StoryStructure structure;

    /** Never null — the database enforces it (V6). LAZY: ownership checks compare ids. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String premise;
    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "novel", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<NovelBeat> beats = new ArrayList<>();

    public void addBeat(NovelBeat beat) {
        beats.add(beat);
        beat.setNovel(this);
    }

    // returns id
    public Long getId() { return id; }

    // get and set methods for owner
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    // get and set methods for structure
    public StoryStructure getStructure() { return structure; }
    public void setStructure(StoryStructure structure) { this.structure = structure; }
    
    // get and set methods for title
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    // get and set methods for premise
    public String getPremise() { return premise; }
    public void setPremise(String premise) { this.premise = premise; }
    
    // get method for beats
    public List<NovelBeat> getBeats() { return beats; }
}
