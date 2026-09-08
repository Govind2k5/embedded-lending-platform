package com.lendingplatform.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Spins up real Postgres and Redis containers so integration tests exercise
 * the same stack the app runs against in production - no H2 or embedded-Redis
 * shortcuts that could hide dialect or serialization differences.
 *
 * Containers are started once for the whole test run (the "singleton
 * containers" pattern) instead of per test class: they are plain static
 * fields, not @Container-annotated, so JUnit's Testcontainers extension
 * never stops them between test classes.
 *
 * Why not @Container? An earlier version of this class used
 * `@Container static` fields, which JUnit 5's Testcontainers extension
 * stops in afterAll() for EVERY subclass - even ones sharing the same
 * static instance. That meant the first integration test class to finish
 * killed the containers out from under any test class that ran after it
 * (surfacing as "Redis command timed out" / "Connection refused" failures
 * that only showed up when running the full suite, not any single class in
 * isolation). Starting the containers manually in a static initializer,
 * with no @Container annotation, keeps them alive for the whole JVM run.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("lending_platform_test")
            .withUsername("test")
            .withPassword("test");

    // A plain, unconfigured Redis container - no special image needed since
    // this app only uses basic string/value commands (GET/SET/INCR/EXPIRE/DEL).
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    /**
     * Points the Spring context's datasource/redis config at whatever
     * random host ports Testcontainers assigned the two containers above -
     * these override application.yml's localhost:5432/6379 defaults for
     * the duration of the test JVM.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    // Drives real HTTP requests against the randomly-assigned port the test
    // server started on - exercises the full stack (interceptors, Jackson
    // serialization, validation, the real controllers) rather than calling
    // service methods directly.
    @Autowired
    protected TestRestTemplate restTemplate;
}
