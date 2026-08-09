package io.github.nidhivgandhi.wordsmith.novel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NovelRepository extends JpaRepository<Novel, Long> {

    /**
     * Ownership is part of the lookup, not a check performed afterwards. "Find it, then
     * see if you're allowed" is one forgotten `if` away from a leak; a row that isn't
     * yours simply does not come back from this method.
     */
    Optional<Novel> findByIdAndOwnerId(Long id, Long ownerId);

    List<Novel> findAllByOwnerId(Long ownerId);
}
