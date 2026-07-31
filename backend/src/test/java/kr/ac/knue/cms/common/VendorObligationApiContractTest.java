package kr.ac.knue.cms.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VendorObligationApiContractTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/auth/login enforces happy auth validation business and side-effect contract")
    void login_requires_valid_credentials_and_creates_session_cookie() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"loginId\":\"admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fieldErrors[0].field").value("password"));

        mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"loginId\":\"admin\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isCreated())
            .andExpect(cookie().exists("SESSION"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.user.userId").value("USR-ADMIN"))
            .andExpect(jsonPath("$.data.user.roleCodes[0]").value("R09"))
            .andReturn();
        String sessionId = result.getResponse().getCookie("SESSION").getValue();
        Integer stored = jdbc.queryForObject("select count(*) from session_store where session_id = ?", Integer.class, sessionId);
        assertThat(stored).isEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/auth/logout requires session and deletes the persisted session")
    void logout_requires_authentication_and_deletes_session_side_effect() throws Exception {
        mvc.perform(post("/api/auth/logout"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        String sessionId = loginSession();
        mvc.perform(post("/api/auth/logout").cookie(new Cookie("SESSION", sessionId)))
            .andExpect(status().isCreated())
            .andExpect(cookie().maxAge("SESSION", 0))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.loggedOut").value(true));
        Integer stored = jdbc.queryForObject("select count(*) from session_store where session_id = ?", Integer.class, sessionId);
        assertThat(stored).isZero();
    }

    @Test
    @DisplayName("PATCH /api/admin/users/{userId}/usage requires auth validates enum and persists usage change")
    void update_user_usage_validates_business_rules_and_persists_change() throws Exception {
        mvc.perform(patch("/api/admin/users/USR-1002/usage")
                .contentType("application/json")
                .content("{\"systemUseYn\":\"N\",\"reason\":\"권한 검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        Cookie session = adminCookie();
        mvc.perform(patch("/api/admin/users/USR-1002/usage")
                .cookie(session)
                .contentType("application/json")
                .content("{\"systemUseYn\":\"INVALID\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.fieldErrors[0].field").value("systemUseYn"));

        mvc.perform(patch("/api/admin/users/USR-1002/usage")
                .cookie(session)
                .contentType("application/json")
                .content("{\"systemUseYn\":\"N\",\"reason\":\"계정 잠금 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("USR-1002"))
            .andExpect(jsonPath("$.data.systemUseYn").value("N"));
        String useYn = jdbc.queryForObject("select system_use_yn from user_account where user_id = ?", String.class, "USR-1002");
        Integer historyRows = jdbc.queryForObject("select count(*) from change_history where entity_name = 'user_account' and entity_id = ? and action_type = 'UPDATE'", Integer.class, "USR-1002");
        assertThat(useYn).isEqualTo("N");
        assertThat(historyRows).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("PUT /api/admin/users/{userId}/roles validates role codes and replaces persisted active roles")
    void replace_user_roles_validates_business_rules_and_replaces_assignments() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/users/USR-1001/roles")
                .contentType("application/json")
                .content("{\"roleCodes\":[\"R03\"],\"reason\":\"무인증 검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/users/USR-1001/roles")
                .cookie(session)
                .contentType("application/json")
                .content("{\"roleCodes\":[\"R10\"],\"reason\":\"범위 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/users/USR-1001/roles")
                .cookie(session)
                .contentType("application/json")
                .content("{\"roleCodes\":[\"R03\"],\"reason\":\"업무 역할 조정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value("USR-1001"))
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R03"));
        Integer activeR03 = jdbc.queryForObject("select count(*) from user_role_assignment where user_id = ? and role_code = ? and status = 'ACTIVE'", Integer.class, "USR-1001", "R03");
        Integer revokedOld = jdbc.queryForObject("select count(*) from user_role_assignment where user_id = ? and role_code = ? and status = 'REVOKED'", Integer.class, "USR-1001", "R01");
        assertThat(activeR03).isEqualTo(1);
        assertThat(revokedOld).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("POST /api/admin/code-groups validates required fields and creates a persisted code group")
    void create_code_group_validates_and_persists_group() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(post("/api/admin/code-groups")
                .contentType("application/json")
                .content("{\"groupId\":\"TDD_GROUP\",\"groupName\":\"누락\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(post("/api/admin/code-groups")
                .cookie(session)
                .contentType("application/json")
                .content("{\"groupId\":\"TDD_GROUP\",\"groupName\":\"누락\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(post("/api/admin/code-groups")
                .cookie(session)
                .contentType("application/json")
                .content("{\"groupId\":\"TDD_GROUP\",\"groupName\":\"테스트 코드그룹\",\"description\":\"계약 검증\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\",\"reason\":\"등록 검증\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.groupId").value("TDD_GROUP"));
        Integer rows = jdbc.queryForObject("select count(*) from code_group where group_id = ? and managing_department = ?", Integer.class, "TDD_GROUP", "교수지원과");
        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DisplayName("PUT /api/admin/code-groups/{groupId} validates useYn and updates persisted group")
    void update_code_group_validates_and_persists_group_update() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/code-groups/SYSTEM_STATUS")
                .contentType("application/json")
                .content("{\"groupName\":\"시스템 상태\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/code-groups/SYSTEM_STATUS")
                .cookie(session)
                .contentType("application/json")
                .content("{\"groupName\":\"시스템 상태\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"X\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/code-groups/SYSTEM_STATUS")
                .cookie(session)
                .contentType("application/json")
                .content("{\"groupName\":\"시스템 상태 수정\",\"description\":\"수정 검증\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\",\"reason\":\"수정 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.groupId").value("SYSTEM_STATUS"));
        String name = jdbc.queryForObject("select group_name from code_group where group_id = ?", String.class, "SYSTEM_STATUS");
        assertThat(name).isEqualTo("시스템 상태 수정");
    }

    @Test
    @DisplayName("POST /api/admin/code-groups/{groupId}/codes validates required fields and creates detail code")
    void create_detail_code_validates_and_persists_code() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(post("/api/admin/code-groups/SYSTEM_STATUS/codes")
                .contentType("application/json")
                .content("{\"codeValue\":\"PENDING\",\"codeName\":\"대기\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(post("/api/admin/code-groups/SYSTEM_STATUS/codes")
                .cookie(session)
                .contentType("application/json")
                .content("{\"codeValue\":\"PENDING\",\"codeName\":\"대기\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(post("/api/admin/code-groups/SYSTEM_STATUS/codes")
                .cookie(session)
                .contentType("application/json")
                .content("{\"codeValue\":\"PENDING\",\"codeName\":\"대기\",\"sortOrder\":3,\"useYn\":\"Y\",\"validFrom\":\"2026-01-01\",\"extraAttributes\":\"{}\",\"reason\":\"상세코드 등록\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.groupId").value("SYSTEM_STATUS"))
            .andExpect(jsonPath("$.data.codeValue").value("PENDING"));
        Integer rows = jdbc.queryForObject("select count(*) from detail_code where group_id = ? and code_value = ?", Integer.class, "SYSTEM_STATUS", "PENDING");
        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DisplayName("PUT /api/admin/code-groups/{groupId}/codes/{codeValue} validates dates and updates detail code")
    void update_detail_code_validates_and_updates_code() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/code-groups/SYSTEM_STATUS/codes/ACTIVE")
                .contentType("application/json")
                .content("{\"codeName\":\"활성\",\"sortOrder\":1,\"useYn\":\"Y\",\"validFrom\":\"2026-01-01\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/code-groups/SYSTEM_STATUS/codes/ACTIVE")
                .cookie(session)
                .contentType("application/json")
                .content("{\"codeName\":\"활성\",\"sortOrder\":1,\"useYn\":\"X\",\"validFrom\":\"2026-01-01\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/code-groups/SYSTEM_STATUS/codes/ACTIVE")
                .cookie(session)
                .contentType("application/json")
                .content("{\"codeName\":\"활성 수정\",\"sortOrder\":11,\"useYn\":\"Y\",\"validFrom\":\"2026-01-01\",\"extraAttributes\":\"{}\",\"reason\":\"상세코드 수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.groupId").value("SYSTEM_STATUS"))
            .andExpect(jsonPath("$.data.codeValue").value("ACTIVE"));
        String name = jdbc.queryForObject("select code_name from detail_code where group_id = ? and code_value = ?", String.class, "SYSTEM_STATUS", "ACTIVE");
        assertThat(name).isEqualTo("활성 수정");
    }

    @Test
    @DisplayName("POST /api/admin/roles validates role code business rule and creates role")
    void create_role_validates_and_persists_role() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(post("/api/admin/roles")
                .contentType("application/json")
                .content("{\"roleCode\":\"R04\",\"roleName\":\"교수지원과\",\"purpose\":\"관리\",\"grantCriteria\":\"지정\",\"defaultDataScope\":\"ALL\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(post("/api/admin/roles")
                .cookie(session)
                .contentType("application/json")
                .content("{\"roleCode\":\"R10\",\"roleName\":\"범위오류\",\"purpose\":\"관리\",\"grantCriteria\":\"지정\",\"defaultDataScope\":\"ALL\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(post("/api/admin/roles")
                .cookie(session)
                .contentType("application/json")
                .content("{\"roleCode\":\"R04\",\"roleName\":\"교수지원과 담당\",\"purpose\":\"행정 관리\",\"grantCriteria\":\"지정\",\"defaultDataScope\":\"ALL\",\"reason\":\"역할 등록 검증\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.roleCode").value("R04"));
        String roleName = jdbc.queryForObject("select role_name from role where role_code = ?", String.class, "R04");
        assertThat(roleName).isEqualTo("교수지원과 담당");
    }

    @Test
    @DisplayName("PUT /api/admin/roles/{roleCode} validates required fields and updates role")
    void update_role_validates_and_updates_role() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/roles/R09")
                .contentType("application/json")
                .content("{\"roleName\":\"시스템관리자\",\"purpose\":\"관리\",\"grantCriteria\":\"지정\",\"defaultDataScope\":\"ALL\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/roles/R09")
                .cookie(session)
                .contentType("application/json")
                .content("{\"roleName\":\"시스템관리자\",\"purpose\":\"관리\",\"grantCriteria\":\"지정\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/roles/R09")
                .cookie(session)
                .contentType("application/json")
                .content("{\"roleName\":\"시스템관리자 계약검증\",\"purpose\":\"시스템 관리\",\"grantCriteria\":\"관리자 지정\",\"defaultDataScope\":\"ALL\",\"reason\":\"역할 수정 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCode").value("R09"));
        String roleName = jdbc.queryForObject("select role_name from role where role_code = ?", String.class, "R09");
        assertThat(roleName).isEqualTo("시스템관리자 계약검증");
    }

    @Test
    @DisplayName("POST /api/admin/user-roles validates references and creates assignment")
    void assign_user_role_validates_and_persists_assignment() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(post("/api/admin/user-roles")
                .contentType("application/json")
                .content("{\"userId\":\"USR-1002\",\"roleCode\":\"R03\",\"validFrom\":\"2026-02-01\",\"assignmentSource\":\"MANUAL\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(post("/api/admin/user-roles")
                .cookie(session)
                .contentType("application/json")
                .content("{\"userId\":\"USR-1002\",\"roleCode\":\"R10\",\"validFrom\":\"2026-02-01\",\"assignmentSource\":\"MANUAL\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(post("/api/admin/user-roles")
                .cookie(session)
                .contentType("application/json")
                .content("{\"userId\":\"USR-1002\",\"roleCode\":\"R03\",\"validFrom\":\"2026-02-01\",\"assignmentSource\":\"MANUAL\",\"approverId\":\"USR-ADMIN\",\"reason\":\"사용자 역할 부여 검증\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.assigned").value(true));
        Integer rows = jdbc.queryForObject("select count(*) from user_role_assignment where user_id = ? and role_code = ? and status = 'ACTIVE'", Integer.class, "USR-1002", "R03");
        assertThat(rows).isEqualTo(1);
    }

    @Test
    @DisplayName("PUT /api/admin/user-roles/{assignmentId} validates date input and updates assignment period")
    void update_user_role_validates_and_persists_assignment_period() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/user-roles/2")
                .contentType("application/json")
                .content("{\"validFrom\":\"2026-03-01\",\"validTo\":\"2026-12-31\",\"assignmentSource\":\"MANUAL\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/user-roles/99999")
                .cookie(session)
                .contentType("application/json")
                .content("{\"validFrom\":\"2026-03-01\",\"validTo\":\"2026-12-31\",\"assignmentSource\":\"MANUAL\",\"reason\":\"미존재 검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/user-roles/2")
                .cookie(session)
                .contentType("application/json")
                .content("{\"validFrom\":\"2026-03-01\",\"validTo\":\"2026-12-31\",\"assignmentSource\":\"MANUAL\",\"approverId\":\"USR-ADMIN\",\"reason\":\"역할 기간 수정 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.assignmentId").value(2));
        String validTo = jdbc.queryForObject("select cast(valid_to as varchar) from user_role_assignment where assignment_id = ?", String.class, 2L);
        assertThat(validTo).startsWith("2026-12-31");
    }

    @Test
    @DisplayName("DELETE /api/admin/user-roles/{assignmentId} requires auth and revokes assignment")
    void revoke_user_role_requires_auth_and_marks_assignment_revoked() throws Exception {
        mvc.perform(delete("/api/admin/user-roles/3"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        Cookie session = adminCookie();
        mvc.perform(delete("/api/admin/user-roles/3").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.assignmentId").value(3))
            .andExpect(jsonPath("$.data.status").value("REVOKED"));
        String status = jdbc.queryForObject("select status from user_role_assignment where assignment_id = ?", String.class, 3L);
        assertThat(status).isEqualTo("REVOKED");
    }

    @Test
    @DisplayName("GET /api/admin/menu-permissions filters by target portal data and returns permission contract")
    void menu_permissions_literal_operation_filters_by_target() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(get("/api/admin/menu-permissions"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        MvcResult result = mvc.perform(get("/api/admin/menu-permissions?targetType=ROLE&targetId=R09").cookie(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].targetType").value("ROLE"))
            .andExpect(jsonPath("$.data.items[0].targetId").value("R09"))
            .andExpect(jsonPath("$.data.items[0].canAccess").value("Y"))
            .andReturn();
        JsonNode items = mapper.readTree(result.getResponse().getContentAsString()).path("data").path("items");
        for (JsonNode item : items) {
            assertThat(item.path("targetId").asText()).isEqualTo("R09");
        }
    }

    @Test
    @DisplayName("PUT /api/admin/menu-permissions validates YN flags and persists permission side effect")
    void save_menu_permissions_validates_and_persists_permission() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/menu-permissions")
                .contentType("application/json")
                .content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"menuId\":\"M-USERS\",\"canRead\":\"Y\",\"canCreate\":\"Y\",\"canUpdate\":\"Y\",\"canDisable\":\"Y\",\"canAccess\":\"Y\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/menu-permissions")
                .cookie(session)
                .contentType("application/json")
                .content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"menuId\":\"M-USERS\",\"canRead\":\"X\",\"canCreate\":\"Y\",\"canUpdate\":\"Y\",\"canDisable\":\"Y\",\"canAccess\":\"Y\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/menu-permissions")
                .cookie(session)
                .contentType("application/json")
                .content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"menuId\":\"M-USERS\",\"canRead\":\"Y\",\"canCreate\":\"N\",\"canUpdate\":\"Y\",\"canDisable\":\"N\",\"canAccess\":\"Y\",\"reason\":\"권한 저장 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.saved").value(true));
        String canCreate = jdbc.queryForObject("select can_create from menu_permission where target_type = ? and target_id = ? and menu_id = ?", String.class, "ROLE", "R09", "M-USERS");
        assertThat(canCreate).isEqualTo("N");
    }

    @Test
    @DisplayName("POST /api/admin/menus validates required fields and creates menu")
    void create_menu_validates_and_persists_menu() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(post("/api/admin/menus")
                .contentType("application/json")
                .content("{\"menuId\":\"M-TDD\",\"menuName\":\"테스트 메뉴\",\"businessArea\":\"COMMON\",\"useYn\":\"Y\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(post("/api/admin/menus")
                .cookie(session)
                .contentType("application/json")
                .content("{\"menuId\":\"M-TDD\",\"menuName\":\"테스트 메뉴\",\"businessArea\":\"COMMON\",\"useYn\":\"X\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(post("/api/admin/menus")
                .cookie(session)
                .contentType("application/json")
                .content("{\"menuId\":\"M-TDD\",\"parentMenuId\":\"SYS\",\"menuName\":\"테스트 메뉴\",\"screenId\":\"SCR-TDD\",\"routePath\":\"/system/tdd\",\"iconName\":\"test\",\"businessArea\":\"COMMON\",\"description\":\"계약 테스트 메뉴\",\"sortOrder\":99,\"useYn\":\"Y\",\"reason\":\"메뉴 등록 검증\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.menuId").value("M-TDD"));
        String route = jdbc.queryForObject("select route_path from menu where menu_id = ?", String.class, "M-TDD");
        assertThat(route).isEqualTo("/system/tdd");
    }

    @Test
    @DisplayName("PUT /api/admin/menus/{menuId} validates required fields and updates menu")
    void update_menu_validates_and_persists_menu_update() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/menus/M-USERS")
                .contentType("application/json")
                .content("{\"menuName\":\"사용자 관리\",\"businessArea\":\"COMMON\",\"useYn\":\"Y\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/menus/M-USERS")
                .cookie(session)
                .contentType("application/json")
                .content("{\"menuName\":\"사용자 관리\",\"useYn\":\"Y\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/menus/M-USERS")
                .cookie(session)
                .contentType("application/json")
                .content("{\"parentMenuId\":\"SYS-UO\",\"menuName\":\"사용자 관리 수정\",\"screenId\":\"SCR-USERS\",\"routePath\":\"/system/users\",\"iconName\":\"user\",\"businessArea\":\"COMMON\",\"description\":\"사용자 검색 및 역할 관리\",\"sortOrder\":1,\"useYn\":\"Y\",\"reason\":\"메뉴 수정 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuId").value("M-USERS"));
        String name = jdbc.queryForObject("select menu_name from menu where menu_id = ?", String.class, "M-USERS");
        assertThat(name).isEqualTo("사용자 관리 수정");
    }

    @Test
    @DisplayName("PUT /api/admin/menus/{menuId}/parent rejects self parent and persists parent change")
    void change_menu_parent_validates_and_persists_parent() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/menus/M-USERS/parent")
                .contentType("application/json")
                .content("{\"parentMenuId\":\"SYS-MENU\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/menus/M-USERS/parent")
                .cookie(session)
                .contentType("application/json")
                .content("{\"parentMenuId\":\"M-USERS\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/menus/M-USERS/parent")
                .cookie(session)
                .contentType("application/json")
                .content("{\"parentMenuId\":\"SYS-MENU\",\"reason\":\"상위 메뉴 변경 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.menuId").value("M-USERS"))
            .andExpect(jsonPath("$.data.parentMenuId").value("SYS-MENU"));
        String parent = jdbc.queryForObject("select parent_menu_id from menu where menu_id = ?", String.class, "M-USERS");
        assertThat(parent).isEqualTo("SYS-MENU");
    }

    @Test
    @DisplayName("PUT /api/admin/menus/reorder requires auth and persists sort order side effect")
    void reorder_menus_requires_auth_and_persists_sort_order() throws Exception {
        mvc.perform(put("/api/admin/menus/reorder")
                .contentType("application/json")
                .content("[{\"menuId\":\"M-USERS\",\"sortOrder\":7}]"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/menus/reorder")
                .cookie(session)
                .contentType("application/json")
                .content("[{\"menuId\":\"M-USERS\",\"sortOrder\":7},{\"menuId\":\"M-ORGS\",\"sortOrder\":8}]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.count").value(2));
        Integer sortOrder = jdbc.queryForObject("select sort_order from menu where menu_id = ?", Integer.class, "M-USERS");
        Integer historyRows = jdbc.queryForObject("select count(*) from change_history where entity_name = 'menu' and entity_id = 'bulk-reorder'", Integer.class);
        assertThat(sortOrder).isEqualTo(7);
        assertThat(historyRows).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("PUT /api/admin/organizations/{orgCode}/relations validates date range and updates relation")
    void save_organization_relations_validates_and_persists_relation() throws Exception {
        Cookie session = adminCookie();
        mvc.perform(put("/api/admin/organizations/DEP-COM/relations")
                .contentType("application/json")
                .content("{\"parentOrgCode\":\"COL-EDU\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-12-31\",\"reason\":\"무인증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        mvc.perform(put("/api/admin/organizations/DEP-COM/relations")
                .cookie(session)
                .contentType("application/json")
                .content("{\"parentOrgCode\":\"COL-EDU\",\"startDate\":\"2026-12-31\",\"endDate\":\"2026-01-01\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));

        mvc.perform(put("/api/admin/organizations/DEP-COM/relations")
                .cookie(session)
                .contentType("application/json")
                .content("{\"parentOrgCode\":\"COL-EDU\",\"startDate\":\"2026-01-01\",\"endDate\":\"2026-12-31\",\"reason\":\"조직 관계 수정 검증\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orgCode").value("DEP-COM"))
            .andExpect(jsonPath("$.data.updated").value(true));
        String endDate = jdbc.queryForObject("select cast(end_date as varchar) from organization where org_code = ?", String.class, "DEP-COM");
        assertThat(endDate).startsWith("2026-12-31");
    }

    private Cookie adminCookie() throws Exception {
        return new Cookie("SESSION", loginSession());
    }

    private String loginSession() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isCreated())
            .andExpect(cookie().exists("SESSION"))
            .andExpect(jsonPath("$.data.user.userId").value("USR-ADMIN"))
            .andReturn();
        assertThat(TestTransaction.isActive()).isTrue();
        return result.getResponse().getCookie("SESSION").getValue();
    }
}
