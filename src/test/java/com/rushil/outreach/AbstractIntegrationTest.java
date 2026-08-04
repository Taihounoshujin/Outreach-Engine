package com.rushil.outreach;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for tests that need a real PostgreSQL.
 *
 * <p>Testcontainers rather than H2 is a requirement here, not a preference:
 * {@code FOR UPDATE SKIP LOCKED} does not exist in H2, and the JSONB column on
 * lead does not either. A test suite running against an in-memory database would
 * pass while proving nothing about the behaviour that actually matters.
 *
 * <p>The container is {@code static} so one instance is shared across every test
 * class in the run — starting a fresh Postgres per class would add seconds to each.
 */
@SpringBootTest(properties = {
        "outreach.scheduler.enabled=false",
        "outreach.sending.provider=test",
        "outreach.sending.unsubscribe-base-url=http://localhost/u"
})
@Testcontainers
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("outreach_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}