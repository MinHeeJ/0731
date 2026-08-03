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
import static org.mockito.Mockito.atLeastOnce;
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
class ApiOperationContractCoverageTest {
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
    void openapi_fixture_is_loaded_from_classpath_contracts() throws Exception {
        ClassPathResource contract = new ClassPathResource("contracts/openapi.yaml");
        org.assertj.core.api.Assertions.assertThat(contract.exists()).isTrue();
        org.assertj.core.api.Assertions.assertThat(contract.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("/api/auth/login");
    }

    @Test
    void post_auth_login_persists_active_session_and_sets_httponly_samesite_cookie() throws Exception {
        when(authenticationPort.authenticate("admin", "admin")).thenReturn(new SessionUser(null, "admin", "admin", "시스템관리자", List.of("R09")));
        when(sessionService.create("admin")).thenReturn("SESSION-ACTIVE-1");
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", containsString("SESSION=SESSION-ACTIVE-1")))
            .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
            .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.roles[0]").value("R09"));
        verify(sessionService).create("admin");
    }

    @Test
    void post_auth_login_rejects_missing_password_without_session_side_effect() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{\"loginId\":\"admin\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.password").exists());
        verify(sessionService, never()).create(anyString());
    }

    @Test
    void post_auth_logout_revokes_active_session_and_expires_cookie() throws Exception {
        mvc.perform(post("/api/auth/logout").cookie(new jakarta.servlet.http.Cookie("SESSION", "SESSION-ACTIVE-1")))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
            .andExpect(jsonPath("$.data.status").value("REVOKED"));
        verify(sessionService).revoke("SESSION-ACTIVE-1");
    }

    @Test
    void get_auth_me_returns_api_response_body_contract() throws Exception {
        mvc.perform(get("/api/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void get_users_applies_filter_and_returns_portal_user_body_contract() throws Exception {
        when(userManagementService.list(anyMap())).thenReturn(List.of(Map.of("userId", "U10002", "staffNo", "10002", "displayName", "홍길동", "roleCodes", "R02")));
        mvc.perform(get("/api/users?staffNo=10002&roleCode=R02"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].staffNo").value("10002"))
            .andExpect(jsonPath("$.data[0].roleCodes").value("R02"));
        verify(userManagementService).list(anyMap());
    }

    @Test
    void patch_users_usage_updates_use_yn_and_change_history_side_effect() throws Exception {
        when(userManagementService.updateUsage("U10002", "N", "휴직")).thenReturn(Map.of("userId", "U10002", "useYn", "N", "changeHistoryRecorded", true));
        mvc.perform(patch("/api/users/U10002/usage").contentType("application/json").content("{\"useYn\":\"N\",\"staffName\":\"원천변경금지\",\"changeReason\":\"휴직\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.useYn").value("N"))
            .andExpect(jsonPath("$.data.changeHistoryRecorded").value(true));
        verify(userManagementService).updateUsage("U10002", "N", "휴직");
    }

    @Test
    void put_users_roles_replaces_manual_roles_and_returns_side_effect_contract() throws Exception {
        when(userManagementService.replaceRoles(eq("U10002"), any(), eq("권한변경"))).thenReturn(Map.of("userId", "U10002", "roleCodes", "R02", "changeHistoryRecorded", true));
        mvc.perform(put("/api/users/U10002/roles").contentType("application/json").content("{\"roleCodes\":[\"R02\"],\"changeReason\":\"권한변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.userId").value("U10002"))
            .andExpect(jsonPath("$.data.changeHistoryRecorded").value(true));
        verify(userManagementService).replaceRoles(eq("U10002"), any(), eq("권한변경"));
    }

    @Test
    void get_organizations_and_tree_return_db_backed_org_contract() throws Exception {
        when(userMapper.listOrganizations(anyMap())).thenReturn(List.of(Map.of("organizationCode", "ORG100", "organizationName", "교무처")));
        mvc.perform(get("/api/organizations?organizationCode=ORG100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].organizationCode").value("ORG100"));
        mvc.perform(get("/api/organizations/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].organizationName").value("교무처"));
        verify(userMapper, atLeastOnce()).listOrganizations(anyMap());
    }

    @Test
    void put_organizations_relations_records_relation_and_change_history() throws Exception {
        when(userMapper.listOrganizationRelations("ORG100")).thenReturn(List.of(), List.of(Map.of("organizationCode", "ORG100", "parentOrganizationCode", "ROOT")));
        mvc.perform(put("/api/organizations/ORG100/relations").contentType("application/json").content("{\"parentOrganizationCode\":\"ROOT\",\"effectiveStartDate\":\"2026-08-01\",\"effectiveEndDate\":\"2026-12-31\",\"changeReason\":\"조직개편\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].parentOrganizationCode").value("ROOT"));
        verify(userMapper).insertOrganizationRelation("ORG100", "ROOT", "2026-08-01", "2026-12-31", "조직개편");
        verify(changeHistoryService).record(eq("organization_relation"), eq("ORG100"), eq("UPDATE"), any(), any(), anyString(), eq("조직개편"));
    }

    @Test
    void get_roles_post_roles_and_put_roles_cover_role_side_effects() throws Exception {
        when(adminMapper.listRoles(anyMap())).thenReturn(List.of(Map.of("roleCode", "R02", "roleName", "부서관리자")));
        when(adminMapper.updateRole(eq("R02"), anyMap())).thenReturn(1);
        mvc.perform(get("/api/roles?roleCode=R02"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].roleCode").value("R02"));
        mvc.perform(post("/api/roles").contentType("application/json").content("{\"roleCode\":\"R02\",\"roleName\":\"부서관리자\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].roleName").value("부서관리자"));
        mvc.perform(put("/api/roles/R02").contentType("application/json").content("{\"purpose\":\"관리\",\"assignmentCriteria\":\"MANUAL\",\"defaultDataScope\":\"ORG\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].roleCode").value("R02"));
        verify(adminMapper).createRole(anyMap());
        verify(adminMapper).updateRole(eq("R02"), anyMap());
        verify(changeHistoryService).record(eq("role"), eq("R02"), eq("CREATE"), any(), any(), anyString(), eq("등록"));
        verify(changeHistoryService).record(eq("role"), eq("R02"), eq("UPDATE"), any(), any(), anyString(), eq("수정"));
    }

    @Test
    void user_role_operations_grant_update_and_revoke_manual_assignments() throws Exception {
        List<Map<String, Object>> activeAssignment = List.of(Map.<String, Object>of("assignmentId", 10, "userId", "U10002", "roleCode", "R02", "status", "ACTIVE"));
        List<Map<String, Object>> revokedAssignment = List.of(Map.<String, Object>of("assignmentId", 10, "userId", "U10002", "roleCode", "R02", "status", "REVOKED"));
        when(adminMapper.listUserRoles(anyMap())).thenReturn(activeAssignment, activeAssignment, activeAssignment, revokedAssignment);
        when(adminMapper.updateUserRole(eq(10L), anyMap())).thenReturn(1);
        when(adminMapper.revokeUserRole(10L)).thenReturn(1);
        mvc.perform(get("/api/user-roles?userId=U10002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
        mvc.perform(post("/api/user-roles").contentType("application/json").content("{\"userId\":\"U10002\",\"roleCode\":\"R02\",\"changeReason\":\"수동부여\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].roleCode").value("R02"));
        mvc.perform(put("/api/user-roles/10").contentType("application/json").content("{\"approvedBy\":\"admin\",\"effectiveStartDate\":\"2026-08-01\",\"changeReason\":\"기간수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].assignmentId").value(10));
        mvc.perform(delete("/api/user-roles/10").contentType("application/json").content("{\"changeReason\":\"회수\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].status").value("REVOKED"));
        verify(adminMapper).grantUserRole(anyMap(), anyString());
        verify(adminMapper).updateUserRole(eq(10L), anyMap());
        verify(adminMapper).revokeUserRole(10L);
        verify(changeHistoryService).record(eq("user_role"), eq("10"), eq("REVOKE"), any(), eq("{status:REVOKED}"), anyString(), eq("회수"));
    }

    @Test
    void menu_operations_cover_list_create_update_tree_parent_and_reorder_side_effects() throws Exception {
        when(adminMapper.listMenus(anyMap())).thenReturn(List.of(Map.of("menuId", "M100", "menuName", "사용자관리", "displayOrder", 1)));
        when(adminMapper.updateMenu(eq("M100"), anyMap())).thenReturn(1);
        mvc.perform(get("/api/menus?menuName=사용자"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("M100"));
        mvc.perform(get("/api/menus/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuName").value("사용자관리"));
        mvc.perform(post("/api/menus").contentType("application/json").content("{\"menuId\":\"M100\",\"menuName\":\"사용자관리\",\"url\":\"/users\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("M100"));
        mvc.perform(put("/api/menus/M100").contentType("application/json").content("{\"menuName\":\"사용자관리\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("M100"));
        mvc.perform(put("/api/menus/M100/parent").contentType("application/json").content("{\"parentMenuId\":\"ROOT\",\"changeReason\":\"부모변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuId").value("M100"));
        mvc.perform(put("/api/menus/reorder").contentType("application/json").content("{\"items\":[{\"menuId\":\"M100\",\"displayOrder\":1}],\"changeReason\":\"순서변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].displayOrder").value(1));
        verify(adminMapper).createMenu(anyMap());
        verify(adminMapper).updateMenu(eq("M100"), anyMap());
        verify(adminMapper).updateMenuParent("M100", "ROOT");
        verify(adminMapper).updateMenuOrder("M100", 1);
    }

    @Test
    void put_menus_parent_rejects_self_parent_business_rule_without_db_update() throws Exception {
        mvc.perform(put("/api/menus/M100/parent").contentType("application/json").content("{\"parentMenuId\":\"M100\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.parentMenuId").exists());
        verify(adminMapper, never()).updateMenuParent(anyString(), anyString());
    }

    @Test
    void my_menus_flows_through_service_to_persistence_signal() throws Exception {
        when(myMenuService.listMyMenus(anyString())).thenReturn(List.of(Map.of("menuId", "M100", "menuName", "내 메뉴")));
        mvc.perform(get("/api/menus/my"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].menuName").value("내 메뉴"));
        verify(myMenuService).listMyMenus(anyString());
    }

    @Test
    void menu_permissions_get_and_put_upsert_authorization_side_effect() throws Exception {
        when(adminMapper.listMenuPermissions(anyMap())).thenReturn(List.of(Map.of("targetType", "ROLE", "targetId", "R02", "menuId", "M100", "decision", "ALLOW")));
        when(authorizationService.canAccess(anyString(), eq("/api/menu-permissions"))).thenReturn(true);
        mvc.perform(get("/api/menu-permissions?targetType=ROLE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].decision").value("ALLOW"));
        mvc.perform(put("/api/menu-permissions").contentType("application/json").content("{\"permissions\":[{\"targetType\":\"ROLE\",\"targetId\":\"R02\",\"menuId\":\"M100\",\"decision\":\"ALLOW\"}],\"targetId\":\"R02\",\"changeReason\":\"권한저장\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].targetId").value("R02"));
        verify(adminMapper).upsertMenuPermission(anyMap());
        verify(authorizationService).canAccess(anyString(), eq("/api/menu-permissions"));
        verify(changeHistoryService).record(eq("menu_permission"), eq("R02"), eq("UPDATE"), any(), any(), anyString(), eq("권한저장"));
    }

    @Test
    void code_group_and_detail_code_operations_cover_tree_create_update_side_effects() throws Exception {
        when(adminMapper.listCodeGroups(anyMap())).thenReturn(List.of(Map.of("groupId", "CG100", "groupName", "공통코드")));
        when(adminMapper.listDetailCodes("CG100")).thenReturn(List.of(Map.of("groupId", "CG100", "codeValue", "D100", "codeName", "상세")));
        when(adminMapper.updateCodeGroup(eq("CG100"), anyMap())).thenReturn(1);
        when(adminMapper.updateDetailCode(eq("CG100"), eq("D100"), anyMap())).thenReturn(1);
        mvc.perform(get("/api/code-groups?groupId=CG100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupId").value("CG100"));
        mvc.perform(post("/api/code-groups").contentType("application/json").content("{\"groupId\":\"CG100\",\"groupName\":\"공통코드\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupName").value("공통코드"));
        mvc.perform(put("/api/code-groups/CG100").contentType("application/json").content("{\"groupName\":\"공통코드수정\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupId").value("CG100"));
        mvc.perform(get("/api/code-groups/CG100/detail-codes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeValue").value("D100"));
        mvc.perform(get("/api/code-groups/CG100/detail-codes/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeName").value("상세"));
        mvc.perform(post("/api/code-groups/CG100/detail-codes").contentType("application/json").content("{\"codeValue\":\"D100\",\"codeName\":\"상세\",\"changeReason\":\"등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].codeValue").value("D100"));
        mvc.perform(put("/api/code-groups/CG100/detail-codes/D100").contentType("application/json").content("{\"codeName\":\"상세수정\",\"changeReason\":\"수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].groupId").value("CG100"));
        verify(adminMapper).createCodeGroup(anyMap());
        verify(adminMapper).updateCodeGroup(eq("CG100"), anyMap());
        verify(adminMapper).createDetailCode(eq("CG100"), anyMap());
        verify(adminMapper).updateDetailCode(eq("CG100"), eq("D100"), anyMap());
    }
}
