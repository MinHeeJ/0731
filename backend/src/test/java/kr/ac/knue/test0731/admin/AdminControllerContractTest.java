package kr.ac.knue.test0731.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
class AdminControllerContractTest {
    @Autowired MockMvc mvc;
    @MockBean AdminService adminService;

    @Test
    void openApiFixtureIsAvailableOnClasspath() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("operationId: listUsers")
            .contains("/api/admin/detail-codes");
    }

    @Test
    void getHealthReturnsOpenApiEnvelopeContract() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.service").value("test0731-system-admin"));
    }

    @Test
    void listUsersReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("users"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000001"), "name", "포털 Users")));
        mvc.perform(get("/api/admin/users").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 Users"));
        verify(adminService).list("users", "포털", 0, 20);
    }

    @Test
    void getUsersReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000001");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("users"), eq(id))).willReturn(Map.of("id", id, "name", "상세 Users"));
        given(adminService.get(eq("users"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/users/10000000-0000-0000-0000-000000000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 Users"));
        mvc.perform(get("/api/admin/users/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listOrganizationsReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("organizations"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000002"), "name", "포털 Organizations")));
        mvc.perform(get("/api/admin/organizations").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 Organizations"));
        verify(adminService).list("organizations", "포털", 0, 20);
    }

    @Test
    void getOrganizationsReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000002");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("organizations"), eq(id))).willReturn(Map.of("id", id, "name", "상세 Organizations"));
        given(adminService.get(eq("organizations"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/organizations/10000000-0000-0000-0000-000000000002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 Organizations"));
        mvc.perform(get("/api/admin/organizations/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listRolesReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("roles"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000003"), "name", "포털 Roles")));
        mvc.perform(get("/api/admin/roles").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 Roles"));
        verify(adminService).list("roles", "포털", 0, 20);
    }

    @Test
    void getRolesReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000003");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("roles"), eq(id))).willReturn(Map.of("id", id, "name", "상세 Roles"));
        given(adminService.get(eq("roles"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/roles/10000000-0000-0000-0000-000000000003"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 Roles"));
        mvc.perform(get("/api/admin/roles/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listUserRolesReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("user-roles"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000004"), "name", "포털 UserRoles")));
        mvc.perform(get("/api/admin/user-roles").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 UserRoles"));
        verify(adminService).list("user-roles", "포털", 0, 20);
    }

    @Test
    void getUserRolesReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000004");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("user-roles"), eq(id))).willReturn(Map.of("id", id, "name", "상세 UserRoles"));
        given(adminService.get(eq("user-roles"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/user-roles/10000000-0000-0000-0000-000000000004"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 UserRoles"));
        mvc.perform(get("/api/admin/user-roles/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listMenuPermissionsReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("menu-permissions"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000005"), "name", "포털 MenuPermissions")));
        mvc.perform(get("/api/admin/menu-permissions").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 MenuPermissions"));
        verify(adminService).list("menu-permissions", "포털", 0, 20);
    }

    @Test
    void getMenuPermissionsReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000005");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("menu-permissions"), eq(id))).willReturn(Map.of("id", id, "name", "상세 MenuPermissions"));
        given(adminService.get(eq("menu-permissions"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/menu-permissions/10000000-0000-0000-0000-000000000005"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 MenuPermissions"));
        mvc.perform(get("/api/admin/menu-permissions/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listMenuStructuresReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("menu-structures"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000006"), "name", "포털 MenuStructures")));
        mvc.perform(get("/api/admin/menu-structures").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 MenuStructures"));
        verify(adminService).list("menu-structures", "포털", 0, 20);
    }

    @Test
    void getMenuStructuresReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000006");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("menu-structures"), eq(id))).willReturn(Map.of("id", id, "name", "상세 MenuStructures"));
        given(adminService.get(eq("menu-structures"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/menu-structures/10000000-0000-0000-0000-000000000006"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 MenuStructures"));
        mvc.perform(get("/api/admin/menu-structures/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listMenusReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("menus"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000007"), "name", "포털 Menus")));
        mvc.perform(get("/api/admin/menus").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 Menus"));
        verify(adminService).list("menus", "포털", 0, 20);
    }

    @Test
    void getMenusReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000007");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("menus"), eq(id))).willReturn(Map.of("id", id, "name", "상세 Menus"));
        given(adminService.get(eq("menus"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/menus/10000000-0000-0000-0000-000000000007"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 Menus"));
        mvc.perform(get("/api/admin/menus/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listCodeGroupsReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("code-groups"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000008"), "name", "포털 CodeGroups")));
        mvc.perform(get("/api/admin/code-groups").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 CodeGroups"));
        verify(adminService).list("code-groups", "포털", 0, 20);
    }

    @Test
    void getCodeGroupsReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000008");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("code-groups"), eq(id))).willReturn(Map.of("id", id, "name", "상세 CodeGroups"));
        given(adminService.get(eq("code-groups"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/code-groups/10000000-0000-0000-0000-000000000008"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 CodeGroups"));
        mvc.perform(get("/api/admin/code-groups/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void listDetailCodesReturnsPortalFilteredDatabaseResults() throws Exception {
        given(adminService.list(eq("detail-codes"), eq("포털"), eq(0), eq(20)))
            .willReturn(List.of(Map.of("id", UUID.fromString("10000000-0000-0000-0000-000000000009"), "name", "포털 DetailCodes")));
        mvc.perform(get("/api/admin/detail-codes").param("keyword", "포털").param("page", "0").param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].name").value("포털 DetailCodes"));
        verify(adminService).list("detail-codes", "포털", 0, 20);
    }

    @Test
    void getDetailCodesReturnsDetailBodyAndNotFoundNegativeCase() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000009");
        UUID missingId = UUID.fromString("20000000-0000-0000-0000-000000000001");
        given(adminService.get(eq("detail-codes"), eq(id))).willReturn(Map.of("id", id, "name", "상세 DetailCodes"));
        given(adminService.get(eq("detail-codes"), eq(missingId))).willThrow(new NotFoundException());
        mvc.perform(get("/api/admin/detail-codes/10000000-0000-0000-0000-000000000009"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("상세 DetailCodes"));
        mvc.perform(get("/api/admin/detail-codes/20000000-0000-0000-0000-000000000001"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void createUsersEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000001");
        given(adminService.create(eq("users"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 Users", "status", "ENABLED", "targetTable", "local_user_account"));
        mvc.perform(post("/api/admin/users").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Users\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Users\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 Users"))
            .andExpect(jsonPath("$.data.targetTable").value("local_user_account"));
        verify(adminService).create(eq("users"), any(AdminItem.class));
    }

    @Test
    void updateUsersEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000001");
        given(adminService.update(eq("users"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 Users", "status", "DISABLED", "targetTable", "local_user_account"));
        mvc.perform(put("/api/admin/users/10000000-0000-0000-0000-000000000001").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Users\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/users/10000000-0000-0000-0000-000000000001").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/users/10000000-0000-0000-0000-000000000001").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Users\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 Users"))
            .andExpect(jsonPath("$.data.targetTable").value("local_user_account"));
        verify(adminService).update(eq("users"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteUsersEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000001");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("users"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/users/10000000-0000-0000-0000-000000000001").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("users"), eq(id), any());
        mvc.perform(delete("/api/admin/users/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/users/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/users/10000000-0000-0000-0000-000000000001").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("users", id, "계약 삭제");
    }

    @Test
    void createOrganizationsEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000002");
        given(adminService.create(eq("organizations"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 Organizations", "status", "ENABLED", "targetTable", "local_organization_setting"));
        mvc.perform(post("/api/admin/organizations").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Organizations\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/organizations").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/organizations").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Organizations\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 Organizations"))
            .andExpect(jsonPath("$.data.targetTable").value("local_organization_setting"));
        verify(adminService).create(eq("organizations"), any(AdminItem.class));
    }

    @Test
    void updateOrganizationsEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000002");
        given(adminService.update(eq("organizations"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 Organizations", "status", "DISABLED", "targetTable", "local_organization_setting"));
        mvc.perform(put("/api/admin/organizations/10000000-0000-0000-0000-000000000002").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Organizations\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/organizations/10000000-0000-0000-0000-000000000002").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/organizations/10000000-0000-0000-0000-000000000002").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Organizations\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 Organizations"))
            .andExpect(jsonPath("$.data.targetTable").value("local_organization_setting"));
        verify(adminService).update(eq("organizations"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteOrganizationsEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000002");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("organizations"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/organizations/10000000-0000-0000-0000-000000000002").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("organizations"), eq(id), any());
        mvc.perform(delete("/api/admin/organizations/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/organizations/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/organizations/10000000-0000-0000-0000-000000000002").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("organizations", id, "계약 삭제");
    }

    @Test
    void createRolesEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000003");
        given(adminService.create(eq("roles"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 Roles", "status", "ENABLED", "targetTable", "role"));
        mvc.perform(post("/api/admin/roles").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Roles\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/roles").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/roles").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Roles\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 Roles"))
            .andExpect(jsonPath("$.data.targetTable").value("role"));
        verify(adminService).create(eq("roles"), any(AdminItem.class));
    }

    @Test
    void updateRolesEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000003");
        given(adminService.update(eq("roles"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 Roles", "status", "DISABLED", "targetTable", "role"));
        mvc.perform(put("/api/admin/roles/10000000-0000-0000-0000-000000000003").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Roles\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/roles/10000000-0000-0000-0000-000000000003").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/roles/10000000-0000-0000-0000-000000000003").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Roles\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 Roles"))
            .andExpect(jsonPath("$.data.targetTable").value("role"));
        verify(adminService).update(eq("roles"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteRolesEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000003");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("roles"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/roles/10000000-0000-0000-0000-000000000003").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("roles"), eq(id), any());
        mvc.perform(delete("/api/admin/roles/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/roles/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/roles/10000000-0000-0000-0000-000000000003").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("roles", id, "계약 삭제");
    }

    @Test
    void createUserRolesEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000004");
        given(adminService.create(eq("user-roles"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 UserRoles", "status", "ACTIVE", "targetTable", "user_role_assignment"));
        mvc.perform(post("/api/admin/user-roles").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 UserRoles\",\"changeReason\":\"계약 변경\",\"effectiveStatus\":\"ACTIVE\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/user-roles").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/user-roles").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 UserRoles\",\"changeReason\":\"계약 변경\",\"effectiveStatus\":\"ACTIVE\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 UserRoles"))
            .andExpect(jsonPath("$.data.targetTable").value("user_role_assignment"));
        verify(adminService).create(eq("user-roles"), any(AdminItem.class));
    }

    @Test
    void updateUserRolesEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000004");
        given(adminService.update(eq("user-roles"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 UserRoles", "status", "REVOKED", "targetTable", "user_role_assignment"));
        mvc.perform(put("/api/admin/user-roles/10000000-0000-0000-0000-000000000004").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 UserRoles\",\"changeReason\":\"계약 변경\",\"effectiveStatus\":\"REVOKED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/user-roles/10000000-0000-0000-0000-000000000004").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/user-roles/10000000-0000-0000-0000-000000000004").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 UserRoles\",\"changeReason\":\"계약 변경\",\"effectiveStatus\":\"REVOKED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 UserRoles"))
            .andExpect(jsonPath("$.data.targetTable").value("user_role_assignment"));
        verify(adminService).update(eq("user-roles"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteUserRolesEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000004");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("user-roles"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/user-roles/10000000-0000-0000-0000-000000000004").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("user-roles"), eq(id), any());
        mvc.perform(delete("/api/admin/user-roles/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/user-roles/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/user-roles/10000000-0000-0000-0000-000000000004").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("user-roles", id, "계약 삭제");
    }

    @Test
    void createMenuPermissionsEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000005");
        given(adminService.create(eq("menu-permissions"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 MenuPermissions", "status", "READ", "targetTable", "menu_permission"));
        mvc.perform(post("/api/admin/menu-permissions").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 MenuPermissions\",\"changeReason\":\"계약 변경\",\"permissionAction\":\"READ\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/menu-permissions").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/menu-permissions").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 MenuPermissions\",\"changeReason\":\"계약 변경\",\"permissionAction\":\"READ\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 MenuPermissions"))
            .andExpect(jsonPath("$.data.targetTable").value("menu_permission"));
        verify(adminService).create(eq("menu-permissions"), any(AdminItem.class));
    }

    @Test
    void updateMenuPermissionsEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000005");
        given(adminService.update(eq("menu-permissions"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 MenuPermissions", "status", "UPDATE", "targetTable", "menu_permission"));
        mvc.perform(put("/api/admin/menu-permissions/10000000-0000-0000-0000-000000000005").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 MenuPermissions\",\"changeReason\":\"계약 변경\",\"permissionAction\":\"UPDATE\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/menu-permissions/10000000-0000-0000-0000-000000000005").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/menu-permissions/10000000-0000-0000-0000-000000000005").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 MenuPermissions\",\"changeReason\":\"계약 변경\",\"permissionAction\":\"UPDATE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 MenuPermissions"))
            .andExpect(jsonPath("$.data.targetTable").value("menu_permission"));
        verify(adminService).update(eq("menu-permissions"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteMenuPermissionsEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000005");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("menu-permissions"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/menu-permissions/10000000-0000-0000-0000-000000000005").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("menu-permissions"), eq(id), any());
        mvc.perform(delete("/api/admin/menu-permissions/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/menu-permissions/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/menu-permissions/10000000-0000-0000-0000-000000000005").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("menu-permissions", id, "계약 삭제");
    }

    @Test
    void createMenuStructuresEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000006");
        given(adminService.create(eq("menu-structures"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 MenuStructures", "targetTable", "menu"));
        mvc.perform(post("/api/admin/menu-structures").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 MenuStructures\",\"changeReason\":\"계약 변경\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/menu-structures").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/menu-structures").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 MenuStructures\",\"changeReason\":\"계약 변경\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 MenuStructures"))
            .andExpect(jsonPath("$.data.targetTable").value("menu"));
        verify(adminService).create(eq("menu-structures"), any(AdminItem.class));
    }

    @Test
    void updateMenuStructuresEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000006");
        given(adminService.update(eq("menu-structures"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 MenuStructures", "targetTable", "menu"));
        mvc.perform(put("/api/admin/menu-structures/10000000-0000-0000-0000-000000000006").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 MenuStructures\",\"changeReason\":\"계약 변경\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/menu-structures/10000000-0000-0000-0000-000000000006").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/menu-structures/10000000-0000-0000-0000-000000000006").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 MenuStructures\",\"changeReason\":\"계약 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 MenuStructures"))
            .andExpect(jsonPath("$.data.targetTable").value("menu"));
        verify(adminService).update(eq("menu-structures"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteMenuStructuresEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000006");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("menu-structures"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/menu-structures/10000000-0000-0000-0000-000000000006").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("menu-structures"), eq(id), any());
        mvc.perform(delete("/api/admin/menu-structures/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/menu-structures/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/menu-structures/10000000-0000-0000-0000-000000000006").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("menu-structures", id, "계약 삭제");
    }

    @Test
    void createMenusEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000007");
        given(adminService.create(eq("menus"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 Menus", "status", "VISIBLE", "targetTable", "menu"));
        mvc.perform(post("/api/admin/menus").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Menus\",\"changeReason\":\"계약 변경\",\"displayStatus\":\"VISIBLE\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/menus").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/menus").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 Menus\",\"changeReason\":\"계약 변경\",\"displayStatus\":\"VISIBLE\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 Menus"))
            .andExpect(jsonPath("$.data.targetTable").value("menu"));
        verify(adminService).create(eq("menus"), any(AdminItem.class));
    }

    @Test
    void updateMenusEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000007");
        given(adminService.update(eq("menus"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 Menus", "status", "HIDDEN", "targetTable", "menu"));
        mvc.perform(put("/api/admin/menus/10000000-0000-0000-0000-000000000007").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Menus\",\"changeReason\":\"계약 변경\",\"displayStatus\":\"HIDDEN\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/menus/10000000-0000-0000-0000-000000000007").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/menus/10000000-0000-0000-0000-000000000007").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 Menus\",\"changeReason\":\"계약 변경\",\"displayStatus\":\"HIDDEN\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 Menus"))
            .andExpect(jsonPath("$.data.targetTable").value("menu"));
        verify(adminService).update(eq("menus"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteMenusEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000007");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("menus"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/menus/10000000-0000-0000-0000-000000000007").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("menus"), eq(id), any());
        mvc.perform(delete("/api/admin/menus/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/menus/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/menus/10000000-0000-0000-0000-000000000007").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("menus", id, "계약 삭제");
    }

    @Test
    void createCodeGroupsEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000008");
        given(adminService.create(eq("code-groups"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 CodeGroups", "status", "ENABLED", "targetTable", "code_group"));
        mvc.perform(post("/api/admin/code-groups").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 CodeGroups\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/code-groups").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/code-groups").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 CodeGroups\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 CodeGroups"))
            .andExpect(jsonPath("$.data.targetTable").value("code_group"));
        verify(adminService).create(eq("code-groups"), any(AdminItem.class));
    }

    @Test
    void updateCodeGroupsEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000008");
        given(adminService.update(eq("code-groups"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 CodeGroups", "status", "DISABLED", "targetTable", "code_group"));
        mvc.perform(put("/api/admin/code-groups/10000000-0000-0000-0000-000000000008").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 CodeGroups\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/code-groups/10000000-0000-0000-0000-000000000008").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/code-groups/10000000-0000-0000-0000-000000000008").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 CodeGroups\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 CodeGroups"))
            .andExpect(jsonPath("$.data.targetTable").value("code_group"));
        verify(adminService).update(eq("code-groups"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteCodeGroupsEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000008");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("code-groups"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/code-groups/10000000-0000-0000-0000-000000000008").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("code-groups"), eq(id), any());
        mvc.perform(delete("/api/admin/code-groups/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/code-groups/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/code-groups/10000000-0000-0000-0000-000000000008").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("code-groups", id, "계약 삭제");
    }

    @Test
    void createDetailCodesEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000009");
        given(adminService.create(eq("detail-codes"), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "등록 DetailCodes", "status", "ENABLED", "targetTable", "detail_code"));
        mvc.perform(post("/api/admin/detail-codes").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 DetailCodes\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(post("/api/admin/detail-codes").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(post("/api/admin/detail-codes").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"등록 DetailCodes\",\"changeReason\":\"계약 변경\",\"useStatus\":\"ENABLED\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("등록 DetailCodes"))
            .andExpect(jsonPath("$.data.targetTable").value("detail_code"));
        verify(adminService).create(eq("detail-codes"), any(AdminItem.class));
    }

    @Test
    void updateDetailCodesEnforcesAuthBusinessHappySideEffectValidationAndStateTransitionContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000009");
        given(adminService.update(eq("detail-codes"), eq(id), any(AdminItem.class)))
            .willReturn(Map.of("id", id, "name", "수정 DetailCodes", "status", "DISABLED", "targetTable", "detail_code"));
        mvc.perform(put("/api/admin/detail-codes/10000000-0000-0000-0000-000000000009").header("X-Role", "SYSTEM_VIEWER").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 DetailCodes\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        mvc.perform(put("/api/admin/detail-codes/10000000-0000-0000-0000-000000000009").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"   \",\"changeReason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.errors[0].field").value("name"));
        mvc.perform(put("/api/admin/detail-codes/10000000-0000-0000-0000-000000000009").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"수정 DetailCodes\",\"changeReason\":\"계약 변경\",\"useStatus\":\"DISABLED\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("수정 DetailCodes"))
            .andExpect(jsonPath("$.data.targetTable").value("detail_code"));
        verify(adminService).update(eq("detail-codes"), eq(id), any(AdminItem.class));
    }

    @Test
    void deleteDetailCodesEnforcesAuthBusinessHappySideEffectAndValidationContract() throws Exception {
        UUID id = UUID.fromString("10000000-0000-0000-0000-000000000009");
        UUID referencedId = UUID.fromString("30000000-0000-0000-0000-000000000001");
        doThrow(new BusinessException("참조 중인 항목은 삭제할 수 없습니다")).when(adminService).delete(eq("detail-codes"), eq(referencedId), isNull());
        mvc.perform(delete("/api/admin/detail-codes/10000000-0000-0000-0000-000000000009").header("X-Role", "SYSTEM_VIEWER"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
        verify(adminService, never()).delete(eq("detail-codes"), eq(id), any());
        mvc.perform(delete("/api/admin/detail-codes/not-a-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.error.errors[0].field").value("id"));
        mvc.perform(delete("/api/admin/detail-codes/30000000-0000-0000-0000-000000000001"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"));
        mvc.perform(delete("/api/admin/detail-codes/10000000-0000-0000-0000-000000000009").param("reason", "계약 삭제"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.deleted").value(true));
        verify(adminService).delete("detail-codes", id, "계약 삭제");
    }
}
