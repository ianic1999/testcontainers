package com.example.testcontainers.common;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class AbstractTestContainersIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = (PostgreSQLContainer<?>) new PostgreSQLContainer(DockerImageName.parse("postgres:13.3"))
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("password");

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeAll
    public static void setUp() {
        postgres.start();
    }

}
