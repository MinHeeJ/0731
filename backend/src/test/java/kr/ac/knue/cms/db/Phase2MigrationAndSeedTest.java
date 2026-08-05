package kr.ac.knue.cms.db;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class Phase2MigrationAndSeedTest {
    @Test
    void phase2_migration_adds_only_position_config_notice_tables_and_comments() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V005__create_common_foundation_phase2_tables.sql"));
        assertThat(sql)
            .contains("CREATE TABLE IF NOT EXISTS common_position")
            .contains("CREATE TABLE IF NOT EXISTS common_config")
            .contains("CREATE TABLE IF NOT EXISTS notice")
            .contains("CREATE TABLE IF NOT EXISTS notice_attachment")
            .contains("COMMENT ON TABLE common_position")
            .contains("COMMENT ON TABLE common_config")
            .contains("COMMENT ON TABLE notice")
            .contains("COMMENT ON TABLE notice_attachment")
            .contains("REFERENCES user_account(user_id)")
            .contains("REFERENCES organization(organization_code)")
            .contains("REFERENCES role(role_code)");
    }

    @Test
    void phase2_seed_adds_three_menus_r09_permissions_defaults_and_examples() throws Exception {
        String seed = Files.readString(Path.of("src/main/resources/db/migration/V006__seed_common_foundation_phase2.sql"));
        assertThat(seed)
            .contains("보직 관리")
            .contains("공통 환경설정")
            .contains("공지사항 관리")
            .contains("'ROLE','R09'")
            .contains("SESSION_IDLE_TIMEOUT_MINUTES")
            .contains("PAGE_SIZE")
            .contains("DEFAULT_SEARCH_PERIOD_YEARS")
            .contains("BULK_QUERY_THRESHOLD_COUNT")
            .contains("LONG_TASK_NOTICE_THRESHOLD_SECONDS")
            .contains("DEPT_HEAD")
            .contains("2차 공통기능 안내");
    }
}
