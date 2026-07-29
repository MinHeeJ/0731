package kr.ac.knue.common;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:knue_common;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;INIT=CREATE DOMAIN IF NOT EXISTS JSONB AS JSON",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver"
})
@AutoConfigureMockMvc
class CommonApiContractTest {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void openApiFixtureIsAvailableOnClasspath() throws Exception {
    ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
    assertThat(resource.exists()).isTrue();
    assertThat(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("operationId: searchUsers");
  }

  @Test
  void loginHealthAndAdminMenuContextExposeSeedAdministrator() throws Exception {
    MvcResult login = mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.userId").value("admin"))
        .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"))
        .andReturn();
    String cookie = login.getResponse().getHeader("Set-Cookie");
    assertThat(cookie).contains("KNUESESSION").contains("HttpOnly").contains("SameSite=Lax");
    mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("UP"));
    mvc.perform(get("/api/auth/me").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.userId").value("admin"))
        .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));
    mvc.perform(get("/api/admin/me").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"))
        .andExpect(jsonPath("$.data.menus.length()").value(14));
  }

  @Test
  void logoutRequiresSessionAndPersistsLoggedOutState() throws Exception {
    String cookie = loginCookie("admin", "admin");
    String sessionId = sessionId(cookie);
    mvc.perform(post("/api/auth/logout").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isOk())
        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
        .andExpect(jsonPath("$.data.status").value("LOGGED_OUT"));
    assertThat(jdbc.queryForObject("select status from session where session_id=?", String.class, sessionId)).isEqualTo("LOGGED_OUT");
    mvc.perform(get("/api/auth/me").cookie(sessionCookie(cookie)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
  }

  @Test
  void userManagementSearchAndAccessUpdatePersistWithChangeReason() throws Exception {
    String cookie = loginCookie("admin", "admin");
    long beforeHistory = historyCount("user_account", "prof1001");
    mvc.perform(get("/api/admin/users").param("name", "김").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].positionName").exists())
        .andExpect(jsonPath("$.data[0].lastSyncedAt").exists());
    mvc.perform(patch("/api/admin/users/prof1001/access").cookie(sessionCookie(cookie))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"systemUseYn\":\"N\",\"changeReason\":\"접근 중지 테스트\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.systemUseYn").value("N"))
        .andExpect(jsonPath("$.data.status").value("DISABLED"));
    assertThat(jdbc.queryForObject("select system_use_yn from user_account where user_id='prof1001'", String.class)).isEqualTo("N");
    assertThat(historyCount("user_account", "prof1001")).isGreaterThan(beforeHistory);
    mvc.perform(patch("/api/admin/users/prof1001/access").cookie(sessionCookie(cookie))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"systemUseYn\":\"Y\",\"changeReason\":\"접근 복구 테스트\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.systemUseYn").value("Y"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    mvc.perform(patch("/api/admin/users/prof1001/access").cookie(sessionCookie(cookie))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"systemUseYn\":\"INVALID\",\"changeReason\":\"검증 실패\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
  }

  @Test
  void readOperationsExposePortalFilteredReferenceDataFromDatabase() throws Exception {
    String cookie = loginCookie("admin", "admin");
    mvc.perform(get("/api/admin/organizations").param("organizationType", "DEPARTMENT").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].organizationCode").value("DEPT_COMEDU"))
        .andExpect(jsonPath("$.data[0].parentOrganizationCode").exists());
    mvc.perform(get("/api/admin/roles").param("useYn", "Y").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].roleCode").exists())
        .andExpect(jsonPath("$.data[0].defaultDataScope").exists());
    mvc.perform(get("/api/admin/user-roles").param("userId", "admin").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].assignmentId").exists())
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    mvc.perform(get("/api/admin/menu-permissions").param("targetType", "ROLE").param("targetId", "R09").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].menuId").exists())
        .andExpect(jsonPath("$.data[0].accessYn").value("Y"));
    mvc.perform(get("/api/admin/menus/tree").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].menuId").value("SYS"));
    mvc.perform(get("/api/admin/menus").param("businessCategory", "시스템 관리").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].menuName").exists())
        .andExpect(jsonPath("$.data[0].useYn").value("Y"));
    mvc.perform(get("/api/admin/code-groups").param("useYn", "Y").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].groupId").exists())
        .andExpect(jsonPath("$.data[0].managingDepartment").exists());
    mvc.perform(get("/api/admin/code-groups/USER_STATUS/codes").cookie(sessionCookie(cookie)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].codeValue").value("ACTIVE"))
        .andExpect(jsonPath("$.data[0].validFrom").exists());
  }

  @Test
  void referenceDataWriteFlowsValidateReasonAndSupportSearchAgain() throws Exception {
    String cookie = loginCookie("admin", "admin");
    long roleHistoryBefore = historyCount("role", "R09");
    long groupHistoryBefore = historyCount("code_group", "USER_STATUS");
    long codeHistoryBefore = historyCount("common_code", "USER_STATUS:ACTIVE");
    mvc.perform(put("/api/admin/roles/R09").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"roleName\":\"시스템관리자\",\"purpose\":\"공통기능 관리\",\"grantCriteria\":\"관리자 지정\",\"defaultDataScope\":\"ALL\",\"useYn\":\"Y\",\"changeReason\":\"역할 계약 테스트\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roleCode").value("R09"))
        .andExpect(jsonPath("$.data.defaultDataScope").value("ALL"));
    assertThat(historyCount("role", "R09")).isGreaterThan(roleHistoryBefore);
    mvc.perform(put("/api/admin/code-groups/USER_STATUS").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"groupName\":\"사용자 상태\",\"description\":\"상태\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"N\",\"changeReason\":\"코드그룹 수정\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groupId").value("USER_STATUS"))
        .andExpect(jsonPath("$.data.useYn").value("N"));
    assertThat(historyCount("code_group", "USER_STATUS")).isGreaterThan(groupHistoryBefore);
    mvc.perform(put("/api/admin/code-groups/USER_STATUS/codes/ACTIVE").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"codeName\":\"활성\",\"sortOrder\":1,\"additionalAttributes\":{\"ui\":\"badge\"},\"validFrom\":\"2026-01-01\",\"useYn\":\"N\",\"changeReason\":\"상세코드 수정\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.codeValue").value("ACTIVE"))
        .andExpect(jsonPath("$.data.useYn").value("N"));
    assertThat(historyCount("common_code", "USER_STATUS:ACTIVE")).isGreaterThan(codeHistoryBefore);
    mvc.perform(put("/api/admin/code-groups/USER_STATUS").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"groupName\":\"사용자 상태\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
  }

  @Test
  void createOperationsPersistActiveRowsAndChangeHistory() throws Exception {
    String cookie = loginCookie("admin", "admin");
    mvc.perform(post("/api/admin/roles").param("roleCode", "R10").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"roleName\":\"계약테스트역할\",\"purpose\":\"계약 테스트\",\"grantCriteria\":\"테스트 승인\",\"defaultDataScope\":\"SELF\",\"useYn\":\"Y\",\"changeReason\":\"역할 생성\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roleCode").value("R10"))
        .andExpect(jsonPath("$.data.useYn").value("Y"));
    assertThat(historyCount("role", "R10")).isPositive();
    mvc.perform(post("/api/admin/code-groups").param("groupId", "CONTRACT_STATUS").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"groupName\":\"계약 상태\",\"description\":\"계약 테스트 코드그룹\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\",\"changeReason\":\"코드그룹 생성\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groupId").value("CONTRACT_STATUS"))
        .andExpect(jsonPath("$.data.useYn").value("Y"));
    mvc.perform(post("/api/admin/code-groups/CONTRACT_PATH_STATUS").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"groupName\":\"계약 상태 경로\",\"description\":\"경로 생성\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\",\"changeReason\":\"코드그룹 경로 생성\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groupId").value("CONTRACT_PATH_STATUS"));
    assertThat(historyCount("code_group", "CONTRACT_STATUS")).isPositive();
    assertThat(historyCount("code_group", "CONTRACT_PATH_STATUS")).isPositive();
    mvc.perform(post("/api/admin/code-groups/CONTRACT_STATUS/codes").param("codeValue", "OPEN").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"codeName\":\"진행\",\"sortOrder\":1,\"additionalAttributes\":{\"badge\":\"blue\"},\"validFrom\":\"2026-01-01\",\"useYn\":\"Y\",\"changeReason\":\"상세코드 생성\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.codeValue").value("OPEN"))
        .andExpect(jsonPath("$.data.useYn").value("Y"));
    mvc.perform(post("/api/admin/code-groups/CONTRACT_STATUS/codes/CLOSED").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"codeName\":\"종료\",\"sortOrder\":2,\"additionalAttributes\":{\"badge\":\"gray\"},\"validFrom\":\"2026-01-01\",\"useYn\":\"Y\",\"changeReason\":\"상세코드 경로 생성\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.codeValue").value("CLOSED"));
    assertThat(historyCount("common_code", "CONTRACT_STATUS:OPEN")).isPositive();
    assertThat(historyCount("common_code", "CONTRACT_STATUS:CLOSED")).isPositive();
  }

  @Test
  void menuOrganizationAndPermissionWritesPersistSideEffects() throws Exception {
    String cookie = loginCookie("admin", "admin");
    mvc.perform(post("/api/admin/menus").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"menuId\":\"MENU_CONTRACT\",\"parentMenuId\":\"SYS_MENU\",\"menuLevel\":3,\"menuName\":\"계약 메뉴\",\"screenId\":\"SCR-CONTRACT\",\"url\":\"/admin/contract\",\"icon\":\"test\",\"businessCategory\":\"시스템 관리\",\"description\":\"계약 테스트 메뉴\",\"displayOrder\":9,\"useYn\":\"Y\",\"changeReason\":\"메뉴 생성\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.menuId").value("MENU_CONTRACT"))
        .andExpect(jsonPath("$.data.useYn").value("Y"));
    assertThat(historyCount("menu", "MENU_CONTRACT")).isPositive();
    mvc.perform(put("/api/admin/menus/MENU_CONTRACT").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"parentMenuId\":\"SYS_MENU\",\"menuLevel\":3,\"menuName\":\"계약 메뉴 수정\",\"screenId\":\"SCR-CONTRACT\",\"url\":\"/admin/contract\",\"icon\":\"test\",\"businessCategory\":\"시스템 관리\",\"description\":\"계약 테스트 메뉴 수정\",\"displayOrder\":8,\"useYn\":\"N\",\"changeReason\":\"메뉴 수정\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.menuId").value("MENU_CONTRACT"))
        .andExpect(jsonPath("$.data.useYn").value("N"));
    mvc.perform(patch("/api/admin/menus/tree/reorder").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"parentMenuId\":\"SYS_MENU\",\"orderedMenuIds\":[\"MENU_TREE\",\"MENU_INFO\",\"MENU_CONTRACT\"],\"changeReason\":\"메뉴 순서 변경\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].menuId").exists());
    assertThat(jdbc.queryForObject("select display_order from menu where menu_id='MENU_TREE'", Integer.class)).isEqualTo(1);
    mvc.perform(put("/api/admin/menu-permissions").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"targetType\":\"USER\",\"targetId\":\"prof1001\",\"permissions\":[{\"menuId\":\"MENU_CONTRACT\",\"accessYn\":\"Y\"}],\"changeReason\":\"메뉴 권한 저장\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].menuId").value("MENU_CONTRACT"))
        .andExpect(jsonPath("$.data[0].accessYn").value("Y"));
    assertThat(historyCount("menu_permission", "USER:prof1001")).isPositive();
    mvc.perform(put("/api/admin/organizations/DEPT_COMEDU/relation").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"parentOrganizationCode\":\"KNUE\",\"effectiveStartDate\":\"2026-03-01\",\"changeReason\":\"조직 관계 변경\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.organizationCode").value("DEPT_COMEDU"))
        .andExpect(jsonPath("$.data.parentOrganizationCode").value("KNUE"));
    assertThat(historyCount("organization_relation_history", "DEPT_COMEDU")).isPositive();
    assertThat(jdbc.queryForObject("select count(*) from organization_relation_history where organization_code='DEPT_COMEDU' and effective_end_date is not null", Long.class)).isPositive();
  }

  @Test
  void userRoleAssignmentGrantUpdateReplaceAndRevokeTrackStateTransitions() throws Exception {
    String cookie = loginCookie("admin", "admin");
    mvc.perform(put("/api/admin/user-roles/1").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    mvc.perform(delete("/api/admin/user-roles/1").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    mvc.perform(post("/api/admin/user-roles").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"userId\":\"prof1001\",\"roleCode\":\"R04\",\"grantType\":\"MANUAL\",\"approverUserId\":\"admin\",\"validFrom\":\"2026-02-01\",\"validTo\":\"2026-12-31\",\"changeReason\":\"사용자 역할 부여\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.userId").value("prof1001"))
        .andExpect(jsonPath("$.data.roleCode").value("R04"))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    Long assignmentId = jdbc.queryForObject("select assignment_id from user_role_assignment where user_id='prof1001' and role_code='R04' and status='ACTIVE' order by assignment_id desc limit 1", Long.class);
    assertThat(historyCount("user_role_assignment", "prof1001")).isPositive();
    mvc.perform(put("/api/admin/user-roles/" + assignmentId).cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"roleCode\":\"R04\",\"grantType\":\"MANUAL\",\"approverUserId\":\"admin\",\"validFrom\":\"2026-02-01\",\"validTo\":\"2026-11-30\",\"changeReason\":\"사용자 역할 기간 변경\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.assignmentId").value(assignmentId.intValue()))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    assertThat(historyCount("user_role_assignment", String.valueOf(assignmentId))).isPositive();
    mvc.perform(delete("/api/admin/user-roles/" + assignmentId).cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"changeReason\":\"사용자 역할 회수\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.assignmentId").value(assignmentId.intValue()))
        .andExpect(jsonPath("$.data.status").value("REVOKED"));
    assertThat(jdbc.queryForObject("select status from user_role_assignment where assignment_id=?", String.class, assignmentId)).isEqualTo("REVOKED");
    mvc.perform(put("/api/admin/users/prof1001/roles").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"roleCodes\":[\"R01\",\"R02\"],\"approverUserId\":\"admin\",\"validFrom\":\"2026-04-01\",\"grantType\":\"MANUAL\",\"changeReason\":\"사용자 역할 일괄 교체\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].userId").value("prof1001"))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    assertThat(jdbc.queryForObject("select count(*) from user_role_assignment where user_id='prof1001' and grant_type='MANUAL' and status='ACTIVE'", Long.class)).isEqualTo(2);
    mvc.perform(put("/api/admin/users/prof1001/roles").cookie(sessionCookie(cookie)).contentType(MediaType.APPLICATION_JSON)
        .content("{\"roleCodes\":[],\"approverUserId\":\"admin\",\"validFrom\":\"2026-04-01\",\"changeReason\":\"검증 실패\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
  }

  @Test
  void unauthenticatedAndNonAdminRequestsAreBlockedWithoutLeakingSensitiveErrors() throws Exception {
    mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    String professorCookie = loginCookie("prof1001", "admin");
    mvc.perform(patch("/api/admin/users/prof1001/access").cookie(sessionCookie(professorCookie))
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"systemUseYn\":\"N\",\"changeReason\":\"권한 없음\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
  }

  private String loginCookie(String userId, String password) throws Exception {
    String setCookie = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"userId\":\"" + userId + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk()).andReturn().getResponse().getHeader("Set-Cookie");
    return sessionCookieHeader(setCookie);
  }

  private String sessionCookieHeader(String setCookie) {
    assertThat(setCookie).isNotBlank();
    int end = setCookie.indexOf(';');
    return end < 0 ? setCookie : setCookie.substring(0, end);
  }

  private Cookie sessionCookie(String setCookie) {
    String pair = sessionCookieHeader(setCookie);
    return new Cookie("KNUESESSION", pair.substring("KNUESESSION=".length()));
  }

  private String sessionId(String cookie) {
    assertThat(cookie).isNotBlank();
    int start = cookie.indexOf("KNUESESSION=") + "KNUESESSION=".length();
    int end = cookie.indexOf(';');
    return cookie.substring(start, end < 0 ? cookie.length() : end);
  }

  private long historyCount(String entityName, String entityKey) {
    Long count = jdbc.queryForObject(
        "select count(*) from change_history where entity_name=? and entity_key=?",
        Long.class,
        entityName,
        entityKey);
    return count == null ? 0 : count;
  }
}

