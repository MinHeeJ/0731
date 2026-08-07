package kr.ac.knue.cms.commonsetting;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class CommonSettingSchemaContractTest {
    @Test
    void migration_defines_common_setting_only_without_override_or_strategy_tables() throws Exception {
        String sql = new ClassPathResource("db/migration/V005__create_common_setting.sql")
            .getContentAsString(StandardCharsets.UTF_8);

        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS common_setting");
        assertThat(sql).contains("setting_key varchar(80) PRIMARY KEY");
        assertThat(sql).contains("setting_value varchar(200) NOT NULL");
        assertThat(sql).contains("COMMENT ON TABLE common_setting");
        assertThat(sql).contains("sessionIdleTime", "pageSize", "defaultSearchPeriod", "bulkQueryThreshold", "longRunningTaskNoticeThreshold");
        assertThat(sql).doesNotContain("common_setting_history");
        assertThat(sql).doesNotContain("user_id varchar", "business_id varchar", "override_value");
        assertThat(sql).doesNotContain("worker", "queue", "batch_state", "token_policy", "session_storage");
    }
}
