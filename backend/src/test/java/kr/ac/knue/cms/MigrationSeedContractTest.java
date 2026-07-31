package kr.ac.knue.cms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MigrationSeedContractTest {
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void flyway_schema_contains_required_foundation_tables() {
        List<String> tables = jdbcTemplate.queryForList("""
            select table_name from information_schema.tables
            where table_schema = 'public'
            """, String.class);

        assertThat(tables).contains(
            "app_user",
            "korus_personnel_snapshot",
            "org",
            "role",
            "user_role",
            "menu",
            "menu_permission",
            "code_group",
            "code_detail",
            "session",
            "change_history"
        );
    }

    @Test
    void seed_data_contains_roles_admin_account_menus_and_r09_permissions() {
        Integer roleCount = jdbcTemplate.queryForObject("select count(*) from role where role_code between 'R01' and 'R09'", Integer.class);
        Integer adminCount = jdbcTemplate.queryForObject("select count(*) from app_user u join user_role r on r.user_id = u.user_id where u.user_id = 'admin' and r.role_code = 'R09' and r.status = 'ACTIVE'", Integer.class);
        Integer screenMenuCount = jdbcTemplate.queryForObject("select count(*) from menu where menu_type = 'SCREEN' and use_yn = 'Y'", Integer.class);
        Integer r09PermissionCount = jdbcTemplate.queryForObject("select count(*) from menu_permission where target_type = 'ROLE' and target_id = 'R09' and access_allowed = true", Integer.class);

        assertThat(roleCount).isEqualTo(9);
        assertThat(adminCount).isEqualTo(1);
        assertThat(screenMenuCount).isGreaterThanOrEqualTo(9);
        assertThat(r09PermissionCount).isGreaterThanOrEqualTo(9);
    }
}
