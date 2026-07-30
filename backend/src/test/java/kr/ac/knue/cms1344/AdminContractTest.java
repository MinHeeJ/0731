package kr.ac.knue.cms1344;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import kr.ac.knue.cms1344.persistence.AdminMapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")
@AutoConfigureMockMvc
class AdminContractTest {
  @Autowired MockMvc mockMvc;
  @MockBean AdminMapper mapper;

  @Test
  void openApiFixtureIsAvailableFromClasspath() throws Exception {
    ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
    assert resource.exists();
    assert new String(resource.getInputStream().readAllBytes()).contains("operationId: login");
  }

  @Test
  void getHealthReturnsSuccessEnvelopeBodyContract() throws Exception {
    mockMvc.perform(get("/api/health"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success", is(true)))
      .andExpect(jsonPath("$.data.status", is("UP")))
      .andExpect(jsonPath("$.data.service", is("cms1344-common-foundation")));
  }

  @Test
  void postAuthLoginCreatesHttpOnlySameSiteSessionTouchesLastLoginAndReturnsPortalMenu() throws Exception {
    when(mapper.canLogin(eq("admin"), anyString())).thenReturn(1);
    when(mapper.userByLogin("admin")).thenReturn(adminUser());
    when(mapper.permissionMatrix("ROLE", "R09")).thenReturn(List.of(Map.of("url", "/system/users", "accessAllowedYn", "Y")));

    mockMvc.perform(post("/api/auth/login").contentType("application/json").content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
      .andExpect(status().isOk())
      .andExpect(cookie().exists("SESSION"))
      .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
      .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
      .andExpect(jsonPath("$.success", is(true)))
      .andExpect(jsonPath("$.data.user.roleCodes[0]", is("R09")))
      .andExpect(jsonPath("$.data.menuRoutes[0]", is("/system/users")));

    verify(mapper).insertSession(anyString(), eq("U-ADMIN"));
    verify(mapper).touchLogin("U-ADMIN");
  }

  @Test
  void mapperSqlAliasesRuntimeMapKeysToCamelCaseForLoginSessionAndMenuFlow() throws Exception {
    String loginSql = String.join(" ", AdminMapper.class.getMethod("userByLogin", String.class).getAnnotation(Select.class).value());
    String userByIdSql = String.join(" ", AdminMapper.class.getMethod("userById", String.class).getAnnotation(Select.class).value());
    String activeSessionSql = String.join(" ", AdminMapper.class.getMethod("activeSession", String.class).getAnnotation(Select.class).value());
    String permissionSql = String.join(" ", AdminMapper.class.getMethod("permissionMatrix", String.class, String.class).getAnnotation(Select.class).value());

    assertTrue(loginSql.contains("u.user_id as \"userId\""));
    assertTrue(loginSql.contains("u.login_id as \"loginId\""));
    assertTrue(loginSql.contains("u.system_use_yn as \"systemUseYn\""));
    assertTrue(loginSql.contains("array_agg(ur.role_code order by ur.role_code) as \"roleCodes\""));
    assertTrue(userByIdSql.contains("u.user_id as \"userId\""));
    assertTrue(activeSessionSql.contains("s.session_id as \"sessionId\""));
    assertTrue(activeSessionSql.contains("s.user_id as \"userId\""));
    assertTrue(permissionSql.contains("mp.access_allowed_yn,'N') as \"accessAllowedYn\""));
  }

  @Test
  void postAuthLoginRejectsBlankPasswordWithFieldErrorAndNoSessionSideEffect() throws Exception {
    mockMvc.perform(post("/api/auth/login").contentType("application/json").content("{\"loginId\":\"admin\",\"password\":\"\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.success", is(false)))
      .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")))
      .andExpect(jsonPath("$.error.fieldErrors[0].field", is("password")));

    verify(mapper, never()).insertSession(anyString(), anyString());
  }

  @Test
  void getAuthMeRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.success", is(false)))
      .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
  }

  @Test
  void getAuthMeReturnsCurrentUserAndPortalFilteredMenuRoutes() throws Exception {
    authenticatedAsAdmin();
    when(mapper.permissionMatrix("ROLE", "R09")).thenReturn(List.of(
      Map.of("url", "/system/users", "accessAllowedYn", "Y"),
      Map.of("url", "/system/hidden", "accessAllowedYn", "N")));

    mockMvc.perform(get("/api/auth/me").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success", is(true)))
      .andExpect(jsonPath("$.data.user.userId", is("U-ADMIN")))
      .andExpect(jsonPath("$.data.menuRoutes[0]", is("/system/users")));
  }

  @Test
  void postAuthLogoutExpiresAuthenticatedSessionAndCookie() throws Exception {
    when(mapper.activeSession("S-1")).thenReturn(Map.of("sessionId", "S-1", "userId", "U-ADMIN", "status", "AUTHENTICATED"));

    mockMvc.perform(post("/api/auth/logout").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
      .andExpect(jsonPath("$.success", is(true)))
      .andExpect(jsonPath("$.data.loggedOut", is(true)));

    verify(mapper).expireSession("S-1");
  }

  @Test
  void postAuthLogoutRejectsAnonymousAndDoesNotInvalidateAnySession() throws Exception {
    mockMvc.perform(post("/api/auth/logout"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));

    verify(mapper, never()).expireSession(anyString());
  }

  @Test
  void getUsersRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/users"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
  }

  @Test
  void getUsersReturnsDbBackedItemsEnvelope() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listUsers(any())).thenReturn(List.of(Map.of("userId", "U-FACULTY", "personName", "교수", "useYn", "Y")));

    mockMvc.perform(get("/api/users").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success", is(true)))
      .andExpect(jsonPath("$.data.items[0].userId", is("U-FACULTY")))
      .andExpect(jsonPath("$.data.items[0].useYn", is("Y")));
  }

  @Test
  void getUsersWithIdReturnsDetailOrNotFound() throws Exception {
    authenticatedAsAdmin();
    when(mapper.getUser("U-FACULTY")).thenReturn(Map.of("userId", "U-FACULTY", "roles", List.of(Map.of("roleCode", "R04"))));

    mockMvc.perform(get("/api/users/U-FACULTY").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.userId", is("U-FACULTY")))
      .andExpect(jsonPath("$.data.roles[0].roleCode", is("R04")));
  }

  @Test
  void getUsersWithIdReturnsNotFoundEnvelopeWhenMissing() throws Exception {
    authenticatedAsAdmin();
    when(mapper.getUser("U-MISSING")).thenReturn(null);

    mockMvc.perform(get("/api/users/U-MISSING").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
  }

  @Test
  void patchUsersUsageRequiresR09AndRecordsBeforeAfterChangeHistoryStateTransition() throws Exception {
    authenticatedAsAdmin();
    Map<String,Object> before = Map.of("userId", "U-FACULTY", "useYn", "Y", "status", "ACTIVE");
    Map<String,Object> after = Map.of("userId", "U-FACULTY", "useYn", "N", "status", "INACTIVE");
    when(mapper.getUser("U-FACULTY")).thenReturn(before, after);
    when(mapper.updateUserUsage("U-FACULTY", "N", "U-ADMIN")).thenReturn(1);

    mockMvc.perform(patch("/api/users/U-FACULTY/usage").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"useYn\":\"N\",\"changeReason\":\"휴직\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success", is(true)))
      .andExpect(jsonPath("$.data.useYn", is("N")))
      .andExpect(jsonPath("$.data.status", is("INACTIVE")));

    verify(mapper).updateUserUsage("U-FACULTY", "N", "U-ADMIN");
    verify(mapper).history(anyString(), eq("internal_user"), eq("U-FACULTY"), eq("UPDATE_USAGE"), contains("ACTIVE"), contains("INACTIVE"), eq("U-ADMIN"), eq("휴직"));
  }

  @Test
  void patchUsersUsageRejectsInvalidUseYnBeforeDbWrite() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(patch("/api/users/U-FACULTY/usage").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"useYn\":\"X\",\"changeReason\":\"오입력\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

    verify(mapper, never()).updateUserUsage(anyString(), anyString(), anyString());
  }

  @Test
  void putUsersRolesRevokesAndInsertsActiveRolesWithHistory() throws Exception {
    authenticatedAsAdmin();
    Map<String,Object> before = Map.of("userId", "U-FACULTY", "roles", List.of(Map.of("roleCode", "R01", "status", "ACTIVE")));
    Map<String,Object> after = Map.of("userId", "U-FACULTY", "roles", List.of(Map.of("roleCode", "R04", "status", "ACTIVE")));
    when(mapper.getUser("U-FACULTY")).thenReturn(before, after);

    mockMvc.perform(put("/api/users/U-FACULTY/roles").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"roles\":[{\"roleCode\":\"R04\",\"assignmentType\":\"MANUAL\",\"approvedBy\":\"U-ADMIN\",\"validFrom\":\"2026-01-01\"}],\"changeReason\":\"보직 변경\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.roles[0].roleCode", is("R04")));

    verify(mapper).revokeActiveRolesForUser("U-FACULTY", "U-ADMIN", "보직 변경");
    verify(mapper).insertUserRole(anyString(), eq("U-FACULTY"), eq("R04"), eq("MANUAL"), eq("U-ADMIN"), any(), eq(null), eq("보직 변경"), eq("U-ADMIN"));
    verify(mapper).history(anyString(), eq("user_role"), eq("U-FACULTY"), eq("REPLACE"), contains("R01"), contains("R04"), eq("U-ADMIN"), eq("보직 변경"));
  }

  @Test
  void putUsersRolesRejectsEmptyRolesWithNoRoleSideEffect() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(put("/api/users/U-FACULTY/roles").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"roles\":[],\"changeReason\":\"누락\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fieldErrors[0].field", is("roles")));

    verify(mapper, never()).revokeActiveRolesForUser(anyString(), anyString(), anyString());
  }

  @Test
  void getOrganizationsReturnsItemsFromPersistenceWithPortalFiltering() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listOrganizations(any())).thenReturn(List.of(Map.of("orgCode", "KNUE", "orgName", "한국교원대", "useYn", "Y")));

    mockMvc.perform(get("/api/organizations").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].orgCode", is("KNUE")))
      .andExpect(jsonPath("$.data.items[0].useYn", is("Y")));
  }

  @Test
  void getOrganizationsTreeReturnsTreeRows() throws Exception {
    authenticatedAsAdmin();
    when(mapper.orgTreeRows()).thenReturn(List.of(Map.of("orgCode", "KNUE", "parentOrgCode", "ROOT")));

    mockMvc.perform(get("/api/organizations/tree").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].parentOrgCode", is("ROOT")));
  }

  @Test
  void putOrganizationsRelationsValidatesCycleThenPersistsRelationAndHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.existsOrganization("DEP-COMPUTER")).thenReturn(1);
    when(mapper.existsOrganization("COL-EDU")).thenReturn(1);

    mockMvc.perform(put("/api/organizations/DEP-COMPUTER/relations").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"parentOrgCode\":\"COL-EDU\",\"effectiveStartDate\":\"2026-03-01\",\"changeReason\":\"조직개편\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.orgCode", is("DEP-COMPUTER")))
      .andExpect(jsonPath("$.data.parentOrgCode", is("COL-EDU")));

    verify(mapper).insertOrgRelation(anyString(), eq("DEP-COMPUTER"), eq("COL-EDU"), any(), eq(null), eq("조직개편"), eq("U-ADMIN"));
    verify(mapper).history(anyString(), eq("organization_relation_history"), eq("DEP-COMPUTER"), eq("UPSERT"), anyString(), contains("COL-EDU"), eq("U-ADMIN"), eq("조직개편"));
  }

  @Test
  void putOrganizationsRelationsRejectsSelfParentWithoutInsert() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(put("/api/organizations/DEP-COMPUTER/relations").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"parentOrgCode\":\"DEP-COMPUTER\",\"effectiveStartDate\":\"2026-03-01\",\"changeReason\":\"순환\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

    verify(mapper, never()).insertOrgRelation(anyString(), anyString(), anyString(), any(), any(), anyString(), anyString());
  }

  @Test
  void getRolesReturnsRolePolicies() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listRoles(any())).thenReturn(List.of(Map.of("roleCode", "R09", "defaultDataScope", "ALL")));

    mockMvc.perform(get("/api/roles").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].roleCode", is("R09")))
      .andExpect(jsonPath("$.data.items[0].defaultDataScope", is("ALL")));
  }

  @Test
  void putRolesRequiresAdminAndStoresAssignmentCriteriaDefaultDataScopeHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listRoles(any())).thenReturn(List.of(Map.of("roleCode", "R04", "assignmentCriteria", "담당자", "defaultDataScope", "ORG")));

    mockMvc.perform(put("/api/roles").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"roleCode\":\"R04\",\"roleName\":\"부서관리자\",\"purpose\":\"부서 관리\",\"assignmentCriteria\":\"담당자\",\"defaultDataScope\":\"ORG\",\"useYn\":\"Y\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.assignmentCriteria", is("담당자")))
      .andExpect(jsonPath("$.data.defaultDataScope", is("ORG")));

    verify(mapper).updateRole("R04", "부서관리자", "부서 관리", "담당자", "ORG", "Y", "U-ADMIN");
    verify(mapper).history(anyString(), eq("role"), eq("R04"), eq("UPSERT"), anyString(), contains("defaultDataScope"), eq("U-ADMIN"), eq("역할 정책 저장"));
  }

  @Test
  void putRolesRejectsNonAdminWithForbiddenAndNoUpdate() throws Exception {
    authenticatedAsFaculty();

    mockMvc.perform(put("/api/roles").cookie(new Cookie("SESSION", "S-2")).contentType("application/json").content("{\"roleCode\":\"R04\",\"purpose\":\"관리\",\"assignmentCriteria\":\"담당자\",\"defaultDataScope\":\"ALL\"}"))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));

    verify(mapper, never()).updateRole(anyString(), any(), anyString(), anyString(), anyString(), any(), anyString());
  }

  @Test
  void getUserRolesReturnsAssignmentRows() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listUserRoles(any())).thenReturn(List.of(Map.of("userRoleId", "UR-ADMIN-R09", "status", "ACTIVE")));

    mockMvc.perform(get("/api/user-roles").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].userRoleId", is("UR-ADMIN-R09")))
      .andExpect(jsonPath("$.data.items[0].status", is("ACTIVE")));
  }

  @Test
  void postUserRolesValidatesBusinessDuplicateThenCreatesActiveRoleAndChangeHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.getUser("U-FACULTY")).thenReturn(Map.of("userId", "U-FACULTY"));
    when(mapper.duplicateUserRole(eq("U-FACULTY"), eq("R04"), any(), eq(null))).thenReturn(0);

    mockMvc.perform(post("/api/user-roles").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"userId\":\"U-FACULTY\",\"roleCode\":\"R04\",\"assignmentType\":\"MANUAL\",\"approvedBy\":\"U-ADMIN\",\"validFrom\":\"2026-01-01\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.status", is("ACTIVE")))
      .andExpect(jsonPath("$.data.assignmentId").exists());

    verify(mapper).insertUserRole(anyString(), eq("U-FACULTY"), eq("R04"), eq("MANUAL"), eq("U-ADMIN"), any(), eq(null), eq("역할 부여"), eq("U-ADMIN"));
    verify(mapper).history(anyString(), eq("user_role"), anyString(), eq("ASSIGN"), anyString(), contains("assignmentId"), eq("U-ADMIN"), eq("역할 부여"));
  }

  @Test
  void postUserRolesRejectsDuplicateWithoutInsert() throws Exception {
    authenticatedAsAdmin();
    when(mapper.getUser("U-FACULTY")).thenReturn(Map.of("userId", "U-FACULTY"));
    when(mapper.duplicateUserRole(eq("U-FACULTY"), eq("R04"), any(), eq(null))).thenReturn(1);

    mockMvc.perform(post("/api/user-roles").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"userId\":\"U-FACULTY\",\"roleCode\":\"R04\",\"assignmentType\":\"MANUAL\",\"approvedBy\":\"U-ADMIN\",\"validFrom\":\"2026-01-01\"}"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.error.code", is("CONFLICT")));

    verify(mapper, never()).insertUserRole(anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), anyString(), anyString());
  }

  @Test
  void deleteUserRolesRevokesActiveAssignmentAndRecordsRevokedStateHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.revokeAssignment("UR-ADMIN-R09", "U-ADMIN", "권한 회수")).thenReturn(1);

    mockMvc.perform(delete("/api/user-roles/UR-ADMIN-R09").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"changeReason\":\"권한 회수\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.assignmentId", is("UR-ADMIN-R09")))
      .andExpect(jsonPath("$.data.status", is("REVOKED")));

    verify(mapper).revokeAssignment("UR-ADMIN-R09", "U-ADMIN", "권한 회수");
    verify(mapper).history(anyString(), eq("user_role"), eq("UR-ADMIN-R09"), eq("REVOKE"), anyString(), contains("REVOKED"), eq("U-ADMIN"), eq("권한 회수"));
  }

  @Test
  void deleteUserRolesRejectsMissingChangeReasonWithoutRevoke() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(delete("/api/user-roles/UR-ADMIN-R09").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"changeReason\":\"\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fieldErrors[0].field", is("changeReason")));

    verify(mapper, never()).revokeAssignment(anyString(), anyString(), anyString());
  }

  @Test
  void getMenuPermissionsMatrixRequiresValidTargetAndReturnsAuthorizationRows() throws Exception {
    authenticatedAsAdmin();
    when(mapper.permissionMatrix("ROLE", "R09")).thenReturn(List.of(Map.of("menuId", "M-USR", "accessAllowedYn", "Y")));

    mockMvc.perform(get("/api/menu-permissions/matrix?targetType=ROLE&targetId=R09").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].menuId", is("M-USR")))
      .andExpect(jsonPath("$.data.items[0].accessAllowedYn", is("Y")));
  }

  @Test
  void putMenuPermissionsMatrixUpsertsBackendAuthorizationAndRecordsBeforeAfterHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.existsMenu("M-USR")).thenReturn(1);
    when(mapper.permissionMatrix("ROLE", "R09")).thenReturn(List.of(Map.of("menuId", "M-USR", "accessAllowedYn", "Y")));

    mockMvc.perform(put("/api/menu-permissions/matrix").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"permissions\":[{\"menuId\":\"M-USR\",\"accessAllowedYn\":\"Y\",\"functionPermissions\":[\"READ\"]}]}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].accessAllowedYn", is("Y")));

    verify(mapper).upsertPermission(anyString(), eq("ROLE"), eq("R09"), eq("M-USR"), eq("Y"), contains("READ"), eq("U-ADMIN"));
    verify(mapper).history(anyString(), eq("menu_permission"), eq("ROLE:R09"), eq("UPSERT"), anyString(), contains("M-USR"), eq("U-ADMIN"), eq("메뉴 권한 저장"));
  }

  @Test
  void putMenuPermissionsMatrixRejectsUnknownMenuBeforeAuthorizationWrite() throws Exception {
    authenticatedAsAdmin();
    when(mapper.existsMenu("M-MISSING")).thenReturn(0);

    mockMvc.perform(put("/api/menu-permissions/matrix").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"permissions\":[{\"menuId\":\"M-MISSING\",\"accessAllowedYn\":\"Y\"}]}"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));

    verify(mapper, never()).upsertPermission(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void getMenusTreeReturnsMenuRows() throws Exception {
    authenticatedAsAdmin();
    when(mapper.menuTreeRows()).thenReturn(List.of(Map.of("menuId", "M-USR", "displayOrder", 1)));

    mockMvc.perform(get("/api/menus/tree").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].menuId", is("M-USR")));
  }

  @Test
  void patchMenusMoveUpdatesDisplayOrderAndMenuParentWithHistory() throws Exception {
    authenticatedAsAdmin();
    Map<String,Object> before = Map.of("menuId", "M-CHILD", "menuLevel", 2, "displayOrder", 3);
    Map<String,Object> parent = Map.of("menuId", "M-PARENT", "menuLevel", 1);
    Map<String,Object> after = Map.of("menuId", "M-CHILD", "parentMenuId", "M-PARENT", "menuLevel", 2, "displayOrder", 1);
    when(mapper.menu("M-CHILD")).thenReturn(before, after, after);
    when(mapper.menu("M-PARENT")).thenReturn(parent);

    mockMvc.perform(patch("/api/menus/M-CHILD/move").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"parentMenuId\":\"M-PARENT\",\"displayOrder\":1,\"changeReason\":\"순서 변경\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.parentMenuId", is("M-PARENT")))
      .andExpect(jsonPath("$.data.displayOrder", is(1)));

    verify(mapper).moveMenu("M-CHILD", "M-PARENT", 2, 1, "U-ADMIN");
    verify(mapper).history(anyString(), eq("menu"), eq("M-CHILD"), eq("MOVE"), contains("displayOrder"), contains("M-PARENT"), eq("U-ADMIN"), eq("순서 변경"));
  }

  @Test
  void patchMenusMoveRejectsCycleWithoutMenuWrite() throws Exception {
    authenticatedAsAdmin();
    when(mapper.menu("M-CHILD")).thenReturn(Map.of("menuId", "M-CHILD", "menuLevel", 2));

    mockMvc.perform(patch("/api/menus/M-CHILD/move").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"parentMenuId\":\"M-CHILD\",\"displayOrder\":1,\"changeReason\":\"순환\"}"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.error.code", is("CONFLICT")));

    verify(mapper, never()).moveMenu(anyString(), anyString(), anyInt(), anyInt(), anyString());
  }

  @Test
  void patchMenusReorderUpdatesEachDisplayOrderAndCacheRelatedHistory() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(patch("/api/menus/reorder").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"parentMenuId\":\"M-PARENT\",\"orderedMenuIds\":[\"M-A\",\"M-B\"],\"changeReason\":\"표시순서 조정\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.orderedMenuIds[0]", is("M-A")))
      .andExpect(jsonPath("$.data.orderedMenuIds[1]", is("M-B")));

    verify(mapper).updateMenuOrder("M-A", 1, "U-ADMIN");
    verify(mapper).updateMenuOrder("M-B", 2, "U-ADMIN");
    verify(mapper).history(anyString(), eq("menu"), eq("M-PARENT"), eq("REORDER"), anyString(), contains("orderedMenuIds"), eq("U-ADMIN"), eq("표시순서 조정"));
  }

  @Test
  void patchMenusReorderRejectsEmptyOrderWithoutDisplayOrderWrite() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(patch("/api/menus/reorder").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"parentMenuId\":\"M-PARENT\",\"orderedMenuIds\":[],\"changeReason\":\"누락\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fieldErrors[0].field", is("orderedMenuIds")));

    verify(mapper, never()).updateMenuOrder(anyString(), anyInt(), anyString());
  }

  @Test
  void getMenuInfoReturnsLifecycleRows() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listMenuInfo(any())).thenReturn(List.of(Map.of("menuId", "M-USR", "useYn", "Y")));

    mockMvc.perform(get("/api/menu-info").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].menuId", is("M-USR")))
      .andExpect(jsonPath("$.data.items[0].useYn", is("Y")));
  }

  @Test
  void putMenuInfoUpsertsLifecycleMenuAndChangeHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.duplicateActiveScreen("SCR-USR", "M-USR")).thenReturn(0);
    when(mapper.menu("M-USR")).thenReturn(Map.of("menuId", "M-USR", "menuName", "사용자", "useYn", "Y"));

    mockMvc.perform(put("/api/menu-info").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"menuId\":\"M-USR\",\"menuName\":\"사용자\",\"screenId\":\"SCR-USR\",\"url\":\"/system/users\",\"useYn\":\"Y\",\"changeReason\":\"메뉴 정비\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.menuId", is("M-USR")))
      .andExpect(jsonPath("$.data.useYn", is("Y")));

    verify(mapper).upsertMenuInfo("M-USR", "사용자", "SCR-USR", "/system/users", null, null, null, "Y", "U-ADMIN");
    verify(mapper).history(anyString(), eq("menu"), eq("M-USR"), eq("UPSERT_INFO"), anyString(), contains("사용자"), eq("U-ADMIN"), eq("메뉴 정비"));
  }

  @Test
  void putMenuInfoRejectsInvalidUrlWithoutLifecycleWrite() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(put("/api/menu-info").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"menuId\":\"M-USR\",\"menuName\":\"사용자\",\"screenId\":\"SCR-USR\",\"url\":\"/external\",\"useYn\":\"Y\",\"changeReason\":\"오류\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

    verify(mapper, never()).upsertMenuInfo(anyString(), anyString(), anyString(), anyString(), any(), any(), any(), anyString(), anyString());
  }

  @Test
  void getCodeGroupsReturnsLifecycleRows() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listCodeGroups(any())).thenReturn(List.of(Map.of("groupId", "USE_YN", "useYn", "Y")));

    mockMvc.perform(get("/api/code-groups").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].groupId", is("USE_YN")))
      .andExpect(jsonPath("$.data.items[0].useYn", is("Y")));
  }

  @Test
  void putCodeGroupsUpsertsLifecycleAndHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listCodeGroups(any())).thenReturn(List.of(Map.of("groupId", "USE_YN", "useYn", "Y")));

    mockMvc.perform(put("/api/code-groups").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"groupId\":\"USE_YN\",\"groupName\":\"사용여부\",\"managingDepartment\":\"SYS\",\"useYn\":\"Y\",\"changeReason\":\"코드 정비\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.groupId", is("USE_YN")))
      .andExpect(jsonPath("$.data.useYn", is("Y")));

    verify(mapper).upsertCodeGroup("USE_YN", "사용여부", null, "SYS", "Y", "U-ADMIN");
    verify(mapper).history(anyString(), eq("code_group"), eq("USE_YN"), eq("UPSERT"), anyString(), contains("USE_YN"), eq("U-ADMIN"), eq("코드 정비"));
  }

  @Test
  void putCodeGroupsRejectsInvalidLifecycleValueBeforeWrite() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(put("/api/code-groups").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"groupId\":\"USE_YN\",\"groupName\":\"사용여부\",\"managingDepartment\":\"SYS\",\"useYn\":\"X\",\"changeReason\":\"오류\"}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

    verify(mapper, never()).upsertCodeGroup(anyString(), anyString(), any(), anyString(), anyString(), anyString());
  }

  @Test
  void getCodeDetailsRequiresGroupIdValidation() throws Exception {
    authenticatedAsAdmin();

    mockMvc.perform(get("/api/code-details").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error.fieldErrors[0].field", is("groupId")));
  }

  @Test
  void getCodeDetailsReturnsGroupScopedRows() throws Exception {
    authenticatedAsAdmin();
    when(mapper.listCodeDetails(any())).thenReturn(List.of(Map.of("groupId", "USE_YN", "codeValue", "Y")));

    mockMvc.perform(get("/api/code-details?groupId=USE_YN").cookie(new Cookie("SESSION", "S-1")))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.items[0].codeValue", is("Y")));
  }

  @Test
  void putCodeDetailsUpsertsLifecycleAndHistory() throws Exception {
    authenticatedAsAdmin();
    when(mapper.existsCodeGroup("USE_YN")).thenReturn(1);
    when(mapper.listCodeDetails(any())).thenReturn(List.of(Map.of("groupId", "USE_YN", "codeValue", "Y", "useYn", "Y")));

    mockMvc.perform(put("/api/code-details").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"groupId\":\"USE_YN\",\"codeValue\":\"Y\",\"codeName\":\"예\",\"displayOrder\":1,\"extraAttributes\":{\"color\":\"green\"},\"useYn\":\"Y\",\"changeReason\":\"상세코드 정비\"}"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.codeValue", is("Y")))
      .andExpect(jsonPath("$.data.useYn", is("Y")));

    verify(mapper).upsertCodeDetail(eq("USE_YN"), eq("Y"), eq("예"), eq(null), eq(1), contains("green"), eq("Y"), eq(null), eq(null), eq("U-ADMIN"));
    verify(mapper).history(anyString(), eq("code_detail"), eq("USE_YN:Y"), eq("UPSERT"), anyString(), contains("codeValue"), eq("U-ADMIN"), eq("상세코드 정비"));
  }

  @Test
  void putCodeDetailsRejectsMissingGroupBeforeCodeDetailWrite() throws Exception {
    authenticatedAsAdmin();
    when(mapper.existsCodeGroup("MISSING")).thenReturn(0);

    mockMvc.perform(put("/api/code-details").cookie(new Cookie("SESSION", "S-1")).contentType("application/json").content("{\"groupId\":\"MISSING\",\"codeValue\":\"Y\",\"codeName\":\"예\",\"displayOrder\":1,\"useYn\":\"Y\",\"changeReason\":\"오류\"}"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));

    verify(mapper, never()).upsertCodeDetail(anyString(), anyString(), anyString(), any(), any(), anyString(), anyString(), any(), any(), anyString());
  }

  private void authenticatedAsAdmin() {
    when(mapper.activeSession("S-1")).thenReturn(Map.of("sessionId", "S-1", "userId", "U-ADMIN", "status", "AUTHENTICATED"));
    when(mapper.userById("U-ADMIN")).thenReturn(adminUser());
    when(mapper.isAdmin("U-ADMIN")).thenReturn(1);
  }

  private void authenticatedAsFaculty() {
    when(mapper.activeSession("S-2")).thenReturn(Map.of("sessionId", "S-2", "userId", "U-FACULTY", "status", "AUTHENTICATED"));
    when(mapper.userById("U-FACULTY")).thenReturn(Map.of("userId", "U-FACULTY", "loginId", "faculty", "roleCodes", List.of("R01")));
    when(mapper.isAdmin("U-FACULTY")).thenReturn(0);
  }

  private Map<String,Object> adminUser() {
    return Map.of("userId", "U-ADMIN", "loginId", "admin", "personName", "관리자", "roleCodes", List.of("R09"));
  }
}
