package com.kc1vmz.netlog;

import java.nio.file.Files;
import java.nio.file.Path;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.assertj.core.api.Assertions.assertThat;

class SectionLeadershipMigrationTest {

    @Test
    void existingDatabaseGainsOptionalLeadershipFieldsWithoutLosingSections() throws Exception {
        Path database = Files.createTempFile("netlog-section-leadership-", ".db");
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:sqlite:" + database);
            for (int number = 1; number <= 10; number++) {
                migrate(dataSource, "classpath:db/changelog/%02d-schema.xml".formatted(number));
            }
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            jdbc.update("INSERT INTO section (id, name, description, active) VALUES (?, ?, ?, ?)",
                    "legacy-section", "Existing section", "Existing description", true);

            migrate(dataSource, "classpath:db/liquibase-changelog.xml");
            var section = jdbc.queryForMap("SELECT * FROM section WHERE id = 'legacy-section'");
            assertThat(section).containsEntry("name", "Existing section")
                    .containsEntry("description", "Existing description")
                    .containsEntry("leader_name", null).containsEntry("leader_contact", null);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM section", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM DATABASECHANGELOG", Integer.class)).isEqualTo(11);

            jdbc.update("UPDATE section SET leader_name = ?, leader_contact = ? WHERE id = 'legacy-section'",
                    "Section leader", "leader@example.test");
            migrate(dataSource, "classpath:db/liquibase-changelog.xml");
            assertThat(jdbc.queryForObject("SELECT leader_contact FROM section WHERE id = 'legacy-section'", String.class))
                    .isEqualTo("leader@example.test");
        } finally {
            Files.deleteIfExists(database);
        }
    }

    private void migrate(DriverManagerDataSource dataSource, String changelog) throws Exception {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setResourceLoader(new DefaultResourceLoader());
        liquibase.setChangeLog(changelog);
        liquibase.afterPropertiesSet();
    }
}
