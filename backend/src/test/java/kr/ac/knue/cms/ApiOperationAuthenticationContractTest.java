package kr.ac.knue.cms;

import kr.ac.knue.cms.auth.AuthController;
import kr.ac.knue.cms.auth.AuthenticationPort;
import kr.ac.knue.cms.auth.SessionAuthenticationInterceptor;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.authorization.AuthorizationService;
import kr.ac.knue.cms.authorization.MenuPermissionController;
import kr.ac.knue.cms.authorization.UserRoleController;
import kr.ac.knue.cms.code.CodeGroupController;
import kr.ac.knue.cms.code.DetailCodeController;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.ChangeHistoryService;
import kr.ac.knue.cms.config.SecurityConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
@Import({SecurityConfig.class, SessionAuthenticationInterceptor.class})
class ApiOperationAuthenticationContractTest {
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
    void patch_api_users_usage_requires_auth_before_user_account_side_effect() throws Exception {
        mvc.perform(patch("/api/users/U10002/usage").contentType("application/json").content("{\"useYn\":\"N\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(userManagementService, never()).updateUsage(anyString(), anyString(), anyString());
    }

    @Test
    void put_api_users_roles_requires_auth_before_user_role_side_effect() throws Exception {
        mvc.perform(put("/api/users/U10002/roles").contentType("application/json").content("{\"roleCodes\":[\"R02\"],\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(userManagementService, never()).replaceRoles(anyString(), any(), anyString());
    }

    @Test
    void post_api_code_groups_requires_auth_before_change_history_side_effect() throws Exception {
        mvc.perform(post("/api/code-groups").contentType("application/json").content("{\"groupId\":\"CG100\",\"groupName\":\"공통코드\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).createCodeGroup(anyMap());
    }

    @Test
    void put_api_code_groups_requires_auth_before_change_history_side_effect() throws Exception {
        mvc.perform(put("/api/code-groups/CG100").contentType("application/json").content("{\"groupName\":\"공통코드수정\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).updateCodeGroup(anyString(), anyMap());
    }

    @Test
    void post_api_code_groups_detail_codes_requires_auth_before_change_history_side_effect() throws Exception {
        mvc.perform(post("/api/code-groups/CG100/detail-codes").contentType("application/json").content("{\"codeValue\":\"D100\",\"codeName\":\"상세\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).createDetailCode(anyString(), anyMap());
    }

    @Test
    void put_api_code_groups_detail_codes_requires_auth_before_change_history_side_effect() throws Exception {
        mvc.perform(put("/api/code-groups/CG100/detail-codes/D100").contentType("application/json").content("{\"codeName\":\"상세수정\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).updateDetailCode(anyString(), anyString(), anyMap());
    }

    @Test
    void post_api_menus_requires_auth_before_change_history_side_effect() throws Exception {
        mvc.perform(post("/api/menus").contentType("application/json").content("{\"menuId\":\"M100\",\"menuName\":\"사용자관리\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).createMenu(anyMap());
    }

    @Test
    void put_api_menus_requires_auth_before_change_history_side_effect() throws Exception {
        mvc.perform(put("/api/menus/M100").contentType("application/json").content("{\"menuName\":\"사용자관리\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).updateMenu(anyString(), anyMap());
    }

    @Test
    void put_api_menus_parent_requires_auth_before_parent_menu_id_side_effect() throws Exception {
        mvc.perform(put("/api/menus/M100/parent").contentType("application/json").content("{\"parentMenuId\":\"ROOT\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).updateMenuParent(anyString(), anyString());
    }

    @Test
    void put_api_menus_reorder_requires_auth_before_display_order_side_effect() throws Exception {
        mvc.perform(put("/api/menus/reorder").contentType("application/json").content("{\"items\":[{\"menuId\":\"M100\",\"displayOrder\":1}],\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).updateMenuOrder(anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void put_api_organizations_relations_requires_auth_before_relation_side_effect() throws Exception {
        mvc.perform(put("/api/organizations/ORG100/relations").contentType("application/json").content("{\"parentOrganizationCode\":\"ROOT\",\"effectiveStartDate\":\"2026-08-01\",\"effectiveEndDate\":\"2026-12-31\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(userMapper, never()).insertOrganizationRelation(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void post_api_roles_requires_auth_before_change_history_side_effect() throws Exception {
        mvc.perform(post("/api/roles").contentType("application/json").content("{\"roleCode\":\"R02\",\"roleName\":\"부서관리자\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).createRole(anyMap());
    }

    @Test
    void put_api_roles_requires_auth_before_assignment_criteria_side_effect() throws Exception {
        mvc.perform(put("/api/roles/R02").contentType("application/json").content("{\"assignmentCriteria\":\"MANUAL\",\"defaultDataScope\":\"ORG\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).updateRole(anyString(), anyMap());
    }

    @Test
    void post_api_user_roles_requires_auth_before_assignment_transition() throws Exception {
        mvc.perform(post("/api/user-roles").contentType("application/json").content("{\"userId\":\"U10002\",\"roleCode\":\"R02\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).grantUserRole(anyMap(), anyString());
    }

    @Test
    void put_api_user_roles_requires_auth_before_manual_assignment_side_effect() throws Exception {
        mvc.perform(put("/api/user-roles/10").contentType("application/json").content("{\"approvedBy\":\"admin\",\"effectiveStartDate\":\"2026-08-01\",\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).updateUserRole(org.mockito.ArgumentMatchers.anyLong(), anyMap());
    }

    @Test
    void delete_api_user_roles_requires_auth_before_revoke_transition() throws Exception {
        mvc.perform(delete("/api/user-roles/10").contentType("application/json").content("{\"changeReason\":\"권한검증\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(adminMapper, never()).revokeUserRole(org.mockito.ArgumentMatchers.anyLong());
    }
}
