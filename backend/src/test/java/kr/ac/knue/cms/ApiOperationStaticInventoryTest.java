package kr.ac.knue.cms;

import kr.ac.knue.cms.auth.AuthController;
import kr.ac.knue.cms.auth.AuthenticationPort;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.auth.SessionUser;
import kr.ac.knue.cms.authorization.AuthorizationService;
import kr.ac.knue.cms.authorization.MenuPermissionController;
import kr.ac.knue.cms.authorization.UserRoleController;
import kr.ac.knue.cms.code.CodeGroupController;
import kr.ac.knue.cms.code.DetailCodeController;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import kr.ac.knue.cms.menu.MenuInformationController;
import kr.ac.knue.cms.menu.MenuStructureController;
import kr.ac.knue.cms.menu.MyMenuController;
import kr.ac.knue.cms.menu.MyMenuService;
import kr.ac.knue.cms.organization.OrganizationController;
import kr.ac.knue.cms.role.RoleController;
import kr.ac.knue.cms.user.UserManagementController;
import kr.ac.knue.cms.user.UserManagementService;
import kr.ac.knue.cms.user.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
    AuthController.class,
    UserManagementController.class,
    OrganizationController.class,
    RoleController.class,
    UserRoleController.class,
    MenuInformationController.class,
    MenuStructureController.class,
    MyMenuController.class,
    MenuPermissionController.class,
    CodeGroupController.class,
    DetailCodeController.class
})
class ApiOperationStaticInventoryTest {
    @Autowired MockMvc mvc;
    @MockBean AuthenticationPort authenticationPort;
    @MockBean SessionService sessionService;
    @MockBean UserManagementService userManagementService;
    @MockBean UserMapper userMapper;
    @MockBean AdminMapper adminMapper;
    @MockBean ChangeHistoryService changeHistoryService;
    @MockBean MyMenuService myMenuService;
    @MockBean AuthorizationService authorizationService;

    @Test
    void classpath_openapi_contract_fixture_is_present_for_contract_tests() throws Exception {
        ClassPathResource contract = new ClassPathResource("contracts/openapi.yaml");
        org.assertj.core.api.Assertions.assertThat(contract.exists()).isTrue();
        org.assertj.core.api.Assertions.assertThat(contract.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("/api/users");
    }

    @Test
    void get_api_users_returns_body_contract_and_portal_filter_from_service() throws Exception {
        when(userManagementService.list(anyMap())).thenReturn(List.of(Map.<String, Object>of("userId", "U10002", "staffNo", "10002", "displayName", "홍길동", "roleCodes", "R02")));
        mvc.perform(get("/api/users").queryParam("staffNo", "10002").queryParam("roleCode", "R02"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].userId").value("U10002"))
            .andExpect(jsonPath("$.data[0].roleCodes").value("R02"));
        verify(userManagementService).list(anyMap());
    }

    @Test
    void patch_api_users_usage_validates_use_yn_and_keeps_db_side_effect_blocked() throws Exception {
        mvc.perform(patch("/api/users/U10002/usage").contentType("application/json").content("{\"changeReason\":\"누락검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields.useYn").value("필수값입니다."));
        verify(userManagementService, never()).updateUsage(anyString(), anyString(), anyString());
    }

    @Test
    void patch_api_users_usage_updates_user_account_side_effect_contract() throws Exception {
        when(userManagementService.updateUsage("U10002", "N", "휴직")).thenReturn(Map.of("userId", "U10002", "useYn", "N", "changeHistoryRecorded", true));
        mvc.perform(patch("/api/users/U10002/usage").contentType("application/json").content("{\"useYn\":\"N\",\"changeReason\":\"휴직\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value("U10002"))
            .andExpect(jsonPath("$.data.useYn").value("N"))
            .andExpect(jsonPath("$.data.changeHistoryRecorded").value(true));
        verify(userManagementService).updateUsage("U10002", "N", "휴직");
    }

    @Test
    void put_api_users_roles_validates_role_codes_without_user_role_side_effect() throws Exception {
        mvc.perform(put("/api/users/U10002/roles").contentType("application/json").content("{\"roleCodes\":[],\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.roleCodes").value("하나 이상 필요합니다."));
        verify(userManagementService, never()).replaceRoles(anyString(), any(), anyString());
    }

    @Test
    void put_api_users_roles_replaces_user_role_and_records_change_history_signal() throws Exception {
        when(userManagementService.replaceRoles(eq("U10002"), any(), eq("권한변경"))).thenReturn(Map.of("userId", "U10002", "roleCodes", "R02", "changeHistoryRecorded", true));
        mvc.perform(put("/api/users/U10002/roles").contentType("application/json").content("{\"roleCodes\":[\"R02\"],\"changeReason\":\"권한변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value("U10002"))
            .andExpect(jsonPath("$.data.roleCodes").value("R02"))
            .andExpect(jsonPath("$.data.changeHistoryRecorded").value(true));
        verify(userManagementService).replaceRoles(eq("U10002"), any(), eq("권한변경"));
    }

    @Test
    void post_api_auth_login_rejects_bad_credentials_business_case_without_session_create() throws Exception {
        when(authenticationPort.authenticate("admin", "wrong")).thenThrow(new BusinessException(400, "아이디 또는 비밀번호가 올바르지 않습니다.", Map.of("loginId", "인증 실패")));
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"loginId\":\"admin\",\"password\":\"wrong\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields.loginId").value("인증 실패"));
        verify(sessionService, never()).create(anyString());
    }

    @Test
    void post_api_auth_login_sets_session_cookie_and_returns_user_contract() throws Exception {
        when(authenticationPort.authenticate("admin", "admin")).thenReturn(new SessionUser(null, "admin", "admin", "시스템관리자", List.of("R09")));
        when(sessionService.create("admin")).thenReturn("SESSION-ACTIVE-1");
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", containsString("SESSION=SESSION-ACTIVE-1")))
            .andExpect(jsonPath("$.data.sessionId").value("SESSION-ACTIVE-1"))
            .andExpect(jsonPath("$.data.roles[0]").value("R09"));
        verify(sessionService).create("admin");
    }

    @Test
    void post_api_auth_logout_requires_session_cookie_without_revoke_side_effect() throws Exception {
        mvc.perform(post("/api/auth/logout"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.SESSION").value("필수 쿠키입니다."));
        verify(sessionService, never()).revoke(anyString());
    }

    @Test
    void post_api_auth_logout_revokes_session_and_expires_cookie_state_transition() throws Exception {
        mvc.perform(post("/api/auth/logout").cookie(new jakarta.servlet.http.Cookie("SESSION", "SESSION-ACTIVE-1")))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
            .andExpect(jsonPath("$.data.status").value("REVOKED"));
        verify(sessionService).revoke("SESSION-ACTIVE-1");
    }

    @Test
    void get_api_organizations_returns_filtered_body_contract_from_mapper() throws Exception {
        when(userMapper.listOrganizations(anyMap())).thenReturn(List.of(Map.<String, Object>of("organizationCode", "ORG100", "organizationName", "교무처")));
        mvc.perform(get("/api/organizations").queryParam("organizationCode", "ORG100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].organizationCode").value("ORG100"));
        verify(userMapper).listOrganizations(anyMap());
    }

    @Test
    void put_api_organizations_relations_validates_date_business_rule_without_insert() throws Exception {
        mvc.perform(put("/api/organizations/ORG100/relations").contentType("application/json").content("{\"parentOrganizationCode\":\"ROOT\",\"effectiveStartDate\":\"2026-08-10\",\"effectiveEndDate\":\"2026-08-01\",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.effectiveEndDate").exists());
        verify(userMapper, never()).insertOrganizationRelation(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void put_api_organizations_relations_writes_relation_and_history_side_effect() throws Exception {
        when(userMapper.listOrganizationRelations("ORG100")).thenReturn(List.of(), List.of(Map.<String, Object>of("organizationCode", "ORG100", "parentOrganizationCode", "ROOT")));
        mvc.perform(put("/api/organizations/ORG100/relations").contentType("application/json").content("{\"parentOrganizationCode\":\"ROOT\",\"effectiveStartDate\":\"2026-08-01\",\"effectiveEndDate\":\"2026-12-31\",\"changeReason\":\"조직개편\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].parentOrganizationCode").value("ROOT"));
        verify(userMapper).insertOrganizationRelation("ORG100", "ROOT", "2026-08-01", "2026-12-31", "조직개편");
        verify(changeHistoryService).record(eq("organization_relation"), eq("ORG100"), eq("UPDATE"), any(), any(), anyString(), eq("조직개편"));
    }

    @Test
    void get_api_roles_returns_body_contract_from_mapper() throws Exception {
        when(adminMapper.listRoles(anyMap())).thenReturn(List.of(Map.<String, Object>of("roleCode", "R02", "roleName", "부서관리자")));
        mvc.perform(get("/api/roles").queryParam("roleCode", "R02"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].roleCode").value("R02"));
        verify(adminMapper).listRoles(anyMap());
    }

    @Test
    void post_api_roles_validates_role_code_without_create_side_effect() throws Exception {
        mvc.perform(post("/api/roles").contentType("application/json").content("{\"roleName\":\"부서관리자\",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.roleCode").value("필수값입니다."));
        verify(adminMapper, never()).createRole(anyMap());
    }

    @Test
    void post_api_roles_creates_role_and_records_change_history_side_effect() throws Exception {
        when(adminMapper.listRoles(anyMap())).thenReturn(List.of(Map.<String, Object>of("roleCode", "R02", "roleName", "부서관리자")));
        mvc.perform(post("/api/roles").contentType("application/json").content("{\"roleCode\":\"R02\",\"roleName\":\"부서관리자\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].roleName").value("부서관리자"));
        verify(adminMapper).createRole(anyMap());
        verify(changeHistoryService).record(eq("role"), eq("R02"), eq("CREATE"), any(), any(), anyString(), eq("등록"));
    }

    @Test
    void put_api_roles_validates_not_found_without_change_history() throws Exception {
        when(adminMapper.updateRole(eq("R02"), anyMap())).thenReturn(0);
        mvc.perform(put("/api/roles/R02").contentType("application/json").content("{\"defaultDataScope\":\"ORG\",\"assignmentCriteria\":\"MANUAL\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.roleCode").exists());
        verify(changeHistoryService, never()).record(eq("role"), eq("R02"), eq("UPDATE"), any(), any(), anyString(), anyString());
    }

    @Test
    void put_api_roles_updates_assignment_criteria_default_scope_and_history() throws Exception {
        when(adminMapper.updateRole(eq("R02"), anyMap())).thenReturn(1);
        when(adminMapper.listRoles(anyMap())).thenReturn(List.of(Map.<String, Object>of("roleCode", "R02", "assignmentCriteria", "MANUAL", "defaultDataScope", "ORG")));
        mvc.perform(put("/api/roles/R02").contentType("application/json").content("{\"assignmentCriteria\":\"MANUAL\",\"defaultDataScope\":\"ORG\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].assignmentCriteria").value("MANUAL"))
            .andExpect(jsonPath("$.data[0].defaultDataScope").value("ORG"));
        verify(adminMapper).updateRole(eq("R02"), anyMap());
        verify(changeHistoryService).record(eq("role"), eq("R02"), eq("UPDATE"), any(), any(), anyString(), eq("수정"));
    }

    @Test
    void get_api_user_roles_returns_assignment_body_contract() throws Exception {
        when(adminMapper.listUserRoles(anyMap())).thenReturn(List.of(Map.<String, Object>of("assignmentId", 10, "userId", "U10002", "roleCode", "R02", "status", "ACTIVE")));
        mvc.perform(get("/api/user-roles").queryParam("userId", "U10002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
        verify(adminMapper).listUserRoles(anyMap());
    }

    @Test
    void post_api_user_roles_validates_required_user_and_role_without_grant() throws Exception {
        mvc.perform(post("/api/user-roles").contentType("application/json").content("{\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.userId").value("필수값입니다."))
            .andExpect(jsonPath("$.error.fields.roleCode").value("필수값입니다."));
        verify(adminMapper, never()).grantUserRole(anyMap(), anyString());
    }

    @Test
    void post_api_user_roles_grants_assignment_and_records_change_history_transition() throws Exception {
        when(adminMapper.listUserRoles(anyMap())).thenReturn(List.of(Map.<String, Object>of("assignmentId", 10, "userId", "U10002", "roleCode", "R02", "status", "ACTIVE")));
        mvc.perform(post("/api/user-roles").contentType("application/json").content("{\"userId\":\"U10002\",\"roleCode\":\"R02\",\"changeReason\":\"수동부여\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
        verify(adminMapper).grantUserRole(anyMap(), anyString());
        verify(changeHistoryService).record(eq("user_role"), eq("U10002"), eq("CREATE"), any(), any(), anyString(), eq("수동부여"));
    }

    @Test
    void put_api_user_roles_validates_position_role_business_rule_without_history() throws Exception {
        when(adminMapper.updateUserRole(eq(10L), anyMap())).thenReturn(0);
        mvc.perform(put("/api/user-roles/10").contentType("application/json").content("{\"approvedBy\":\"admin\",\"changeReason\":\"기간수정\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.assignmentId").exists());
        verify(changeHistoryService, never()).record(eq("user_role"), eq("10"), eq("UPDATE"), any(), any(), anyString(), anyString());
    }

    @Test
    void put_api_user_roles_updates_manual_assignment_and_records_history() throws Exception {
        when(adminMapper.updateUserRole(eq(10L), anyMap())).thenReturn(1);
        when(adminMapper.listUserRoles(anyMap())).thenReturn(List.of(Map.<String, Object>of("assignmentId", 10, "status", "ACTIVE", "approvedBy", "admin")));
        mvc.perform(put("/api/user-roles/10").contentType("application/json").content("{\"approvedBy\":\"admin\",\"effectiveStartDate\":\"2026-08-01\",\"changeReason\":\"기간수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].assignmentId").value(10));
        verify(adminMapper).updateUserRole(eq(10L), anyMap());
        verify(changeHistoryService).record(eq("user_role"), eq("10"), eq("UPDATE"), any(), any(), anyString(), eq("기간수정"));
    }

    @Test
    void delete_api_user_roles_validates_position_assignment_without_revoke_history() throws Exception {
        when(adminMapper.revokeUserRole(10L)).thenReturn(0);
        mvc.perform(delete("/api/user-roles/10").contentType("application/json").content("{\"changeReason\":\"회수\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.assignmentId").exists());
        verify(changeHistoryService, never()).record(eq("user_role"), eq("10"), eq("REVOKE"), any(), any(), anyString(), anyString());
    }

    @Test
    void delete_api_user_roles_revokes_assignment_and_records_revoke_transition() throws Exception {
        when(adminMapper.revokeUserRole(10L)).thenReturn(1);
        when(adminMapper.listUserRoles(anyMap())).thenReturn(List.of(Map.<String, Object>of("assignmentId", 10, "status", "REVOKED")));
        mvc.perform(delete("/api/user-roles/10").contentType("application/json").content("{\"changeReason\":\"회수\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("REVOKED"));
        verify(adminMapper).revokeUserRole(10L);
        verify(changeHistoryService).record(eq("user_role"), eq("10"), eq("REVOKE"), any(), eq("{status:REVOKED}"), anyString(), eq("회수"));
    }

    @Test
    void get_api_menus_returns_menu_body_contract() throws Exception {
        when(adminMapper.listMenus(anyMap())).thenReturn(List.of(Map.<String, Object>of("menuId", "M100", "menuName", "사용자관리", "displayOrder", 1)));
        mvc.perform(get("/api/menus").queryParam("menuName", "사용자"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("M100"));
        verify(adminMapper).listMenus(anyMap());
    }

    @Test
    void post_api_menus_validates_required_fields_without_create() throws Exception {
        mvc.perform(post("/api/menus").contentType("application/json").content("{\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.menuId").value("필수값입니다."))
            .andExpect(jsonPath("$.error.fields.menuName").value("필수값입니다."));
        verify(adminMapper, never()).createMenu(anyMap());
    }

    @Test
    void post_api_menus_creates_menu_and_records_change_history() throws Exception {
        when(adminMapper.listMenus(anyMap())).thenReturn(List.of(Map.<String, Object>of("menuId", "M100", "menuName", "사용자관리")));
        mvc.perform(post("/api/menus").contentType("application/json").content("{\"menuId\":\"M100\",\"menuName\":\"사용자관리\",\"url\":\"/users\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("M100"));
        verify(adminMapper).createMenu(anyMap());
        verify(changeHistoryService).record(eq("menu"), eq("M100"), eq("CREATE"), any(), any(), anyString(), eq("등록"));
    }

    @Test
    void put_api_menus_validates_not_found_without_update_history() throws Exception {
        when(adminMapper.updateMenu(eq("M100"), anyMap())).thenReturn(0);
        mvc.perform(put("/api/menus/M100").contentType("application/json").content("{\"menuName\":\"사용자관리\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.menuId").exists());
        verify(changeHistoryService, never()).record(eq("menu"), eq("M100"), eq("UPDATE"), any(), any(), anyString(), anyString());
    }

    @Test
    void put_api_menus_updates_menu_and_records_change_history() throws Exception {
        when(adminMapper.updateMenu(eq("M100"), anyMap())).thenReturn(1);
        when(adminMapper.listMenus(anyMap())).thenReturn(List.of(Map.<String, Object>of("menuId", "M100", "menuName", "사용자관리")));
        mvc.perform(put("/api/menus/M100").contentType("application/json").content("{\"menuName\":\"사용자관리\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("M100"));
        verify(adminMapper).updateMenu(eq("M100"), anyMap());
        verify(changeHistoryService).record(eq("menu"), eq("M100"), eq("UPDATE"), any(), any(), anyString(), eq("수정"));
    }

    @Test
    void put_api_menus_parent_validates_self_parent_without_parent_side_effect() throws Exception {
        mvc.perform(put("/api/menus/M100/parent").contentType("application/json").content("{\"parentMenuId\":\"M100\",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.parentMenuId").exists());
        verify(adminMapper, never()).updateMenuParent(anyString(), anyString());
    }

    @Test
    void put_api_menus_parent_changes_parent_menu_id_and_records_history() throws Exception {
        when(adminMapper.listMenus(anyMap())).thenReturn(List.of(Map.<String, Object>of("menuId", "M100", "parentMenuId", "ROOT")));
        mvc.perform(put("/api/menus/M100/parent").contentType("application/json").content("{\"parentMenuId\":\"ROOT\",\"changeReason\":\"부모변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].parentMenuId").value("ROOT"));
        verify(adminMapper).updateMenuParent("M100", "ROOT");
        verify(changeHistoryService).record(eq("menu"), eq("M100"), eq("UPDATE"), any(), any(), anyString(), eq("부모변경"));
    }

    @Test
    void put_api_menus_reorder_validates_items_without_display_order_updates() throws Exception {
        mvc.perform(put("/api/menus/reorder").contentType("application/json").content("{\"items\":[],\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.items").value("하나 이상 필요합니다."));
        verify(adminMapper, never()).updateMenuOrder(anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void put_api_menus_reorder_updates_display_order_and_records_history() throws Exception {
        when(adminMapper.listMenus(anyMap())).thenReturn(List.of(Map.<String, Object>of("menuId", "M100", "displayOrder", 1)));
        mvc.perform(put("/api/menus/reorder").contentType("application/json").content("{\"items\":[{\"menuId\":\"M100\",\"displayOrder\":1}],\"changeReason\":\"순서변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].displayOrder").value(1));
        verify(adminMapper).updateMenuOrder("M100", 1);
        verify(changeHistoryService).record(eq("menu"), eq("reorder"), eq("UPDATE"), any(), any(), anyString(), eq("순서변경"));
    }

    @Test
    void get_api_menu_permissions_returns_permission_contract() throws Exception {
        when(adminMapper.listMenuPermissions(anyMap())).thenReturn(List.of(Map.<String, Object>of("targetType", "ROLE", "targetId", "R02", "menuId", "M100", "decision", "ALLOW")));
        mvc.perform(get("/api/menu-permissions").queryParam("targetType", "ROLE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].decision").value("ALLOW"));
        verify(adminMapper).listMenuPermissions(anyMap());
    }

    @Test
    void put_api_menu_permissions_validates_empty_permissions_without_upsert() throws Exception {
        mvc.perform(put("/api/menu-permissions").contentType("application/json").content("{\"permissions\":[],\"targetId\":\"R02\",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.permissions").value("하나 이상 필요합니다."));
        verify(adminMapper, never()).upsertMenuPermission(anyMap());
    }

    @Test
    void put_api_menu_permissions_upserts_permissions_and_checks_authorization_side_effect() throws Exception {
        when(adminMapper.listMenuPermissions(anyMap())).thenReturn(List.of(Map.<String, Object>of("targetType", "ROLE", "targetId", "R02", "menuId", "M100", "decision", "ALLOW")));
        when(authorizationService.canAccess(anyString(), eq("/api/menu-permissions"))).thenReturn(true);
        mvc.perform(put("/api/menu-permissions").contentType("application/json").content("{\"permissions\":[{\"targetType\":\"ROLE\",\"targetId\":\"R02\",\"menuId\":\"M100\",\"decision\":\"ALLOW\"}],\"targetId\":\"R02\",\"changeReason\":\"권한저장\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].targetId").value("R02"));
        verify(adminMapper).upsertMenuPermission(anyMap());
        verify(authorizationService).canAccess(anyString(), eq("/api/menu-permissions"));
        verify(changeHistoryService).record(eq("menu_permission"), eq("R02"), eq("UPDATE"), any(), any(), anyString(), eq("권한저장"));
    }

    @Test
    void get_api_code_groups_returns_group_contract() throws Exception {
        when(adminMapper.listCodeGroups(anyMap())).thenReturn(List.of(Map.<String, Object>of("groupId", "CG100", "groupName", "공통코드")));
        mvc.perform(get("/api/code-groups").queryParam("groupId", "CG100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupId").value("CG100"));
        verify(adminMapper).listCodeGroups(anyMap());
    }

    @Test
    void post_api_code_groups_validates_group_id_without_create() throws Exception {
        mvc.perform(post("/api/code-groups").contentType("application/json").content("{\"groupName\":\"공통코드\",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.groupId").value("필수값입니다."));
        verify(adminMapper, never()).createCodeGroup(anyMap());
    }

    @Test
    void post_api_code_groups_creates_group_and_records_change_history() throws Exception {
        when(adminMapper.listCodeGroups(anyMap())).thenReturn(List.of(Map.<String, Object>of("groupId", "CG100", "groupName", "공통코드")));
        mvc.perform(post("/api/code-groups").contentType("application/json").content("{\"groupId\":\"CG100\",\"groupName\":\"공통코드\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupName").value("공통코드"));
        verify(adminMapper).createCodeGroup(anyMap());
        verify(changeHistoryService).record(eq("code_group"), eq("CG100"), eq("CREATE"), any(), any(), anyString(), eq("등록"));
    }

    @Test
    void put_api_code_groups_validates_not_found_without_history() throws Exception {
        when(adminMapper.updateCodeGroup(eq("CG100"), anyMap())).thenReturn(0);
        mvc.perform(put("/api/code-groups/CG100").contentType("application/json").content("{\"groupName\":\"공통코드수정\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.groupId").exists());
        verify(changeHistoryService, never()).record(eq("code_group"), eq("CG100"), eq("UPDATE"), any(), any(), anyString(), anyString());
    }

    @Test
    void put_api_code_groups_updates_group_and_records_change_history() throws Exception {
        when(adminMapper.updateCodeGroup(eq("CG100"), anyMap())).thenReturn(1);
        when(adminMapper.listCodeGroups(anyMap())).thenReturn(List.of(Map.<String, Object>of("groupId", "CG100", "groupName", "공통코드수정")));
        mvc.perform(put("/api/code-groups/CG100").contentType("application/json").content("{\"groupName\":\"공통코드수정\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupName").value("공통코드수정"));
        verify(adminMapper).updateCodeGroup(eq("CG100"), anyMap());
        verify(changeHistoryService).record(eq("code_group"), eq("CG100"), eq("UPDATE"), any(), any(), anyString(), eq("수정"));
    }

    @Test
    void post_api_code_groups_detail_codes_validates_code_value_without_create() throws Exception {
        mvc.perform(post("/api/code-groups/CG100/detail-codes").contentType("application/json").content("{\"codeName\":\"상세\",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.codeValue").value("필수값입니다."));
        verify(adminMapper, never()).createDetailCode(anyString(), anyMap());
    }

    @Test
    void post_api_code_groups_detail_codes_creates_detail_and_records_history() throws Exception {
        when(adminMapper.listDetailCodes("CG100")).thenReturn(List.of(Map.<String, Object>of("groupId", "CG100", "codeValue", "D100", "codeName", "상세")));
        mvc.perform(post("/api/code-groups/CG100/detail-codes").contentType("application/json").content("{\"codeValue\":\"D100\",\"codeName\":\"상세\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeValue").value("D100"));
        verify(adminMapper).createDetailCode(eq("CG100"), anyMap());
        verify(changeHistoryService).record(eq("detail_code"), eq("CG100:D100"), eq("CREATE"), any(), any(), anyString(), eq("등록"));
    }

    @Test
    void put_api_code_groups_detail_codes_validates_not_found_without_history() throws Exception {
        when(adminMapper.updateDetailCode(eq("CG100"), eq("D100"), anyMap())).thenReturn(0);
        mvc.perform(put("/api/code-groups/CG100/detail-codes/D100").contentType("application/json").content("{\"codeName\":\"상세수정\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.codeValue").exists());
        verify(changeHistoryService, never()).record(eq("detail_code"), eq("CG100:D100"), eq("UPDATE"), any(), any(), anyString(), anyString());
    }

    @Test
    void put_api_code_groups_detail_codes_updates_detail_and_records_history() throws Exception {
        when(adminMapper.updateDetailCode(eq("CG100"), eq("D100"), anyMap())).thenReturn(1);
        when(adminMapper.listDetailCodes("CG100")).thenReturn(List.of(Map.<String, Object>of("groupId", "CG100", "codeValue", "D100", "codeName", "상세수정")));
        mvc.perform(put("/api/code-groups/CG100/detail-codes/D100").contentType("application/json").content("{\"codeName\":\"상세수정\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeName").value("상세수정"));
        verify(adminMapper).updateDetailCode(eq("CG100"), eq("D100"), anyMap());
        verify(changeHistoryService).record(eq("detail_code"), eq("CG100:D100"), eq("UPDATE"), any(), any(), anyString(), eq("수정"));
    }
}
