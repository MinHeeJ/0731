package kr.ac.knue.performance;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContractTests {
  @Autowired MockMvc mvc;
  @Autowired JdbcTemplate jdbc;

  private String adminCookie() throws Exception {
    MvcResult result = mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"loginId":"admin","password":"admin"}
            """))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Request-Id"))
        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.requestId").isNotEmpty())
        .andExpect(jsonPath("$.data.loginId").value("admin"))
        .andExpect(jsonPath("$.data.roleCodes", hasItem("R09")))
        .andReturn();
    return result.getResponse().getHeader("Set-Cookie");
  }

  @Test
  void requestIdHeaderUsesAllowlistedValueAndFallsBackForUnsafeInput() throws Exception {
    mvc.perform(get("/api/health").header("X-Request-Id", "req-20260731_A.B~1"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Request-Id", "req-20260731_A.B~1"))
        .andExpect(jsonPath("$.requestId").value("req-20260731_A.B~1"));

    mvc.perform(get("/api/health").header("X-Request-Id", "req-1\r\nSet-Cookie:evil=true"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Request-Id", org.hamcrest.Matchers.matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
        .andExpect(jsonPath("$.requestId", org.hamcrest.Matchers.matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
  }

  @Test
  void healthAndOpenApiProtectedReadOperationsReturnApiEnvelopeAndPortalFilteredBodies() throws Exception {
    String sessionCookie = adminCookie();

    mvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.status").value("UP"));
    mvc.perform(get("/api/auth/me").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.loginId").value("admin"))
        .andExpect(jsonPath("$.data.roleCodes", hasItem("R09")));
    mvc.perform(get("/api/users?staffName=관리&roleCode=R09").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.length()", greaterThan(0)))
        .andExpect(jsonPath("$.data[0].staffName").value("시스템 관리자"));
    mvc.perform(get("/api/organizations?organizationCode=DEPT-COMMON").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].organizationCode").value("DEPT-COMMON"));
    mvc.perform(get("/api/roles?roleCode=R09").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].roleCode").value("R09"));
    mvc.perform(get("/api/user-roles?userId=00000000-0000-0000-0000-000000000001&status=ACTIVE").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    mvc.perform(get("/api/menus?menuName=사용자").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()", greaterThan(0)));
    mvc.perform(get("/api/menu-permissions?targetType=ROLE&targetId=R09").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].targetType").value("ROLE"));
    mvc.perform(get("/api/code-groups?groupId=EMPLOYMENT_STATUS").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].groupId").value("EMPLOYMENT_STATUS"));
    mvc.perform(get("/api/code-details?groupId=EMPLOYMENT_STATUS").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].groupId").value("EMPLOYMENT_STATUS"));
  }

  @Test
  void authLoginLogoutValidationBusinessAndSessionSideEffectsArePersisted() throws Exception {
    Integer sessionsBefore = jdbc.queryForObject("select count(*) from auth_session", Integer.class);
    String sessionCookie = adminCookie();
    Integer activeAfterLogin = jdbc.queryForObject("select count(*) from auth_session where status='ACTIVE'", Integer.class);
    org.assertj.core.api.Assertions.assertThat(activeAfterLogin).isGreaterThan(0);

    mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"loginId":"","password":"admin"}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.loginId").exists());
    mvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"loginId":"admin","password":"wrong-password"}
            """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    Integer sessionsAfterFailedLogin = jdbc.queryForObject("select count(*) from auth_session", Integer.class);
    org.assertj.core.api.Assertions.assertThat(sessionsAfterFailedLogin).isEqualTo(sessionsBefore + 1);

    mvc.perform(post("/api/auth/logout").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")))
        .andExpect(jsonPath("$.data.loginId").value("admin"));
    Integer loggedOutSessions = jdbc.queryForObject("select count(*) from auth_session where status='LOGGED_OUT'", Integer.class);
    org.assertj.core.api.Assertions.assertThat(loggedOutSessions).isGreaterThan(0);
    mvc.perform(post("/api/auth/logout"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
  }

  @Test
  void userUsageAndUserRolesOperationsValidateBusinessRulesAndPersistSideEffects() throws Exception {
    String sessionCookie = adminCookie();

    mvc.perform(patch("/api/users/00000000-0000-0000-0000-000000000001/usage")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"systemUseEnabled":false,"staffName":"변조"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.systemUseEnabled").value(false))
        .andExpect(jsonPath("$.data.staffName").value("시스템 관리자"));
    Boolean useDisabled = jdbc.queryForObject("select system_use_enabled from user_account where user_id='00000000-0000-0000-0000-000000000001'", Boolean.class);
    org.assertj.core.api.Assertions.assertThat(useDisabled).isFalse();
    jdbc.update("update user_account set system_use_enabled=true where user_id='00000000-0000-0000-0000-000000000001'");
    mvc.perform(patch("/api/users/00000000-0000-0000-0000-000000000001/usage")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"systemUseEnabled":true}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.systemUseEnabled").value(true));
    mvc.perform(patch("/api/users/00000000-0000-0000-0000-000000000001/usage")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"systemUseEnabled":null}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.systemUseEnabled").exists());

    mvc.perform(put("/api/users/00000000-0000-0000-0000-000000000001/roles")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCodes":["R09"],"validFrom":"2026-01-01"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roleCodes", hasItem("R09")));
    Integer activeRoleRows = jdbc.queryForObject("select count(*) from user_role_assignment where user_id='00000000-0000-0000-0000-000000000001' and role_code='R09' and status='ACTIVE'", Integer.class);
    org.assertj.core.api.Assertions.assertThat(activeRoleRows).isGreaterThan(0);
    mvc.perform(put("/api/users/00000000-0000-0000-0000-000000000001/roles")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCodes":[]}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.roleCodes").exists());

    int resetRows = jdbc.update("update user_role_assignment set status='ACTIVE', updated_at=now() where assignment_id='00000000-0000-0000-0000-000000000499'");
    if (resetRows == 0) {
      jdbc.update("insert into user_role_assignment(assignment_id, user_id, role_code, assignment_type, valid_from, status, approved_by_user_id) values ('00000000-0000-0000-0000-000000000499','00000000-0000-0000-0000-000000000001','R09','MANUAL','2026-01-01','ACTIVE','00000000-0000-0000-0000-000000000001')");
    }
    mvc.perform(post("/api/user-roles")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"userId":"00000000-0000-0000-0000-000000000001","roleCode":"R08","validFrom":"2026-02-01","assignmentType":"MANUAL"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    mvc.perform(post("/api/user-roles")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"userId":"00000000-0000-0000-0000-000000000001","roleCode":"INVALID","validFrom":"2026-02-01"}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.roleCode").exists());
    mvc.perform(delete("/api/user-roles/00000000-0000-0000-0000-000000000499").header("Cookie", sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("REVOKED"));
    String revokedStatus = jdbc.queryForObject("select status from user_role_assignment where assignment_id='00000000-0000-0000-0000-000000000499'", String.class);
    org.assertj.core.api.Assertions.assertThat(revokedStatus).isEqualTo("REVOKED");
    mvc.perform(delete("/api/user-roles/00000000-0000-0000-0000-000000000499").header("Cookie", sessionCookie))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.assignmentId").exists());
  }

  @Test
  void roleOrganizationMenuAndPermissionWritesExposeDbBackedStateTransitions() throws Exception {
    String sessionCookie = adminCookie();

    mvc.perform(post("/api/roles")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCode":"R08","roleName":"점수산출 감사자","purpose":"감사","grantCriteria":"지정","defaultDataScope":"감사범위"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roleCode").value("R08"));
    mvc.perform(post("/api/roles")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCode":"R99","roleName":"잘못된 역할"}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.roleCode").exists());
    mvc.perform(put("/api/roles/R08")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCode":"R08","roleName":"점수산출 감사자 수정","purpose":"감사","grantCriteria":"승인","defaultDataScope":"전체"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roleName").value("점수산출 감사자 수정"))
        .andExpect(jsonPath("$.data.defaultDataScope").value("전체"));
    String updatedRoleName = jdbc.queryForObject("select role_name from role where role_code='R08'", String.class);
    org.assertj.core.api.Assertions.assertThat(updatedRoleName).isEqualTo("점수산출 감사자 수정");
    mvc.perform(put("/api/roles/R08")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCode":"R09","roleName":"불일치"}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.roleCode").exists());

    mvc.perform(put("/api/organization-relations/00000000-0000-0000-0000-000000000203")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"organizationId":"00000000-0000-0000-0000-000000000103","parentOrganizationId":"00000000-0000-0000-0000-000000000102","effectiveStartDate":"2026-01-01","effectiveEndDate":null}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.parentOrganizationId").value("00000000-0000-0000-0000-000000000102"));
    String parentOrganizationId = jdbc.queryForObject("select parent_organization_id::text from organization_relation where relation_id='00000000-0000-0000-0000-000000000203'", String.class);
    org.assertj.core.api.Assertions.assertThat(parentOrganizationId).isEqualTo("00000000-0000-0000-0000-000000000102");
    mvc.perform(put("/api/organization-relations/00000000-0000-0000-0000-000000000203")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"organizationId":"00000000-0000-0000-0000-000000000103","effectiveStartDate":"2026-02-01","effectiveEndDate":"2026-01-01"}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.effectiveEndDate").exists());

    mvc.perform(post("/api/menus")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"parentMenuId":"00000000-0000-0000-0000-000000001300","menuName":"계약 메뉴","menuLevel":"LEAF","displayOrder":9,"screenId":"SCR-CONTRACT","urlPath":"/admin/contract","isActive":true}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.menuName").value("계약 메뉴"));
    mvc.perform(post("/api/menus")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"parentMenuId":"00000000-0000-0000-0000-000000001300","menuName":"","menuLevel":"LEAF","displayOrder":10}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.menuName").exists());
    mvc.perform(put("/api/menus/00000000-0000-0000-0000-000000001301")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"parentMenuId":"00000000-0000-0000-0000-000000001300","menuName":"메뉴 구조 관리","menuLevel":"LEAF","displayOrder":7,"screenId":"SCR-MENUSTRUCT","urlPath":"/admin/menus/structure","isActive":false}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.displayOrder").value(7))
        .andExpect(jsonPath("$.data.isActive").value(false));
    Integer displayOrder = jdbc.queryForObject("select display_order from menu where menu_id='00000000-0000-0000-0000-000000001301'", Integer.class);
    org.assertj.core.api.Assertions.assertThat(displayOrder).isEqualTo(7);
    mvc.perform(put("/api/menus/00000000-0000-0000-0000-000000001301")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"parentMenuId":"00000000-0000-0000-0000-000000001301","menuName":"메뉴 구조 관리","menuLevel":"LEAF","displayOrder":7}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.parentMenuId").exists());

    mvc.perform(put("/api/menu-permissions")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"targetType":"ROLE","targetId":"R09","permissions":[{"menuId":"00000000-0000-0000-0000-000000001101","allowed":false}]}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.targetType").value("ROLE"));
    Boolean allowed = jdbc.queryForObject("select allowed from menu_permission where target_type='ROLE' and target_id='R09' and menu_id='00000000-0000-0000-0000-000000001101'", Boolean.class);
    org.assertj.core.api.Assertions.assertThat(allowed).isFalse();
    mvc.perform(put("/api/menu-permissions")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"targetType":"UNKNOWN","targetId":"R09","permissions":[{"menuId":"00000000-0000-0000-0000-000000001101","allowed":true}]}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.targetType").exists());
  }

  @Test
  void codeGroupAndCodeDetailWritesValidateContractsAndPersistSideEffects() throws Exception {
    String sessionCookie = adminCookie();

    mvc.perform(post("/api/code-groups")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"CONTRACT_GROUP","groupName":"계약그룹","description":"계약 테스트","managingDepartment":"시스템관리"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groupId").value("CONTRACT_GROUP"));
    mvc.perform(post("/api/code-groups")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"","groupName":"빈 그룹"}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.groupId").exists());
    mvc.perform(put("/api/code-groups/CONTRACT_GROUP")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"CONTRACT_GROUP","groupName":"계약그룹 수정","description":"설명 수정","managingDepartment":"품질관리"}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.groupName").value("계약그룹 수정"))
        .andExpect(jsonPath("$.data.managingDepartment").value("품질관리"));
    String managingDepartment = jdbc.queryForObject("select managing_department from code_group where group_id='CONTRACT_GROUP'", String.class);
    org.assertj.core.api.Assertions.assertThat(managingDepartment).isEqualTo("품질관리");
    mvc.perform(put("/api/code-groups/CONTRACT_GROUP")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"OTHER_GROUP","groupName":"불일치"}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.groupId").exists());

    mvc.perform(post("/api/code-details")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"CONTRACT_GROUP","codeValue":"ALPHA","codeName":"알파","sortOrder":1,"extraAttributes":{"portal":"admin"},"validFrom":"2026-01-01","isActive":true}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.codeValue").value("ALPHA"));
    mvc.perform(post("/api/code-details")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"CONTRACT_GROUP","codeValue":"BETA","codeName":"","sortOrder":2}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.codeName").exists());
    mvc.perform(put("/api/code-details/CONTRACT_GROUP/ALPHA")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"CONTRACT_GROUP","codeValue":"ALPHA","codeName":"알파 수정","parentCodeValue":null,"sortOrder":3,"extraAttributes":{"portal":"admin","scope":"updated"},"validFrom":"2026-01-01","validTo":"2026-12-31","isActive":false}
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.codeName").value("알파 수정"))
        .andExpect(jsonPath("$.data.sortOrder").value(3))
        .andExpect(jsonPath("$.data.isActive").value(false));
    Integer sortOrder = jdbc.queryForObject("select sort_order from code_detail where group_id='CONTRACT_GROUP' and code_value='ALPHA'", Integer.class);
    org.assertj.core.api.Assertions.assertThat(sortOrder).isEqualTo(3);
    mvc.perform(put("/api/code-details/CONTRACT_GROUP/ALPHA")
        .header("Cookie", sessionCookie)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"groupId":"CONTRACT_GROUP","codeValue":"ALPHA","codeName":"알파","parentCodeValue":"ALPHA","sortOrder":3}
            """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.parentCodeValue").exists());
  }

  @Test
  void protectedApiRequiresSessionForEverySecuredOperationFamily() throws Exception {
    mvc.perform(get("/api/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    mvc.perform(post("/api/roles")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCode":"R08","roleName":"인증 없음"}
            """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    mvc.perform(put("/api/users/00000000-0000-0000-0000-000000000001/roles")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"roleCodes":["R09"]}
            """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
  }
}
