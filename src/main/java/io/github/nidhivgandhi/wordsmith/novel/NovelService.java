package io.github.nidhivgandhi.wordsmith.novel;

import io.github.nidhivgandhi.wordsmith.novel.dto.CreateNovelRequest;
import io.github.nidhivgandhi.wordsmith.novel.dto.UpdateBeatRequest;
import io.github.nidhivgandhi.wordsmith.structure.StoryStructure;
import io.github.nidhivgandhi.wordsmith.structure.StoryStructureRepository;
import io.github.nidhivgandhi.wordsmith.structure.StructureBeat;
import io.github.nidhivgandhi.wordsmith.user.User;
import io.github.nidhivgandhi.wordsmith.user.UserRepository;
import io.github.nidhivgandhi.wordsmith.web.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Every method takes the caller's id and scopes its work to that user.
 *
 * The check lives here rather than in the controller on purpose: a controller check
 * protects one endpoint, a service check protects every caller of the service —
 * including the endpoint somebody adds next month without reading this file.
 */
@Service
public class NovelService {
    private final NovelRepository novelRepo;
    private final StoryStructureRepository structureRepo;
    private final NovelBeatRepository beatRepo;
    private final UserRepository userRepo;

    public NovelService(NovelRepository novelRepo, StoryStructureRepository structureRepo,
                        NovelBeatRepository beatRepo, UserRepository userRepo) {
        this.novelRepo = novelRepo;
        this.structureRepo = structureRepo;
        this.beatRepo = beatRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Novel createNovel(CreateNovelRequest req, Long ownerId) {
        StoryStructure structure = structureRepo.findById(req.structureId())
            .orElseThrow(() -> new IllegalArgumentException(
                "No structure found with id " + req.structureId()));

        // getReferenceById, not findById: we only need the foreign key value, and a
        // reference gives us that without a SELECT. The token already proved this id exists.
        User owner = userRepo.getReferenceById(ownerId);

        Novel novel = new Novel();
        novel.setTitle(req.title());
        novel.setPremise(req.premise());
        novel.setStructure(structure);
        novel.setOwner(owner);

        // instantiate: copy each template beat into an editable beat for THIS novel
        for (StructureBeat templateBeat : structure.getBeats()) {
            NovelBeat nb = new NovelBeat();
            nb.setStructureBeat(templateBeat);
            novel.addBeat(nb);   // cascade saves these with the novel
        }

        return novelRepo.save(novel);
    }

    @Transactional(readOnly = true)
    public List<Novel> findAllOwnedBy(Long ownerId) {
        return novelRepo.findAllByOwnerId(ownerId);
    }

    /**
     * 404, not 403, when the novel belongs to someone else.
     *
     * 403 would confirm that a novel with that id exists, letting anyone map out which
     * ids are real by walking the range. 404 makes "not yours" and "not there"
     * indistinguishable from outside, which is exactly what we want to reveal: nothing.
     */
    @Transactional(readOnly = true)
    public Novel findOwned(Long novelId, Long ownerId) {
        return novelRepo.findByIdAndOwnerId(novelId, ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("No novel found with id " + novelId));
    }

    @Transactional
    public NovelBeat updateBeat(Long novelId, Long beatId, UpdateBeatRequest req, Long ownerId) {
        // Ownership of the parent novel is established first: without this, knowing any
        // beat id would be enough to edit a stranger's outline.
        Novel novel = findOwned(novelId, ownerId);

        NovelBeat beat = beatRepo.findById(beatId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No beat found with id " + beatId));

        if (!beat.getNovel().getId().equals(novel.getId())) {
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
