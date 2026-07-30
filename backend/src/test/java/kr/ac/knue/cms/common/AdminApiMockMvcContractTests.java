package kr.ac.knue.cms.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiMockMvcContractTests {
  @Autowired MockMvc mockMvc;

  @Test @DisplayName("admin/admin 로그인 후 현재 사용자와 9개 메뉴를 조회한다")
  void loginAndMenus() throws Exception {
    var login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
      .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andReturn();
    String cookie = login.getResponse().getCookie("KNUESESSION").getValue();
    mockMvc.perform(get("/api/me").cookie(new jakarta.servlet.http.Cookie("KNUESESSION", cookie)))
      .andExpect(status().isOk()).andExpect(jsonPath("$.data.userId").value("admin"));
    mockMvc.perform(get("/api/me/menus").cookie(new jakarta.servlet.http.Cookie("KNUESESSION", cookie)))
      .andExpect(status().isOk()).andExpect(jsonPath("$.data[?(@.url=='/admin/users')]").exists());
  }

  @Test @DisplayName("보호 API는 미인증 401과 비관리자 403을 구분한다")
  void authFailures() throws Exception {
    mockMvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
    var login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"teacher01\",\"password\":\"admin\"}"))
      .andExpect(status().isOk()).andReturn();
    mockMvc.perform(get("/api/admin/users").cookie(login.getResponse().getCookie("KNUESESSION")))
      .andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
  }

  @Test @DisplayName("사용자 검색과 KORUS readonly 필드 수정 차단을 검증한다")
  void usersSearchAndReadonlyValidation() throws Exception {
    var cookie = adminCookie();
    mockMvc.perform(get("/api/admin/users?q=김").cookie(cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.data[0].positionName").exists());
    mockMvc.perform(patch("/api/admin/users/teacher01/account").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"useYn\":\"N\",\"name\":\"변경\"}"))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.errors[0].field").value("korus"));
  }

  @Test @DisplayName("조직 관계와 사용자 역할 기간 역전은 400 field error로 차단한다")
  void periodValidation() throws Exception {
    var cookie = adminCookie();
    mockMvc.perform(put("/api/admin/org-relations/2").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"parentOrgCode\":\"UNIV001\",\"validFrom\":\"2026-02-01\",\"validTo\":\"2026-01-01\"}"))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.errors[0].field").value("validTo"));
    mockMvc.perform(post("/api/admin/user-roles").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"teacher01\",\"roleCode\":\"R02\",\"validFrom\":\"2026-02-01\",\"validTo\":\"2026-01-01\"}"))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.errors[0].field").value("validTo"));
  }

  @Test @DisplayName("역할코드 변경, 자기 부모 메뉴, 코드그룹 필수값을 검증한다")
  void managementValidationErrors() throws Exception {
    var cookie = adminCookie();
    mockMvc.perform(put("/api/admin/roles/R09").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"roleCode\":\"R01\",\"roleName\":\"시스템관리자\"}"))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.errors[0].field").value("roleCode"));
    mockMvc.perform(put("/api/admin/menus/3/structure").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"parentMenuId\":3,\"displayOrder\":30}"))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.errors[0].field").value("parentMenuId"));
    mockMvc.perform(post("/api/admin/code-groups").cookie(cookie).contentType(MediaType.APPLICATION_JSON).content("{\"groupName\":\"테스트\"}"))
      .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.errors[0].field").value("groupId"));
  }

  jakarta.servlet.http.Cookie adminCookie() throws Exception {
    return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":\"admin\",\"password\":\"admin\"}"))
      .andReturn().getResponse().getCookie("KNUESESSION");
  }
}
