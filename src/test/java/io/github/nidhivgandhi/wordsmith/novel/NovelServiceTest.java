package io.github.nidhivgandhi.wordsmith.novel;

import io.github.nidhivgandhi.wordsmith.novel.dto.UpdateBeatRequest;
import io.github.nidhivgandhi.wordsmith.structure.StoryStructureRepository;
import io.github.nidhivgandhi.wordsmith.user.UserRepository;
import io.github.nidhivgandhi.wordsmith.web.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Ownership rules, tested at the layer that enforces them.
 *
 * The controller slice test can only show that the caller's id is passed down; these
 * show what happens to it — that a novel belonging to someone else is indistinguishable
 * from one that does not exist.
 */
class NovelServiceTest {

    private NovelRepository novelRepo;
    private NovelBeatRepository beatRepo;
    private NovelService service;

    private static final Long OWNER = 7L;
    private static final Long INTRUDER = 8L;

    @BeforeEach
    void setUp() {
        novelRepo = mock(NovelRepository.class);
        beatRepo = mock(NovelBeatRepository.class);
        service = new NovelService(novelRepo, mock(StoryStructureRepository.class),
                beatRepo, mock(UserRepository.class));
    }

    @Test
    void findOwnedReturns404ForAnotherUsersNovel() {
        // The repository query itself is scoped by owner, so someone else's novel simply
        // does not come back.
        when(novelRepo.findByIdAndOwnerId(1L, INTRUDER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOwned(1L, INTRUDER))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No novel found with id 1");
    }

    @Test
    void missingNovelAndSomeoneElsesNovelFailIdentically() {
        when(novelRepo.findByIdAndOwnerId(1L, INTRUDER)).thenReturn(Optional.empty());   // exists, not theirs
        when(novelRepo.findByIdAndOwnerId(404L, INTRUDER)).thenReturn(Optional.empty()); // does not exist

        var notYours = org.assertj.core.api.Assertions.catchThrowable(
                () -> service.findOwned(1L, INTRUDER));
        var notThere = org.assertj.core.api.Assertions.catchThrowable(
                () -> service.findOwned(404L, INTRUDER));

        // Same type, same shape of message. A 403 here would confirm that novel 1 exists,
        // which is enough to map out every id in the database.
        assertThatThrownBy(() -> { throw notYours; }).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> { throw notThere; }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateBeatRefusesWhenTheParentNovelIsNotYours() {
        when(novelRepo.findByIdAndOwnerId(1L, INTRUDER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBeat(1L, 5L, new UpdateBeatRequest("notes", null), INTRUDER))
                .isInstanceOf(ResourceNotFoundException.class);

        // The ownership check happens before the beat is even loaded, so knowing a beat id
        // gets an attacker nowhere.
        verify(beatRepo, never()).findById(any());
        verify(beatRepo, never()).save(any());
    }

    @Test
    void updateBeatLoadsTheBeatOnlyForTheOwner() {
        Novel novel = mock(Novel.class);
        when(novel.getId()).thenReturn(1L);
        when(novelRepo.findByIdAndOwnerId(1L, OWNER)).thenReturn(Optional.of(novel));

        NovelBeat beat = new NovelBeat();
        beat.setNovel(novel);
        when(beatRepo.findById(5L)).thenReturn(Optional.of(beat));
        when(beatRepo.save(any(NovelBeat.class))).thenAnswer(inv -> inv.getArgument(0));

        service.updateBeat(1L, 5L, new UpdateBeatRequest("opening scene", "drafted"), OWNER);

        verify(beatRepo).save(beat);
    }
}
