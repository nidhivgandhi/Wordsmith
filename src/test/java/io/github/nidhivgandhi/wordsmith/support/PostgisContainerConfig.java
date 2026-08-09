package io.github.nidhivgandhi.wordsmith.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts a real PostGIS database in Docker for integration tests.
 *
 * Why this exists: the geospatial query is native PostGIS SQL, so an in-memory H2 could
 * never run it. Before Testcontainers, testing it meant "hope a Postgres is running on
 * localhost" — which fails on a fresh clone and cannot work in CI at all.
 *
 * Two details do the real work:
 *
 * - The image is postgis/postgis, not postgres. asCompatibleSubstituteFor("postgres")
 *   tells Testcontainers this image behaves like the official one, which it otherwise
 *   refuses to assume.
 *
 * - @ServiceConnection wires the container's random host/port/credentials straight into
 *   Spring's DataSource. Without it you would hand-write @DynamicPropertySource plumbing
 *   for the same result.
 *
 * The container is a bean, so Spring's test context caching starts it once and shares it
 * across every test class that imports this config — not once per class.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgisContainerConfig {

    /** Pinned to the same image as docker-compose.yml, so tests and dev run identical engines. */
    private static final DockerImageName POSTGIS_IMAGE =
            DockerImageName.parse("postgis/postgis:16-3.4").asCompatibleSubstituteFor("postgres");

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgisContainer() {
        return new PostgreSQLContainer<>(POSTGIS_IMAGE);
    }
}
