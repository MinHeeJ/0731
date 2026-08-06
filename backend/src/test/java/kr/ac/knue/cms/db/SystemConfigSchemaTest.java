package kr.ac.knue.cms.db;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemConfigSchemaTest {
    @Test
    void cmn_system_config_migration_declares_required_columns_unique_key_and_comments() throws Exception {
        ClassPathResource migration = new ClassPathResource("db/migration/V007__create_system_config.sql");
        String sql = migration.getContentAsString(StandardCharsets.UTF_8);

        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS CMN_SYSTEM_CONFIG");
        for (String column : List.of(
            "config_id", "setting_key", "setting_name", "setting_value", "value_type", "unit",
            "default_value", "min_value", "max_value", "description", "use_yn",
            "created_at", "updated_at", "updated_by"
        )) {
            assertThat(sql).contains(column);
        }
        assertThat(sql).contains("UNIQUE (setting_key)");
        assertThat(sql).contains("COMMENT ON TABLE CMN_SYSTEM_CONFIG");
        assertThat(sql).contains("COMMENT ON COLUMN CMN_SYSTEM_CONFIG.use_yn IS 'Y:사용|N:미사용'");
        assertThat(sql).contains("COMMENT ON COLUMN CMN_SYSTEM_CONFIG.value_type IS 'INTEGER:정수'");
    }

    @Test
    void system_config_seed_contains_five_default_rows_menu_and_r09_permission() throws Exception {
        ClassPathResource migration = new ClassPathResource("db/migration/V008__seed_system_config.sql");
        String sql = migration.getContentAsString(StandardCharsets.UTF_8);

        assertThat(sql).contains("시스템 환경설정").contains("공통 환경설정").contains("SCR-CMN-SYSTEM-CONFIG");
        assertThat(sql).contains("'ROLE','R09','SCR-CMN-SYSTEM-CONFIG'");
        assertThat(sql).contains("SESSION_IDLE_TIMEOUT", "PAGE_SIZE", "DEFAULT_SEARCH_PERIOD", "BULK_QUERY_THRESHOLD", "LONG_TASK_NOTICE_THRESHOLD");
        assertThat(sql).contains("'30','INTEGER','분','30','5','480'");
        assertThat(sql).contains("'20','INTEGER','건','20','10','100'");
        assertThat(sql).contains("'30','INTEGER','일','30','7','365'");
        assertThat(sql).contains("'1000','INTEGER','건','1000','100','10000'");
        assertThat(sql).contains("'10','INTEGER','초','10','3','120'");
    }
}
