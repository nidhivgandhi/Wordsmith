package io.github.nidhivgandhi.wordsmith;

import io.github.nidhivgandhi.wordsmith.support.PostgisContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Boots the whole application against a throwaway PostGIS container.
 *
 * This is a stronger test than it looks: it runs every Flyway migration from V1 forward
 * on an empty database, then lets Hibernate's `ddl-auto: validate` compare the resulting
 * schema against the entities. A migration that drifts from the mappings fails here.
 */
@SpringBootTest
@Import(PostgisContainerConfig.class)
class WordsmithApplicationIT {

	@Test
	void contextLoads() {
	}

}
