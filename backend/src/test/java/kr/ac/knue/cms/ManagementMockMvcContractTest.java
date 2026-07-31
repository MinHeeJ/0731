package kr.ac.knue.cms;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagementMockMvcContractTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void health_returns_success_envelope_without_session() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void login_requires_credentials_and_returns_validation_errors() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").exists());
    }

    @Test
    void login_creates_session_and_change_history_side_effect() throws Exception {
        Integer before = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'session' and reason = 'login'", Integer.class);
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("KNUESESSION"))
            .andExpect(jsonPath("$.data.userId").value("admin"))
            .andExpect(jsonPath("$.data.roles[0]").value("R09"));
        Integer after = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'session' and reason = 'login'", Integer.class);
        assertThat(after).isGreaterThan(before);
    }

    @Test
    void logout_deletes_session_cookie_and_writes_change_history_side_effect() throws Exception {
        Cookie cookie = adminSessionCookie();
        Integer before = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'session' and reason = 'logout'", Integer.class);
        mvc.perform(post("/api/auth/logout").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(cookie().maxAge("KNUESESSION", 0))
            .andExpect(jsonPath("$.success").value(true));
        Integer after = jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'session' and reason = 'logout'", Integer.class);
        assertThat(after).isGreaterThan(before);
    }

    @Test
    void protected_admin_api_requires_session() throws Exception {
        mvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    void authenticated_identity_and_menu_contracts_return_portal_filtered_data() throws Exception {
        Cookie cookie = adminSessionCookie();
        mvc.perform(get("/api/me").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value("admin"))
            .andExpect(jsonPath("$.data.roles[0]").value("R09"));
        mvc.perform(get("/api/me/menus").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").exists())
            .andExpect(jsonPath("$.data[0].menuName").exists());
    }

    @Test
    void admin_read_operations_return_db_backed_arrays_with_camel_case_fields() throws Exception {
        Cookie cookie = adminSessionCookie();
        mvc.perform(get("/api/admin/code-groups").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupId").exists());
        mvc.perform(get("/api/admin/code-groups/EVAL_AREA/codes").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeValue").exists());
        mvc.perform(get("/api/admin/menu-permissions").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").exists());
        mvc.perform(get("/api/admin/menus").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").exists());
        mvc.perform(get("/api/admin/menus/tree").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuName").exists());
        mvc.perform(get("/api/admin/orgs").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].orgCode").exists());
        mvc.perform(get("/api/admin/orgs/tree").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].orgCode").exists());
        mvc.perform(get("/api/admin/roles").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].roleCode").exists());
        mvc.perform(get("/api/admin/user-roles").cookie(cookie))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].assignmentId").exists());
    }

    @Test
    void user_account_update_validates_allowed_state_and_writes_history() throws Exception {
        Cookie cookie = adminSessionCookie();
        mvc.perform(patch("/api/admin/users/professor2/account").cookie(cookie).contentType("application/json").content("{\"useYn\":\"BAD\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        Integer before = historyCount("app_user", "professor2");
        mvc.perform(patch("/api/admin/users/professor2/account").cookie(cookie).contentType("application/json").content("{\"useYn\":\"Y\",\"reason\":\"contract-account-active\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        assertThat(historyCount("app_user", "professor2")).isGreaterThan(before);
    }

    @Test
    void replace_user_business_roles_records_revoked_and_active_role_state_transition() throws Exception {
        Cookie cookie = adminSessionCookie();
        Integer before = historyCount("user_role", "professor2");
        mvc.perform(put("/api/admin/users/professor2/roles").cookie(cookie).contentType("application/json").content("{\"roleCodes\":[\"R01\"],\"reason\":\"contract-replace-roles\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R01"));
        Integer active = jdbcTemplate.queryForObject("select count(*) from user_role where user_id = 'professor2' and status = 'ACTIVE'", Integer.class);
        assertThat(active).isGreaterThanOrEqualTo(1);
        assertThat(historyCount("user_role", "professor2")).isGreaterThan(before);
    }

    @Test
    void grant_update_and_revoke_user_role_assert_state_transitions_and_history() throws Exception {
        Cookie cookie = adminSessionCookie();
        mvc.perform(post("/api/admin/user-roles").cookie(cookie).contentType("application/json").content("{\"userId\":\"professor1\",\"roleCode\":\"R03\",\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"approverId\":\"admin\",\"reason\":\"contract-grant-user-role\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").doesNotExist())
            .andExpect(jsonPath("$.data.assignmentId").exists());
        mvc.perform(patch("/api/admin/user-roles/2").cookie(cookie).contentType("application/json").content("{\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-06-30\",\"approverId\":\"admin\",\"status\":\"EXPIRED\",\"reason\":\"contract-expire-user-role\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assignmentId").value(2));
        String expiredStatus = jdbcTemplate.queryForObject("select status from user_role where assignment_id = 2", String.class);
        assertThat(expiredStatus).isEqualTo("EXPIRED");
        mvc.perform(delete("/api/admin/user-roles/2").cookie(cookie).param("reason", "contract-revoke-user-role"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("REVOKED"));
        String revokedStatus = jdbcTemplate.queryForObject("select status from user_role where assignment_id = 2", String.class);
        assertThat(revokedStatus).isEqualTo("REVOKED");
        assertThat(jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = 'user_role' and entity_id = '2'", Integer.class)).isGreaterThanOrEqualTo(2);
    }

    @Test
    void menu_permission_save_replaces_rows_and_records_change_history() throws Exception {
        Cookie cookie = adminSessionCookie();
        Integer before = historyCount("menu_permission", "ROLE:R01");
        mvc.perform(put("/api/admin/menu-permissions").cookie(cookie).contentType("application/json").content("{\"targetType\":\"ROLE\",\"targetId\":\"R01\",\"permissions\":[{\"menuId\":11,\"accessAllowed\":true}],\"reason\":\"contract-save-menu-permissions\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.targetId").value("R01"));
        Boolean allowed = jdbcTemplate.queryForObject("select access_allowed from menu_permission where target_type = 'ROLE' and target_id = 'R01' and menu_id = 11", Boolean.class);
        assertThat(allowed).isTrue();
        assertThat(historyCount("menu_permission", "ROLE:R01")).isGreaterThan(before);
    }

    @Test
    void menu_write_operations_validate_body_mutate_db_and_insert_history() throws Exception {
        Cookie cookie = adminSessionCookie();
        mvc.perform(post("/api/admin/menus").cookie(cookie).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/admin/menus").cookie(cookie).contentType("application/json").content("{\"menuName\":\"계약 메뉴\",\"screenId\":\"SCR-CONTRACT-MENU\",\"url\":\"/contract/menu\",\"reason\":\"contract-create-menu\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuId").exists());
        Long menuId = jdbcTemplate.queryForObject("select menu_id from menu where screen_id = 'SCR-CONTRACT-MENU'", Long.class);
        mvc.perform(put("/api/admin/menus/32").cookie(cookie).contentType("application/json").content("{\"menuName\":\"메뉴 정보 관리\",\"screenId\":\"SCR-MENU-INFO\",\"url\":\"/admin/menu-info\",\"displayOrder\":32,\"useYn\":\"Y\",\"menuType\":\"SCREEN\",\"reason\":\"contract-update-menu\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuId").value(32));
        mvc.perform(put("/api/admin/menus/32/structure").cookie(cookie).contentType("application/json").content("{\"parentMenuId\":30,\"displayOrder\":56,\"reason\":\"contract-menu-structure\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuId").value(32));
        mvc.perform(put("/api/admin/menus/reorder").cookie(cookie).contentType("application/json").content("{\"orders\":[{\"menuId\":11,\"displayOrder\":11},{\"menuId\":12,\"displayOrder\":12}],\"reason\":\"contract-reorder-menus\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.count").value(2));
        assertThat(historyCount("menu", String.valueOf(menuId))).isGreaterThanOrEqualTo(1);
        assertThat(historyCount("menu", "32")).isGreaterThanOrEqualTo(2);
        assertThat(historyCount("menu", "reorder")).isGreaterThanOrEqualTo(1);
    }

    @Test
    void code_group_and_detail_writes_assert_side_effect_history() throws Exception {
        Cookie cookie = adminSessionCookie();
        mvc.perform(post("/api/admin/code-groups").cookie(cookie).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
        mvc.perform(post("/api/admin/code-groups").cookie(cookie).contentType("application/json").content("{\"groupId\":\"CONTRACT_STATUS\",\"groupName\":\"계약 상태\",\"description\":\"정적 계약 테스트\",\"reason\":\"contract-create-code-group\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.groupId").value("CONTRACT_STATUS"));
        mvc.perform(put("/api/admin/code-groups/CONTRACT_STATUS").cookie(cookie).contentType("application/json").content("{\"groupName\":\"계약 상태 수정\",\"description\":\"정적 계약 테스트 수정\",\"useYn\":\"Y\",\"reason\":\"contract-update-code-group\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.groupId").value("CONTRACT_STATUS"));
        mvc.perform(post("/api/admin/code-groups/CONTRACT_STATUS/codes").cookie(cookie).contentType("application/json").content("{\"codeValue\":\"READY\",\"codeName\":\"준비\",\"validFrom\":\"2026-01-01\",\"reason\":\"contract-create-code-detail\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.codeValue").value("READY"));
        mvc.perform(put("/api/admin/code-groups/CONTRACT_STATUS/codes/READY").cookie(cookie).contentType("application/json").content("{\"codeName\":\"준비 완료\",\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"sortOrder\":20,\"useYn\":\"Y\",\"reason\":\"contract-update-code-detail\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.codeValue").value("READY"));
        assertThat(historyCount("code_group", "CONTRACT_STATUS")).isGreaterThanOrEqualTo(2);
        assertThat(historyCount("code_detail", "CONTRACT_STATUS:READY")).isGreaterThanOrEqualTo(2);
    }

    @Test
    void organization_relation_and_role_update_write_history_and_not_found_contracts() throws Exception {
        Cookie cookie = adminSessionCookie();
        mvc.perform(put("/api/admin/org-relations/999999").cookie(cookie).contentType("application/json").content("{\"parentOrgCode\":\"UNIV001\",\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"reason\":\"contract-missing-org-relation\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        Long relationId = jdbcTemplate.queryForObject("select relation_id from org_relation where org_code = 'DEPT001'", Long.class);
        assertThat(relationId).isEqualTo(3L);
        mvc.perform(put("/api/admin/org-relations/3").cookie(cookie).contentType("application/json").content("{\"parentOrgCode\":\"COL001\",\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"reason\":\"contract-update-org-relation\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.relationId").value(3));
        mvc.perform(put("/api/admin/roles/R08").cookie(cookie).contentType("application/json").content("{\"roleName\":\"점수산출 감사자\",\"purpose\":\"계약 테스트\",\"grantCriteria\":\"감사 담당자\",\"defaultDataScope\":\"전체조회\",\"useYn\":\"Y\",\"reason\":\"contract-update-role\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCode").value("R08"));
        assertThat(historyCount("org_relation", String.valueOf(relationId))).isGreaterThanOrEqualTo(1);
        assertThat(historyCount("role", "R08")).isGreaterThanOrEqualTo(1);
    }

    private Cookie adminSessionCookie() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andReturn();
        return result.getResponse().getCookie("KNUESESSION");
    }

    private Integer historyCount(String entityName, String entityId) {
        return jdbcTemplate.queryForObject("select count(*) from change_history where entity_name = ? and entity_id = ?", Integer.class, entityName, entityId);
    }
}
