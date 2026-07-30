package kr.ac.knue.cms.common;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminApiOperationCoverageTests {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  @Test
  void everyReadOperationReturnsContractBodyFromDatabaseBackedHandlers() throws Exception {
    Cookie cookie = adminCookie();

    mvc.perform(get("/api/health"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.status").value("UP"));

    mvc.perform(get("/api/admin/code-groups").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].group_id").exists());

    mvc.perform(get("/api/admin/code-groups/EVAL_AREA/codes").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].code_value").exists());

    mvc.perform(get("/api/admin/menu-permissions?targetType=ROLE&targetId=R09").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].targetType").value("ROLE"));

    mvc.perform(get("/api/admin/menus").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].menu_id").exists());

    mvc.perform(get("/api/admin/menus/tree").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].menu_id").exists());

    mvc.perform(get("/api/admin/orgs").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].org_code").exists());

    mvc.perform(get("/api/admin/orgs/tree").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].orgCode").exists());

    mvc.perform(get("/api/admin/roles").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].role_code").exists());

    mvc.perform(get("/api/admin/user-roles").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data[0].assignment_id").exists());
  }

  @Test
  void userAccountAndBusinessRoleWritesPersistStateTransitionsAndChangeHistory() throws Exception {
    Cookie cookie = adminCookie();
    long beforeAccount = historyCount("app_user", "teacher02", "updateUserAccount");
    long beforeReplace = historyCount("user_role", "teacher01", "replaceUserBusinessRoles");

    mvc.perform(patch("/api/admin/users/teacher02/account").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"useYn":"Y","reason":"계정 활성화 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.userId").value("teacher02"))
      .andExpect(jsonPath("$.data.useYn").value("Y"));
    assertThat(historyCount("app_user", "teacher02", "updateUserAccount")).isGreaterThan(beforeAccount);
    assertThat(jdbc.queryForObject("select status from app_user where user_id='teacher02'", String.class)).isEqualTo("ACTIVE");

    mvc.perform(put("/api/admin/users/teacher01/roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"roleCodes":["R01","R02"],"reason":"업무역할 교체 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.userId").value("teacher01"))
      .andExpect(jsonPath("$.data.roleCodes", containsInAnyOrder("R01", "R02")));
    assertThat(historyCount("user_role", "teacher01", "replaceUserBusinessRoles")).isGreaterThan(beforeReplace);
    assertThat(jdbc.queryForObject("select count(*) from user_role where user_id='teacher01' and status='ACTIVE'", Long.class)).isGreaterThanOrEqualTo(2L);
  }

  @Test
  void userRoleGrantUpdateRevokePersistSideEffectsAndStateTransitions() throws Exception {
    Cookie cookie = adminCookie();
    long beforeGrant = historyCountByOperation("grantUserRole");
    long beforeUpdate = historyCount("user_role", "2", "updateUserRole");
    long beforeRevoke = historyCount("user_role", "2", "revokeUserRole");

    mvc.perform(post("/api/admin/user-roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"userId":"teacher01","roleCode":"R02","assignmentType":"MANUAL","validFrom":"2026-01-01","validTo":"2026-12-31","reason":"역할 부여 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.assignmentId").isNumber())
      .andExpect(jsonPath("$.data.userId").value("teacher01"))
      .andExpect(jsonPath("$.data.roleCode").value("R02"));
    assertThat(historyCountByOperation("grantUserRole")).isGreaterThan(beforeGrant);
    assertThat(jdbc.queryForObject("select count(*) from user_role where user_id='teacher01' and role_code='R02' and status='ACTIVE'", Long.class)).isGreaterThan(0L);

    mvc.perform(patch("/api/admin/user-roles/2").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"userId":"teacher01","roleCode":"R01","status":"EXPIRED","validFrom":"2026-01-01","validTo":"2026-06-30","reason":"역할 만료 전이 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.assignmentId").value(2))
      .andExpect(jsonPath("$.data.status").value("EXPIRED"));
    assertThat(historyCount("user_role", "2", "updateUserRole")).isGreaterThan(beforeUpdate);
    assertThat(jdbc.queryForObject("select status from user_role where assignment_id=2", String.class)).isEqualTo("EXPIRED");

    mvc.perform(delete("/api/admin/user-roles/2").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.assignmentId").value(2))
      .andExpect(jsonPath("$.data.status").value("REVOKED"));
    assertThat(historyCount("user_role", "2", "revokeUserRole")).isGreaterThan(beforeRevoke);
    assertThat(jdbc.queryForObject("select status from user_role where assignment_id=2", String.class)).isEqualTo("REVOKED");
  }

  @Test
  void menuPermissionAndMenuWritesPersistChangeHistory() throws Exception {
    Cookie cookie = adminCookie();
    String menuName = "정적검증메뉴" + System.nanoTime();
    long beforePermission = historyCount("menu_permission", "ROLE:R09", "saveMenuPermissions");
    long beforeReorder = historyCount("menu", "reorder", "reorderMenus");
    long beforeUpdate = historyCount("menu", "3", "updateMenu");
    long beforeStructure = historyCount("menu", "3", "updateMenuStructure");

    mvc.perform(post("/api/admin/menus").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"menuType":"SCREEN","menuName":"%s","screenId":"SCR-STATIC","url":"/admin/static-contract","displayOrder":999,"useYn":"Y","reason":"메뉴 생성 검증"}
        """.formatted(menuName)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.menuId").isNumber())
      .andExpect(jsonPath("$.data.menuName").value(menuName));
    assertThat(historyCountByOperation("createMenu")).isGreaterThan(0L);

    mvc.perform(put("/api/admin/menu-permissions").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"targetType":"ROLE","targetId":"R09","items":[{"menuId":3,"accessAllowed":true}],"reason":"메뉴 권한 저장 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.targetType").value("ROLE"))
      .andExpect(jsonPath("$.data.targetId").value("R09"));
    assertThat(historyCount("menu_permission", "ROLE:R09", "saveMenuPermissions")).isGreaterThan(beforePermission);
    assertThat(jdbc.queryForObject("select access_allowed from menu_permission where target_type='ROLE' and target_id='R09' and menu_id=3", Boolean.class)).isTrue();

    mvc.perform(put("/api/admin/menus/3/structure").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"parentMenuId":2,"displayOrder":31,"reason":"메뉴 구조 저장 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.menuId").value(3));
    assertThat(historyCount("menu", "3", "updateMenuStructure")).isGreaterThan(beforeStructure);

    mvc.perform(put("/api/admin/menus/reorder").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"items":[{"menuId":3,"displayOrder":31}],"reason":"메뉴 순서 저장 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.count").value(1));
    assertThat(historyCount("menu", "reorder", "reorderMenus")).isGreaterThan(beforeReorder);

    mvc.perform(put("/api/admin/menus/3").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"menuName":"사용자 관리","screenId":"SCR-USERS","url":"/admin/users","icon":"user","businessCategory":"COMMON","description":"사용자 조회와 로컬 계정 관리","useYn":"Y","reason":"메뉴 수정 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.menuId").value(3));
    assertThat(historyCount("menu", "3", "updateMenu")).isGreaterThan(beforeUpdate);
  }

  @Test
  void codeGroupAndCodeDetailWritesPersistChangeHistory() throws Exception {
    Cookie cookie = adminCookie();
    String groupId = "TST" + System.nanoTime();
    String codeValue = "CV" + System.nanoTime();
    long beforeGroupUpdate = historyCount("code_group", "EVAL_AREA", "updateCodeGroup");
    long beforeCodeUpdate = historyCount("code_detail", "EVAL_AREA:TEACHING", "updateCodeDetail");

    mvc.perform(post("/api/admin/code-groups").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"groupId":"%s","groupName":"정적검증그룹","description":"계약 테스트","managingDepartment":"정보전산원","useYn":"Y","reason":"코드그룹 생성 검증"}
        """.formatted(groupId)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.groupId").value(groupId));
    assertThat(historyCount("code_group", groupId, "createCodeGroup")).isGreaterThan(0L);

    mvc.perform(put("/api/admin/code-groups/EVAL_AREA").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"groupName":"평가영역","description":"교수업적 평가영역 공통코드","managingDepartment":"교수지원과","useYn":"Y","reason":"코드그룹 수정 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.groupId").value("EVAL_AREA"));
    assertThat(historyCount("code_group", "EVAL_AREA", "updateCodeGroup")).isGreaterThan(beforeGroupUpdate);

    mvc.perform(post("/api/admin/code-groups/EVAL_AREA/codes").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"codeValue":"%s","codeName":"정적검증상세","sortOrder":999,"extraAttributes":"{}","validFrom":"2026-01-01","validTo":"2026-12-31","useYn":"Y","reason":"상세코드 생성 검증"}
        """.formatted(codeValue)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.groupId").value("EVAL_AREA"))
      .andExpect(jsonPath("$.data.codeValue").value(codeValue));
    assertThat(historyCount("code_detail", "EVAL_AREA:" + codeValue, "createCodeDetail")).isGreaterThan(0L);

    mvc.perform(put("/api/admin/code-groups/EVAL_AREA/codes/TEACHING").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"codeName":"교육","sortOrder":10,"extraAttributes":"{}","validFrom":"2026-01-01","validTo":"2026-12-31","useYn":"Y","reason":"상세코드 수정 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.groupId").value("EVAL_AREA"))
      .andExpect(jsonPath("$.data.codeValue").value("TEACHING"));
    assertThat(historyCount("code_detail", "EVAL_AREA:TEACHING", "updateCodeDetail")).isGreaterThan(beforeCodeUpdate);
  }

  @Test
  void organizationRelationAndRoleWritesPersistChangeHistory() throws Exception {
    Cookie cookie = adminCookie();
    long beforeOrg = historyCount("org_relation", "2", "updateOrganizationRelation");
    long beforeRole = historyCount("role", "R09", "updateRole");

    mvc.perform(put("/api/admin/org-relations/2").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"parentOrgCode":"UNIV001","validFrom":"2026-01-01","validTo":"2026-12-31","reason":"조직 관계 저장 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.relationId").value(2));
    assertThat(historyCount("org_relation", "2", "updateOrganizationRelation")).isGreaterThan(beforeOrg);

    mvc.perform(put("/api/admin/roles/R09").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"roleName":"시스템관리자","purpose":"사용자·조직·메뉴·권한·코드 관리","grantCriteria":"시스템 관리자","defaultDataScope":"전체","useYn":"Y","reason":"역할 저장 검증"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.roleCode").value("R09"));
    assertThat(historyCount("role", "R09", "updateRole")).isGreaterThan(beforeRole);
  }

  @Test
  void logoutPersistsLogoutSideEffectAndClearsSessionCookie() throws Exception {
    long beforeLogin = historyCountByOperation("login");
    Cookie cookie = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
        {"userId":"admin","password":"admin"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.userId").value("admin"))
      .andReturn().getResponse().getCookie("KNUESESSION");
    assertThat(historyCountByOperation("login")).isGreaterThan(beforeLogin);
    long beforeLogout = historyCountByOperation("logout");

    mvc.perform(post("/api/auth/logout").cookie(cookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.status").value("LOGGED_OUT"))
      .andExpect(cookie().maxAge("KNUESESSION", 0));
    assertThat(historyCountByOperation("logout")).isGreaterThan(beforeLogout);
  }

  @Test
  void notFoundAndValidationNegativeCasesExposeFieldErrors() throws Exception {
    Cookie cookie = adminCookie();

    mvc.perform(delete("/api/admin/user-roles/999999").cookie(cookie))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.errors[0].field").value("assignmentId"));

    mvc.perform(patch("/api/admin/user-roles/2").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"userId":"teacher01","roleCode":"BAD","status":"ACTIVE","validFrom":"2026-01-01","validTo":"2026-12-31"}
        """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.errors[0].field").value("roleCode"));

    mvc.perform(put("/api/admin/code-groups/EVAL_AREA/codes/TEACHING").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("""
        {"codeName":"교육","sortOrder":10,"validFrom":"2026-12-31","validTo":"2026-01-01","useYn":"Y"}
        """))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.errors[0].field").value("validTo"));
  }

  private Cookie adminCookie() throws Exception {
    return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
        {"userId":"admin","password":"admin"}
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andReturn().getResponse().getCookie("KNUESESSION");
  }

  private long historyCount(String entityName, String entityId, String operationId) {
    return jdbc.queryForObject(
      "select count(*) from change_history where entity_name=? and entity_id=? and cast(after_value as varchar) like ?",
      Long.class,
      entityName,
      entityId,
      "%" + operationId + "%"
    );
  }

  private long historyCountByOperation(String operationId) {
    String operationJson = "%" + operationId + "%";
    return jdbc.queryForObject(
      "select count(*) from change_history where cast(after_value as varchar) like ? or cast(before_value as varchar) like ?",
      Long.class,
      operationJson,
      operationJson
    );
  }
}
