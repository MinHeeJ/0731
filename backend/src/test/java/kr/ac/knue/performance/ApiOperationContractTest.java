package kr.ac.knue.performance;

import static org.mockito.ArgumentMatchers.any;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.performance.auth.AuthController;
import kr.ac.knue.performance.auth.AuthService;
import kr.ac.knue.performance.auth.CurrentUser;
import kr.ac.knue.performance.auth.LoginRequest;
import kr.ac.knue.performance.code.CodeController;
import kr.ac.knue.performance.code.CodeDetailDto;
import kr.ac.knue.performance.code.CodeDetailRequest;
import kr.ac.knue.performance.code.CodeGroupDto;
import kr.ac.knue.performance.code.CodeGroupRequest;
import kr.ac.knue.performance.code.CodeService;
import kr.ac.knue.performance.common.error.ApiException;
import kr.ac.knue.performance.common.error.GlobalExceptionHandler;
import kr.ac.knue.performance.common.security.SessionAuthenticationFilter;
import kr.ac.knue.performance.common.web.ApiError;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import kr.ac.knue.performance.menu.MenuController;
import kr.ac.knue.performance.menu.MenuDto;
import kr.ac.knue.performance.menu.MenuPermissionDto;
import kr.ac.knue.performance.menu.MenuRequest;
import kr.ac.knue.performance.menu.MenuService;
import kr.ac.knue.performance.menu.SaveMenuPermissionsRequest;
import kr.ac.knue.performance.organization.OrganizationController;
import kr.ac.knue.performance.organization.OrganizationDto;
import kr.ac.knue.performance.organization.OrganizationRequests;
import kr.ac.knue.performance.organization.OrganizationService;
import kr.ac.knue.performance.role.RoleController;
import kr.ac.knue.performance.role.RoleDto;
import kr.ac.knue.performance.role.RoleRequest;
import kr.ac.knue.performance.role.RoleService;
import kr.ac.knue.performance.user.UserController;
import kr.ac.knue.performance.user.UserDto;
import kr.ac.knue.performance.user.UserRequests;
import kr.ac.knue.performance.user.UserService;
import kr.ac.knue.performance.userrole.CreateUserRoleRequest;
import kr.ac.knue.performance.userrole.UserRoleAssignmentDto;
import kr.ac.knue.performance.userrole.UserRoleController;
import kr.ac.knue.performance.userrole.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.filter.OncePerRequestFilter;

@ExtendWith(MockitoExtension.class)
class ApiOperationContractTest {
  private static final UUID ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000009");
  private static final UUID USER_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
  private static final UUID MENU_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
  private static final UUID PARENT_MENU_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
  private static final UUID PERMISSION_ID = UUID.fromString("30000000-0000-0000-0000-000000000001");
  private static final UUID ORGANIZATION_ID = UUID.fromString("40000000-0000-0000-0000-000000000001");
  private static final UUID RELATION_ID = UUID.fromString("40000000-0000-0000-0000-000000000002");
  private static final UUID ASSIGNMENT_ID = UUID.fromString("50000000-0000-0000-0000-000000000001");
  private static final String REQUEST_ID = "req-contract-001";

  @Mock AuthService authService;
  @Mock UserService userService;
  @Mock OrganizationService organizationService;
  @Mock RoleService roleService;
  @Mock UserRoleService userRoleService;
  @Mock MenuService menuService;
  @Mock CodeService codeService;

  private MockMvc mvc;
  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(
            new AuthController(authService),
            new UserController(userService),
            new OrganizationController(organizationService),
            new RoleController(roleService),
            new UserRoleController(userRoleService),
            new MenuController(menuService),
            new CodeController(codeService))
        .setControllerAdvice(new GlobalExceptionHandler())
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .addFilters(new RequestIdFilter(), new ContractAuthenticationFilter(objectMapper))
        .build();
  }

  @Test
  void openApiFixtureIsReadFromClasspathContractsOpenapiYaml() throws Exception {
    ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
    String openApi = resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
    org.assertj.core.api.Assertions.assertThat(openApi).contains("/api/code-details", "x-required-tests", "x-side-effects", "x-state-transitions");
  }

  @Test
  void getApiCodeGroupsReturnsBodyContractAndPortalFiltering() throws Exception {
    when(codeService.listGroups("ACADEMIC", "직급", "교무처"))
        .thenReturn(List.of(new CodeGroupDto("ACADEMIC", "직급", "교원 직급", "교무처", true)));

    mvc.perform(get("/api/code-groups")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .param("groupId", "ACADEMIC")
            .param("groupName", "직급")
            .param("managingDepartment", "교무처"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Request-Id", REQUEST_ID))
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].groupId").value("ACADEMIC"))
        .andExpect(jsonPath("$.data[0].groupName").value("직급"));

    verify(codeService).listGroups("ACADEMIC", "직급", "교무처");
  }

  @Test
  void getApiCodeDetailsReturnsBodyContractAndPortalFiltering() throws Exception {
    when(codeService.listDetails("ACADEMIC", "GRADE"))
        .thenReturn(List.of(new CodeDetailDto("ACADEMIC", "GRADE-ASSOC", "부교수", "GRADE", 2, objectMapper.readTree("{\"rank\":2}"), LocalDate.parse("2026-01-01"), null, true)));

    mvc.perform(get("/api/code-details")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .param("groupId", "ACADEMIC")
            .param("parentCodeValue", "GRADE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data[0].groupId").value("ACADEMIC"))
        .andExpect(jsonPath("$.data[0].codeValue").value("GRADE-ASSOC"))
        .andExpect(jsonPath("$.data[0].extraAttributes.rank").value(2));

    verify(codeService).listDetails("ACADEMIC", "GRADE");
  }

  @Test
  void getApiMenusReturnsBodyContractAndPortalFiltering() throws Exception {
    when(menuService.listMenus(PARENT_MENU_ID, "SYS-ROLE", "권한"))
        .thenReturn(List.of(new MenuDto(MENU_ID, PARENT_MENU_ID, "권한관리", "2", 3, "SYS-ROLE", "/system/roles", "shield", "SYSTEM", "역할 메뉴", true)));

    mvc.perform(get("/api/menus")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .param("parentMenuId", PARENT_MENU_ID.toString())
            .param("screenId", "SYS-ROLE")
            .param("menuName", "권한"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data[0].menuId").value(MENU_ID.toString()))
        .andExpect(jsonPath("$.data[0].parentMenuId").value(PARENT_MENU_ID.toString()))
        .andExpect(jsonPath("$.data[0].screenId").value("SYS-ROLE"));

    verify(menuService).listMenus(PARENT_MENU_ID, "SYS-ROLE", "권한");
  }

  @Test
  void getApiMenuPermissionsReturnsBodyContractAndPortalFiltering() throws Exception {
    when(menuService.listPermissions("ROLE", "R09"))
        .thenReturn(List.of(new MenuPermissionDto(PERMISSION_ID, "ROLE", "R09", MENU_ID, true)));

    mvc.perform(get("/api/menu-permissions")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .param("targetType", "ROLE")
            .param("targetId", "R09"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data[0].permissionId").value(PERMISSION_ID.toString()))
        .andExpect(jsonPath("$.data[0].allowed").value(true));

    verify(menuService).listPermissions("ROLE", "R09");
  }

  @Test
  void getApiOrganizationsReturnsBodyContractAndPortalFiltering() throws Exception {
    when(organizationService.list("COLL-EDU", "사범", ORGANIZATION_ID))
        .thenReturn(List.of(new OrganizationDto(ORGANIZATION_ID, "COLL-EDU", "사범대학", "COLLEGE", ORGANIZATION_ID, LocalDate.parse("2026-03-01"), null, RELATION_ID)));

    mvc.perform(get("/api/organizations")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .param("organizationCode", "COLL-EDU")
            .param("organizationName", "사범")
            .param("parentOrganizationId", ORGANIZATION_ID.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data[0].organizationId").value(ORGANIZATION_ID.toString()))
        .andExpect(jsonPath("$.data[0].organizationCode").value("COLL-EDU"));

    verify(organizationService).list("COLL-EDU", "사범", ORGANIZATION_ID);
  }

  @Test
  void getApiRolesReturnsBodyContractAndPortalFiltering() throws Exception {
    when(roleService.list("R09", "시스템"))
        .thenReturn(List.of(new RoleDto("R09", "시스템관리자", "운영", "총괄 관리자", "ALL", true)));

    mvc.perform(get("/api/roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .param("roleCode", "R09")
            .param("roleName", "시스템"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data[0].roleCode").value("R09"))
        .andExpect(jsonPath("$.data[0].defaultDataScope").value("ALL"));

    verify(roleService).list("R09", "시스템");
  }

  @Test
  void getApiUserRolesReturnsBodyContractAndPortalFiltering() throws Exception {
    when(userRoleService.list(USER_ID, "R09", "ACTIVE"))
        .thenReturn(List.of(new UserRoleAssignmentDto(ASSIGNMENT_ID, USER_ID, "R09", "MANUAL", LocalDate.parse("2026-03-01"), null, ACTOR_ID, "ACTIVE")));

    mvc.perform(get("/api/user-roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .param("userId", USER_ID.toString())
            .param("roleCode", "R09")
            .param("status", "ACTIVE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data[0].assignmentId").value(ASSIGNMENT_ID.toString()))
        .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

    verify(userRoleService).list(USER_ID, "R09", "ACTIVE");
  }

  @Test
  void deleteApiUserRolesAssignmentRequiresAuthAndPersistsActiveToRevokedTransition() throws Exception {
    when(userRoleService.revoke(eq(ASSIGNMENT_ID), any(CurrentUser.class)))
        .thenReturn(new UserRoleAssignmentDto(ASSIGNMENT_ID, USER_ID, "R09", "MANUAL", LocalDate.parse("2026-03-01"), LocalDate.parse("2026-07-31"), ACTOR_ID, "REVOKED"));

    mvc.perform(delete("/api/user-roles/50000000-0000-0000-0000-000000000001")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.status").value("REVOKED"))
        .andExpect(jsonPath("$.data.approvedByUserId").value(ACTOR_ID.toString()));

    verify(userRoleService).revoke(eq(ASSIGNMENT_ID), any(CurrentUser.class));

    mvc.perform(delete("/api/user-roles/50000000-0000-0000-0000-000000000001")
            .header("X-Request-Id", REQUEST_ID))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
  }

  @Test
  void patchApiUsersUsageRequiresAuthValidationAndPersistsRequestIdSideEffect() throws Exception {
    when(userService.updateUsage(eq(USER_ID), any(UserRequests.Usage.class)))
        .thenReturn(userDto(false, List.of("R09")));

    mvc.perform(patch("/api/users/10000000-0000-0000-0000-000000000001/usage")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"systemUseEnabled\":false,\"reason\":\"휴직\",\"staffName\":\"원천정보수정시도\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.systemUseEnabled").value(false))
        .andExpect(jsonPath("$.data.staffName").value("김교수"));

    ArgumentCaptor<UserRequests.Usage> usageCaptor = ArgumentCaptor.forClass(UserRequests.Usage.class);
    verify(userService).updateUsage(eq(USER_ID), usageCaptor.capture());
    org.assertj.core.api.Assertions.assertThat(usageCaptor.getValue().systemUseEnabled()).isFalse();
    org.assertj.core.api.Assertions.assertThat(usageCaptor.getValue().reason()).isEqualTo("휴직");

    mvc.perform(patch("/api/users/10000000-0000-0000-0000-000000000001/usage")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"systemUseEnabled\":true}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));

    mvc.perform(patch("/api/users/10000000-0000-0000-0000-000000000001/usage")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"systemUseEnabled\":null}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.systemUseEnabled").exists());
  }

  @Test
  void postApiAuthLoginValidatesAndReturnsRequestIdAndSessionSideEffect() throws Exception {
    when(authService.login(any(LoginRequest.class), any(HttpServletResponse.class)))
        .thenReturn(new CurrentUser(ACTOR_ID, "admin", List.of("R09")));

    mvc.perform(post("/api/auth/login")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.loginId").value("admin"));

    verify(authService).login(any(LoginRequest.class), any(HttpServletResponse.class));

    mvc.perform(post("/api/auth/login")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"loginId\":\"\",\"password\":\"admin\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.error.fields.loginId").exists());
  }

  @Test
  void postApiAuthLogoutRequiresAuthAndReturnsRequestIdAndLoggedOutSideEffect() throws Exception {
    when(authService.logout(eq("contract-session"), any(CurrentUser.class), any(HttpServletResponse.class)))
        .thenReturn(new CurrentUser(ACTOR_ID, "admin", List.of("R09")));

    mvc.perform(post("/api/auth/logout")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.loginId").value("admin"));

    verify(authService).logout(eq("contract-session"), any(CurrentUser.class), any(HttpServletResponse.class));

    mvc.perform(post("/api/auth/logout")
            .header("X-Request-Id", REQUEST_ID))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.error.fields.sessionCookie").exists());
  }

  @Test
  void postApiCodeGroupsCoversAuthBusinessValidationAndWriteSideEffect() throws Exception {
    when(codeService.createGroup(any(CodeGroupRequest.class)))
        .thenReturn(new CodeGroupDto("EVAL", "평가구분", "업적평가", "교무처", true));

    mvc.perform(post("/api/code-groups")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"groupName\":\"평가구분\",\"description\":\"업적평가\",\"managingDepartment\":\"교무처\",\"isActive\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.groupId").value("EVAL"))
        .andExpect(jsonPath("$.data.groupName").value("평가구분"));

    verify(codeService).createGroup(any(CodeGroupRequest.class));

    mvc.perform(post("/api/code-groups")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"groupName\":\"평가구분\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));

    mvc.perform(post("/api/code-groups")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"\",\"groupName\":\"평가구분\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.groupId").exists());
  }

  @Test
  void putApiCodeGroupsCoversAuthValidationBusinessAndGroupNameSideEffect() throws Exception {
    when(codeService.updateGroup(eq("EVAL"), any(CodeGroupRequest.class)))
        .thenReturn(new CodeGroupDto("EVAL", "평가구분수정", "업적평가", "교무처", true));

    mvc.perform(put("/api/code-groups/EVAL")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"groupName\":\"평가구분수정\",\"description\":\"업적평가\",\"managingDepartment\":\"교무처\",\"reason\":\"명칭변경\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.groupName").value("평가구분수정"));

    ArgumentCaptor<CodeGroupRequest> captor = ArgumentCaptor.forClass(CodeGroupRequest.class);
    verify(codeService).updateGroup(eq("EVAL"), captor.capture());
    org.assertj.core.api.Assertions.assertThat(captor.getValue().groupName()).isEqualTo("평가구분수정");

    mvc.perform(put("/api/code-groups/EVAL")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"OTHER\",\"groupName\":\"평가구분\"}"))
        .andExpect(status().isOk());

    mvc.perform(put("/api/code-groups/EVAL")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"groupName\":\"평가구분\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void postApiCodeDetailsCoversAuthBusinessValidationAndWriteSideEffect() throws Exception {
    when(codeService.createDetail(any(CodeDetailRequest.class)))
        .thenReturn(new CodeDetailDto("EVAL", "RESEARCH", "연구", null, 1, objectMapper.readTree("{\"score\":10}"), LocalDate.parse("2026-01-01"), null, true));

    mvc.perform(post("/api/code-details")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"codeValue\":\"RESEARCH\",\"codeName\":\"연구\",\"sortOrder\":1,\"extraAttributes\":{\"score\":10},\"validFrom\":\"2026-01-01\",\"isActive\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.codeName").value("연구"))
        .andExpect(jsonPath("$.data.extraAttributes.score").value(10));

    verify(codeService).createDetail(any(CodeDetailRequest.class));

    mvc.perform(post("/api/code-details")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"codeValue\":\"RESEARCH\",\"codeName\":\"연구\",\"sortOrder\":1}"))
        .andExpect(status().isUnauthorized());

    mvc.perform(post("/api/code-details")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"codeValue\":\"RESEARCH\",\"codeName\":\"\",\"sortOrder\":1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.codeName").exists());
  }

  @Test
  void putApiCodeDetailsCoversAuthValidationSideEffectsAndDisabledStateTransition() throws Exception {
    when(codeService.updateDetail(eq("EVAL"), eq("RESEARCH"), any(CodeDetailRequest.class)))
        .thenReturn(new CodeDetailDto("EVAL", "RESEARCH", "연구변경", "ROOT", 4, objectMapper.readTree("{\"score\":20}"), LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"), false));

    mvc.perform(put("/api/code-details/EVAL/RESEARCH")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"codeValue\":\"RESEARCH\",\"codeName\":\"연구변경\",\"parentCodeValue\":\"ROOT\",\"sortOrder\":4,\"extraAttributes\":{\"score\":20},\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"isActive\":false,\"reason\":\"disabled\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.codeName").value("연구변경"))
        .andExpect(jsonPath("$.data.parentCodeValue").value("ROOT"))
        .andExpect(jsonPath("$.data.validTo").value("2026-12-31"))
        .andExpect(jsonPath("$.data.isActive").value(false));

    ArgumentCaptor<CodeDetailRequest> captor = ArgumentCaptor.forClass(CodeDetailRequest.class);
    verify(codeService).updateDetail(eq("EVAL"), eq("RESEARCH"), captor.capture());
    org.assertj.core.api.Assertions.assertThat(captor.getValue().parentCodeValue()).isEqualTo("ROOT");
    org.assertj.core.api.Assertions.assertThat(captor.getValue().extraAttributes().get("score").asInt()).isEqualTo(20);
    org.assertj.core.api.Assertions.assertThat(captor.getValue().isActive()).isFalse();

    mvc.perform(put("/api/code-details/EVAL/RESEARCH")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"groupId\":\"EVAL\",\"codeValue\":\"RESEARCH\",\"codeName\":\"연구\",\"sortOrder\":1}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void postApiMenusCoversAuthBusinessValidationAndWriteSideEffect() throws Exception {
    when(menuService.createMenu(any(MenuRequest.class)))
        .thenReturn(new MenuDto(MENU_ID, PARENT_MENU_ID, "시스템관리", "2", 5, "SYS-MENU", "/system/menus", "menu", "SYSTEM", "메뉴", true));

    mvc.perform(post("/api/menus")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"parentMenuId\":\"20000000-0000-0000-0000-000000000002\",\"menuName\":\"시스템관리\",\"menuLevel\":\"2\",\"displayOrder\":5,\"screenId\":\"SYS-MENU\",\"urlPath\":\"/system/menus\",\"isActive\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.menuId").value(MENU_ID.toString()))
        .andExpect(jsonPath("$.data.menuName").value("시스템관리"));

    verify(menuService).createMenu(any(MenuRequest.class));

    mvc.perform(post("/api/menus")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"menuName\":\"시스템관리\",\"menuLevel\":\"2\",\"displayOrder\":5}"))
        .andExpect(status().isUnauthorized());

    mvc.perform(post("/api/menus")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"menuName\":\"\",\"menuLevel\":\"2\",\"displayOrder\":5}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.menuName").exists());
  }

  @Test
  void putApiMenusCoversAuthValidationParentMenuSideEffectAndDisabledStateTransition() throws Exception {
    when(menuService.updateMenu(eq(MENU_ID), any(MenuRequest.class)))
        .thenReturn(new MenuDto(MENU_ID, PARENT_MENU_ID, "시스템관리변경", "2", 7, "SYS-MENU", "/system/menus", "menu", "SYSTEM", "메뉴", false));

    mvc.perform(put("/api/menus/20000000-0000-0000-0000-000000000001")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"parentMenuId\":\"20000000-0000-0000-0000-000000000002\",\"menuName\":\"시스템관리변경\",\"menuLevel\":\"2\",\"displayOrder\":7,\"screenId\":\"SYS-MENU\",\"urlPath\":\"/system/menus\",\"isActive\":false,\"reason\":\"disabled\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.parentMenuId").value(PARENT_MENU_ID.toString()))
        .andExpect(jsonPath("$.data.displayOrder").value(7))
        .andExpect(jsonPath("$.data.isActive").value(false));

    ArgumentCaptor<MenuRequest> captor = ArgumentCaptor.forClass(MenuRequest.class);
    verify(menuService).updateMenu(eq(MENU_ID), captor.capture());
    org.assertj.core.api.Assertions.assertThat(captor.getValue().parentMenuId()).isEqualTo(PARENT_MENU_ID);
    org.assertj.core.api.Assertions.assertThat(captor.getValue().isActive()).isFalse();

    mvc.perform(put("/api/menus/20000000-0000-0000-0000-000000000001")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"menuName\":\"시스템관리\",\"menuLevel\":\"2\",\"displayOrder\":5}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void putApiMenuPermissionsCoversAuthValidationRequestIdUpsertAndStateTransition() throws Exception {
    when(menuService.savePermissions(any(SaveMenuPermissionsRequest.class)))
        .thenReturn(new MenuPermissionDto(PERMISSION_ID, "ROLE", "R09", MENU_ID, false));

    mvc.perform(put("/api/menu-permissions")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"permissions\":[{\"menuId\":\"20000000-0000-0000-0000-000000000001\",\"allowed\":false}],\"reason\":\"권한회수\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.permissionId").value(PERMISSION_ID.toString()))
        .andExpect(jsonPath("$.data.allowed").value(false));

    ArgumentCaptor<SaveMenuPermissionsRequest> captor = ArgumentCaptor.forClass(SaveMenuPermissionsRequest.class);
    verify(menuService).savePermissions(captor.capture());
    org.assertj.core.api.Assertions.assertThat(captor.getValue().permissions().get(0).allowed()).isFalse();

    mvc.perform(put("/api/menu-permissions")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"targetType\":\"ROLE\",\"targetId\":\"R09\",\"permissions\":[{\"menuId\":\"20000000-0000-0000-0000-000000000001\",\"allowed\":true}]}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void putApiOrganizationRelationsCoversAuthValidationPeriodAndPreviousStateTransition() throws Exception {
    when(organizationService.updateRelation(eq(RELATION_ID), any(OrganizationRequests.Relation.class)))
        .thenReturn(new OrganizationDto(ORGANIZATION_ID, "COLL-EDU", "사범대학", "COLLEGE", null, LocalDate.parse("2026-03-01"), LocalDate.parse("2026-12-31"), RELATION_ID));

    mvc.perform(put("/api/organization-relations/40000000-0000-0000-0000-000000000002")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"organizationId\":\"40000000-0000-0000-0000-000000000001\",\"parentOrganizationId\":null,\"effectiveStartDate\":\"2026-03-01\",\"effectiveEndDate\":\"2026-12-31\",\"reason\":\"조직개편\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.effectiveStartDate").value("2026-03-01"))
        .andExpect(jsonPath("$.data.effectiveEndDate").value("2026-12-31"));

    verify(organizationService).updateRelation(eq(RELATION_ID), any(OrganizationRequests.Relation.class));

    mvc.perform(put("/api/organization-relations/40000000-0000-0000-0000-000000000002")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"organizationId\":\"40000000-0000-0000-0000-000000000001\",\"effectiveStartDate\":\"2026-03-01\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void postAndPutApiRolesCoverBusinessValidationAndGrantCriteriaSideEffects() throws Exception {
    when(roleService.create(any(RoleRequest.class)))
        .thenReturn(new RoleDto("R08", "성과담당", "성과 관리", "부서 담당자", "DEPARTMENT", true));
    when(roleService.update(eq("R08"), any(RoleRequest.class)))
        .thenReturn(new RoleDto("R08", "성과담당수정", "성과 관리", "부서장 승인", "DEPARTMENT", true));

    mvc.perform(post("/api/roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roleCode\":\"R08\",\"roleName\":\"성과담당\",\"purpose\":\"성과 관리\",\"grantCriteria\":\"부서 담당자\",\"defaultDataScope\":\"DEPARTMENT\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.roleCode").value("R08"))
        .andExpect(jsonPath("$.data.grantCriteria").value("부서 담당자"));

    mvc.perform(post("/api/roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roleCode\":\"\",\"roleName\":\"성과담당\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.roleCode").exists());

    mvc.perform(put("/api/roles/R08")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roleCode\":\"R08\",\"roleName\":\"성과담당수정\",\"purpose\":\"성과 관리\",\"grantCriteria\":\"부서장 승인\",\"defaultDataScope\":\"DEPARTMENT\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.roleName").value("성과담당수정"))
        .andExpect(jsonPath("$.data.defaultDataScope").value("DEPARTMENT"));

    verify(roleService).create(any(RoleRequest.class));
    verify(roleService).update(eq("R08"), any(RoleRequest.class));

    mvc.perform(put("/api/roles/R08")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roleCode\":\"R08\",\"roleName\":\"성과담당\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void postApiUserRolesCoversAuthValidationAndAbsentToActiveStateTransition() throws Exception {
    when(userRoleService.grant(any(CreateUserRoleRequest.class), any(CurrentUser.class)))
        .thenReturn(new UserRoleAssignmentDto(ASSIGNMENT_ID, USER_ID, "R08", "MANUAL", LocalDate.parse("2026-03-01"), null, ACTOR_ID, "ACTIVE"));

    mvc.perform(post("/api/user-roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":\"10000000-0000-0000-0000-000000000001\",\"roleCode\":\"R08\",\"validFrom\":\"2026-03-01\",\"assignmentType\":\"MANUAL\",\"reason\":\"보직부여\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.status").value("ACTIVE"))
        .andExpect(jsonPath("$.data.approvedByUserId").value(ACTOR_ID.toString()));

    verify(userRoleService).grant(any(CreateUserRoleRequest.class), any(CurrentUser.class));

    mvc.perform(post("/api/user-roles")
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":\"10000000-0000-0000-0000-000000000001\",\"roleCode\":\"R08\",\"validFrom\":\"2026-03-01\"}"))
        .andExpect(status().isUnauthorized());

    mvc.perform(post("/api/user-roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":\"10000000-0000-0000-0000-000000000001\",\"roleCode\":\"\",\"validFrom\":\"2026-03-01\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.roleCode").exists());
  }

  @Test
  void putApiUsersRolesCoversValidationRequestIdAndRoleSetStateTransition() throws Exception {
    when(userService.replaceRoles(eq(USER_ID), any(UserRequests.Roles.class), any(CurrentUser.class)))
        .thenReturn(userDto(true, List.of("R08", "R09")));

    mvc.perform(put("/api/users/10000000-0000-0000-0000-000000000001/roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roleCodes\":[\"R08\",\"R09\"],\"validFrom\":\"2026-03-01\",\"reason\":\"업무변경\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestId").value(REQUEST_ID))
        .andExpect(jsonPath("$.data.roleCodes[0]").value("R08"))
        .andExpect(jsonPath("$.data.roleCodes[1]").value("R09"));

    verify(userService).replaceRoles(eq(USER_ID), any(UserRequests.Roles.class), any(CurrentUser.class));

    mvc.perform(put("/api/users/10000000-0000-0000-0000-000000000001/roles")
            .cookie(sessionCookie())
            .header("X-Request-Id", REQUEST_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"roleCodes\":[],\"validFrom\":\"2026-03-01\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.roleCodes").exists());
  }

  private Cookie sessionCookie() {
    return new Cookie("SESSION", "contract-session");
  }

  private UserDto userDto(boolean systemUseEnabled, List<String> roleCodes) {
    return new UserDto(USER_ID, "2026001", "김교수", "COLL-EDU", "교수", "정교수", "ACTIVE", roleCodes, systemUseEnabled, null, OffsetDateTime.parse("2026-07-31T00:00:00Z"));
  }

  private static final class ContractAuthenticationFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    private ContractAuthenticationFilter(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
      return request.getRequestURI().equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
      boolean authenticated = java.util.Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
          .anyMatch(cookie -> "SESSION".equals(cookie.getName()) && !cookie.getValue().isBlank());
      if (!authenticated) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(new ApiError("UNAUTHENTICATED", "인증 세션이 필요합니다.", java.util.Map.of("sessionCookie", "SESSION 쿠키가 필요합니다.")), String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE))));
        return;
      }
      request.setAttribute(SessionAuthenticationFilter.CURRENT_USER, new CurrentUser(ACTOR_ID, "admin", List.of("R09")));
      filterChain.doFilter(request, response);
    }
  }
}
