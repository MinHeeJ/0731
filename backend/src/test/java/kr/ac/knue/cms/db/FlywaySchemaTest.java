package kr.ac.knue.cms.db;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlywaySchemaTest {
    @Test
    void schema_contains_required_tables_and_comments() throws Exception {
        String sql = String.join("\n",
            Files.readString(Path.of("src/main/resources/db/migration/V001__create_common_foundation_schema.sql")),
            Files.readString(Path.of("src/main/resources/db/migration/V004__create_contract_metadata_tables.sql")),
            Files.readString(Path.of("src/main/resources/db/migration/V006__create_cms_1401_scope_contract.sql"))
        );

        for (String table : List.of(
            "user_account",
            "korus_staff_snapshot",
            "organization",
            "role",
            "user_role",
            "menu",
            "menu_permission",
            "code_group",
            "detail_code",
            "session",
            "change_history",
            "technology_stack",
            "version_b_o_m",
            "required_outputs",
            "c_m_s_1401"
        )) {
            assertThat(sql)
                .contains("CREATE TABLE IF NOT EXISTS " + table)
                .contains("COMMENT ON TABLE " + table);
        }
        assertThat(sql).contains("created_at").contains("updated_at").contains("use_yn");
        assertThat(sql)
            .contains("CREATE INDEX IF NOT EXISTS idx_required_outputs_key")
            .contains("CREATE INDEX IF NOT EXISTS idx_technology_stack_area")
            .contains("CREATE INDEX IF NOT EXISTS idx_version_b_o_m_key")
            .contains("CREATE INDEX IF NOT EXISTS idx_c_m_s_1401_feature_code");
    }
}
