package kr.ac.knue.cms.commonsetting;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommonSettingMigrationContractTest {
    @Test
    void migration_defines_global_only_table_and_five_seed_rows() throws Exception {
        String sql = Files.readString(Path.of("src/main/resources/db/migration/V005__create_system_common_setting.sql"));

        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS system_common_setting");
        assertThat(sql).contains("CHECK (scope_type = 'GLOBAL')");
        String tableDefinition = sql.substring(
            sql.indexOf("CREATE TABLE IF NOT EXISTS system_common_setting"),
            sql.indexOf("COMMENT ON TABLE system_common_setting")
        );
        assertThat(tableDefinition).doesNotContain("user_id").doesNotContain("business_category");
        for (String key : List.of(
            "sessionIdleMinutes",
            "pageSize",
            "defaultSearchPeriodDays",
            "bulkQueryThresholdCount",
            "longRunningTaskNoticeSeconds"
        )) {
            assertThat(sql).contains("'" + key + "'");
        }
        assertThat(sql).contains("COMMENT ON TABLE system_common_setting");
        assertThat(sql).contains("CREATE INDEX IF NOT EXISTS idx_system_common_setting_display_order");
    }

    @Test
    void mapper_select_orders_global_settings_by_display_order() throws Exception {
        String xml = Files.readString(Path.of("src/main/resources/mappers/CommonSettingMapper.xml"));

        assertThat(xml).contains("WHERE scope_type = 'GLOBAL'");
        assertThat(xml).contains("ORDER BY display_order, setting_key");
        assertThat(xml).contains("setting_key AS \"settingKey\"");
    }
}
