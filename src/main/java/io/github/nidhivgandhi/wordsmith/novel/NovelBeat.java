package io.github.nidhivgandhi.wordsmith.novel;
import io.github.nidhivgandhi.wordsmith.structure.StructureBeat;
import jakarta.persistence.*;

@Entity
@Table(name = "novel_beats")
public class NovelBeat {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    private Novel novel;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "structure_beat_id")
    private StructureBeat structureBeat;

    @Column(columnDefinition = "text")
    private String notes;
    private String status = "empty";

    public Long getId() { return id; }
    public Novel getNovel() { return novel; }
    public void setNovel(Novel novel) { this.novel = novel; }
    public StructureBeat getStructureBeat() { return structureBeat; }
    public void setStructureBeat(StructureBeat b) { this.structureBeat = b; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
