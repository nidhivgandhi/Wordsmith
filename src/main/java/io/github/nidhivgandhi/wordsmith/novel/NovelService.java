package io.github.nidhivgandhi.wordsmith.novel;

import io.github.nidhivgandhi.wordsmith.novel.dto.CreateNovelRequest;
import io.github.nidhivgandhi.wordsmith.structure.StoryStructure;
import io.github.nidhivgandhi.wordsmith.structure.StoryStructureRepository;
import io.github.nidhivgandhi.wordsmith.structure.StructureBeat;
import io.github.nidhivgandhi.wordsmith.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.nidhivgandhi.wordsmith.novel.dto.UpdateBeatRequest;

@Service
public class NovelService {
    private final NovelRepository novelRepo;
    private final StoryStructureRepository structureRepo;
    private final NovelBeatRepository beatRepo;

    public NovelService(NovelRepository novelRepo, StoryStructureRepository structureRepo, NovelBeatRepository beatRepo) {
        this.novelRepo = novelRepo;
        this.structureRepo = structureRepo;
        this.beatRepo = beatRepo;
    }

    @Transactional
    public Novel createNovel(CreateNovelRequest req) {
        StoryStructure structure = structureRepo.findById(req.structureId())
            .orElseThrow(() -> new IllegalArgumentException(
                "No structure found with id " + req.structureId()));

        Novel novel = new Novel();
        novel.setTitle(req.title());
        novel.setPremise(req.premise());
        novel.setStructure(structure);

        // instantiate: copy each template beat into an editable beat for THIS novel
        for (StructureBeat templateBeat : structure.getBeats()) {
            NovelBeat nb = new NovelBeat();
            nb.setStructureBeat(templateBeat);
            novel.addBeat(nb);   // cascade saves these with the novel
        }

        return novelRepo.save(novel);
    }

    @Transactional
    public NovelBeat updateBeat(Long novelId, Long beatId, UpdateBeatRequest req) {
        NovelBeat beat = beatRepo.findById(beatId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No beat found with id " + beatId));

        if (!beat.getNovel().getId().equals(novelId)) {
            throw new IllegalArgumentException(
                "Beat with id " + beatId + " does not belong to novel with id " + novelId);
        }

        if (req.notes() != null) {
            beat.setNotes(req.notes());
        }
        if (req.status() != null) {
            beat.setStatus(req.status());
        }
        
        return beatRepo.save(beat);
    }
}