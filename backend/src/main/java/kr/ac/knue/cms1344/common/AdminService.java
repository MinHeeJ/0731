package kr.ac.knue.cms1344.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.cms1344.api.ApiException;
import kr.ac.knue.cms1344.api.ApiResponse;
import kr.ac.knue.cms1344.api.Requests.*;
import kr.ac.knue.cms1344.persistence.AdminMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AdminService {
  private static final String SESSION_COOKIE = "SESSION";
  private static final List<String> ROLE_CODES = List.of("R01","R02","R03","R04","R05","R06","R07","R08","R09");
  private static final List<String> USE_YN = List.of("Y", "N");
  private static final List<String> ASSIGNMENT_TYPES = List.of("POSITION_BASED", "MANUAL");
  private static final List<String> TARGET_TYPES = List.of("ROLE", "ORGANIZATION", "USER");
  private final AdminMapper mapper;
  private final ObjectMapper objectMapper;

  public AdminService(AdminMapper mapper, ObjectMapper objectMapper) {
    this.mapper = mapper;
    this.objectMapper = objectMapper;
  }

  public Map<String,Object> health() {
    Map<String,Object> data = new HashMap<>();
    data.put("status", "UP");
    data.put("service", "cms1344-common-foundation");
    return data;
  }

  @Transactional
  public Map<String,Object> login(LoginRequest body, HttpServletResponse response) {
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    if (!StringUtils.hasText(body.loginId())) errors.add(new ApiResponse.FieldError("loginId", "로그인 ID는 필수입니다."));
    if (!StringUtils.hasText(body.password())) errors.add(new ApiResponse.FieldError("password", "비밀번호는 필수입니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
    String hash = sha256(body.password());
    if (mapper.canLogin(body.loginId(), hash) == 0) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "관리자 권한이 없거나 비활성 계정입니다.");
    Map<String,Object> user = mapper.userByLogin(body.loginId());
    String userId = value(user, "userId");
    String sessionId = UUID.randomUUID().toString();
    mapper.insertSession(sessionId, userId);
    mapper.touchLogin(userId);
    Cookie cookie = new Cookie(SESSION_COOKIE, sessionId);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(8 * 60 * 60);
    response.addHeader(HttpHeaders.SET_COOKIE, "%s=%s; Path=/; HttpOnly; SameSite=Lax; Max-Age=%d".formatted(SESSION_COOKIE, sessionId, 8 * 60 * 60));
    return currentUserData(user);
  }

  @Transactional
  public Map<String,Object> logout(HttpServletRequest request, HttpServletResponse response) {
    String sessionId = sessionId(request);
    if (sessionId == null || mapper.activeSession(sessionId) == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 세션이 필요합니다.");
    mapper.expireSession(sessionId);
    response.addHeader(HttpHeaders.SET_COOKIE, SESSION_COOKIE + "=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
    Map<String,Object> data = new HashMap<>();
    data.put("loggedOut", true);
    return data;
  }

  public Map<String,Object> me(HttpServletRequest request) {
    return currentUserData(requireAuth(request));
  }

  public Map<String,Object> requireAuth(HttpServletRequest request) {
    String sessionId = sessionId(request);
    if (sessionId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 세션이 필요합니다.");
    Map<String,Object> session = mapper.activeSession(sessionId);
    if (session == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증 세션이 필요합니다.");
    Map<String,Object> user = mapper.userById(value(session, "userId"));
    if (user == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "사용자를 찾을 수 없습니다.");
    return user;
  }

  public String requireAdmin(HttpServletRequest request) {
    Map<String,Object> user = requireAuth(request);
    String userId = value(user, "userId");
    if (mapper.isAdmin(userId) == 0) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "R09 시스템관리자 권한이 필요합니다.");
    return userId;
  }

  public List<Map<String,Object>> listUsers(Map<String,Object> params, HttpServletRequest request) { requireAdmin(request); return mapper.listUsers(paging(params)); }
  public Map<String,Object> getUser(String userId, HttpServletRequest request) { requireAdmin(request); Map<String,Object> user = mapper.getUser(userId); if (user == null) throw notFound("사용자"); return user; }

  @Transactional
  public Map<String,Object> updateUserUsage(String userId, UpdateUserUsageRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    if (!USE_YN.contains(body.useYn())) errors.add(new ApiResponse.FieldError("useYn", "Y 또는 N만 허용됩니다."));
    if (!StringUtils.hasText(body.changeReason())) errors.add(new ApiResponse.FieldError("changeReason", "변경 사유는 필수입니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
    Map<String,Object> before = mapper.getUser(userId);
    if (before == null) throw notFound("사용자");
    mapper.updateUserUsage(userId, body.useYn(), actor);
    Map<String,Object> after = mapper.getUser(userId);
    history("internal_user", userId, "UPDATE_USAGE", before, after, actor, body.changeReason());
    return after;
  }

  @Transactional
  public Map<String,Object> replaceUserRoles(String userId, ReplaceUserRolesRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    if (body.roles() == null || body.roles().isEmpty()) throw new ApiException(List.of(new ApiResponse.FieldError("roles", "하나 이상의 역할이 필요합니다.")));
    validateReason(body.changeReason());
    Map<String,Object> before = mapper.getUser(userId);
    if (before == null) throw notFound("사용자");
    for (int i = 0; i < body.roles().size(); i++) validateRoleInput("roles[" + i + "]", body.roles().get(i));
    mapper.revokeActiveRolesForUser(userId, actor, body.changeReason());
    for (UserRoleInput role : body.roles()) mapper.insertUserRole(id("UR"), userId, role.roleCode(), role.assignmentType(), role.approvedBy(), role.validFrom(), role.validTo(), body.changeReason(), actor);
    Map<String,Object> after = mapper.getUser(userId);
    history("user_role", userId, "REPLACE", before, after, actor, body.changeReason());
    return after;
  }

  public List<Map<String,Object>> listOrganizations(Map<String,Object> params, HttpServletRequest request) { requireAdmin(request); return mapper.listOrganizations(paging(params)); }
  public List<Map<String,Object>> organizationTree(HttpServletRequest request) { requireAdmin(request); return mapper.orgTreeRows(); }

  @Transactional
  public Map<String,Object> upsertOrganizationRelation(String orgCode, OrganizationRelationRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    if (!StringUtils.hasText(body.parentOrgCode())) errors.add(new ApiResponse.FieldError("parentOrgCode", "상위조직은 필수입니다."));
    if (body.effectiveStartDate() == null) errors.add(new ApiResponse.FieldError("effectiveStartDate", "적용 시작일은 필수입니다."));
    if (body.effectiveStartDate() != null && body.effectiveEndDate() != null && body.effectiveStartDate().isAfter(body.effectiveEndDate())) errors.add(new ApiResponse.FieldError("effectiveStartDate", "시작일은 종료일보다 늦을 수 없습니다."));
    if (orgCode.equals(body.parentOrgCode())) errors.add(new ApiResponse.FieldError("parentOrgCode", "자기 자신은 상위조직이 될 수 없습니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
    if (mapper.existsOrganization(orgCode) == 0 || mapper.existsOrganization(body.parentOrgCode()) == 0) throw notFound("조직");
    mapper.insertOrgRelation(id("ORH"), orgCode, body.parentOrgCode(), body.effectiveStartDate(), body.effectiveEndDate(), body.changeReason(), actor);
    Map<String,Object> after = new HashMap<>();
    after.put("orgCode", orgCode);
    after.put("parentOrgCode", body.parentOrgCode());
    history("organization_relation_history", orgCode, "UPSERT", Map.of(), after, actor, body.changeReason());
    return after;
  }

  public List<Map<String,Object>> listRoles(Map<String,Object> params, HttpServletRequest request) { requireAdmin(request); return mapper.listRoles(paging(params)); }

  @Transactional
  public Map<String,Object> upsertRolePolicy(RolePolicyRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    if (!ROLE_CODES.contains(body.roleCode())) errors.add(new ApiResponse.FieldError("roleCode", "R01~R09만 허용됩니다."));
    if (!StringUtils.hasText(body.purpose())) errors.add(new ApiResponse.FieldError("purpose", "목적은 필수입니다."));
    if (!StringUtils.hasText(body.assignmentCriteria())) errors.add(new ApiResponse.FieldError("assignmentCriteria", "부여 기준은 필수입니다."));
    if (!StringUtils.hasText(body.defaultDataScope())) errors.add(new ApiResponse.FieldError("defaultDataScope", "데이터 범위는 필수입니다."));
    if (body.useYn() != null && !USE_YN.contains(body.useYn())) errors.add(new ApiResponse.FieldError("useYn", "Y 또는 N만 허용됩니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
    mapper.updateRole(body.roleCode(), body.roleName(), body.purpose(), body.assignmentCriteria(), body.defaultDataScope(), body.useYn(), actor);
    Map<String,Object> after = mapper.listRoles(Map.of("roleCode", body.roleCode(), "offset", 0, "size", 1)).get(0);
    history("role", body.roleCode(), "UPSERT", Map.of(), after, actor, "역할 정책 저장");
    return after;
  }

  public List<Map<String,Object>> listUserRoles(Map<String,Object> params, HttpServletRequest request) { requireAdmin(request); return mapper.listUserRoles(paging(params)); }

  @Transactional
  public Map<String,Object> assignUserRole(UserRoleAssignmentRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    validateRoleInput("", new UserRoleInput(body.roleCode(), body.assignmentType(), body.approvedBy(), body.validFrom(), body.validTo()));
    if (!StringUtils.hasText(body.userId())) throw new ApiException(List.of(new ApiResponse.FieldError("userId", "사용자 ID는 필수입니다.")));
    if (mapper.getUser(body.userId()) == null) throw notFound("사용자");
    if (mapper.duplicateUserRole(body.userId(), body.roleCode(), body.validFrom(), body.validTo()) > 0) throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "동일 기간 역할이 이미 존재합니다.");
    String assignmentId = id("UR");
    mapper.insertUserRole(assignmentId, body.userId(), body.roleCode(), body.assignmentType(), body.approvedBy(), body.validFrom(), body.validTo(), "역할 부여", actor);
    history("user_role", assignmentId, "ASSIGN", Map.of(), Map.of("assignmentId", assignmentId), actor, "역할 부여");
    return Map.of("assignmentId", assignmentId, "status", "ACTIVE");
  }

  @Transactional
  public Map<String,Object> revokeUserRole(String assignmentId, RevokeRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    validateReason(body.changeReason());
    int updated = mapper.revokeAssignment(assignmentId, actor, body.changeReason());
    if (updated == 0) throw notFound("활성 사용자 역할");
    history("user_role", assignmentId, "REVOKE", Map.of(), Map.of("assignmentId", assignmentId, "status", "REVOKED"), actor, body.changeReason());
    return Map.of("assignmentId", assignmentId, "status", "REVOKED");
  }

  public List<Map<String,Object>> permissionMatrix(String targetType, String targetId, HttpServletRequest request) {
    requireAdmin(request);
    if (!TARGET_TYPES.contains(targetType) || !StringUtils.hasText(targetId)) throw new ApiException(List.of(new ApiResponse.FieldError("targetType", "권한 대상 유형과 ID를 확인하세요.")));
    return mapper.permissionMatrix(targetType, targetId);
  }

  @Transactional
  public Map<String,Object> savePermissionMatrix(MenuPermissionMatrixRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    if (!TARGET_TYPES.contains(body.targetType())) throw new ApiException(List.of(new ApiResponse.FieldError("targetType", "ROLE, ORGANIZATION, USER만 허용됩니다.")));
    if (body.permissions() == null || body.permissions().isEmpty()) throw new ApiException(List.of(new ApiResponse.FieldError("permissions", "저장할 권한이 필요합니다.")));
    for (MenuPermissionInput p : body.permissions()) {
      if (mapper.existsMenu(p.menuId()) == 0) throw notFound("메뉴");
      if (!USE_YN.contains(p.accessAllowedYn())) throw new ApiException(List.of(new ApiResponse.FieldError("permissions.accessAllowedYn", "Y 또는 N만 허용됩니다.")));
      mapper.upsertPermission(id("MP"), body.targetType(), body.targetId(), p.menuId(), p.accessAllowedYn(), json(p.functionPermissions() == null ? List.of() : p.functionPermissions()), actor);
    }
    List<Map<String,Object>> after = mapper.permissionMatrix(body.targetType(), body.targetId());
    history("menu_permission", body.targetType() + ":" + body.targetId(), "UPSERT", Map.of(), Map.of("items", after), actor, "메뉴 권한 저장");
    return Map.of("items", after);
  }

  public List<Map<String,Object>> menuTree(HttpServletRequest request) { requireAdmin(request); return mapper.menuTreeRows(); }

  @Transactional
  public Map<String,Object> moveMenu(String menuId, MenuMoveRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    if (body.displayOrder() == null || body.displayOrder() < 1) throw new ApiException(List.of(new ApiResponse.FieldError("displayOrder", "1 이상의 정수여야 합니다.")));
    Map<String,Object> target = mapper.menu(menuId);
    if (target == null) throw notFound("메뉴");
    int level = 1;
    if (StringUtils.hasText(body.parentMenuId())) {
      if (menuId.equals(body.parentMenuId())) throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "순환 부모는 허용되지 않습니다.");
      Map<String,Object> parent = mapper.menu(body.parentMenuId());
      if (parent == null) throw notFound("상위 메뉴");
      level = Integer.parseInt(String.valueOf(parent.get("menuLevel"))) + 1;
    }
    if (level > 3) throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "메뉴는 3단계를 초과할 수 없습니다.");
    mapper.moveMenu(menuId, body.parentMenuId(), level, body.displayOrder(), actor);
    history("menu", menuId, "MOVE", target, mapper.menu(menuId), actor, body.changeReason());
    return mapper.menu(menuId);
  }

  @Transactional
  public Map<String,Object> reorderMenus(MenuReorderRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    if (body.orderedMenuIds() == null || body.orderedMenuIds().isEmpty()) throw new ApiException(List.of(new ApiResponse.FieldError("orderedMenuIds", "순서 목록은 필수입니다.")));
    int order = 1;
    for (String menuId : body.orderedMenuIds()) mapper.updateMenuOrder(menuId, order++, actor);
    history("menu", String.valueOf(body.parentMenuId()), "REORDER", Map.of(), Map.of("orderedMenuIds", body.orderedMenuIds()), actor, body.changeReason());
    return Map.of("orderedMenuIds", body.orderedMenuIds());
  }

  public List<Map<String,Object>> listMenuInfo(Map<String,Object> params, HttpServletRequest request) { requireAdmin(request); return mapper.listMenuInfo(paging(params)); }

  @Transactional
  public Map<String,Object> upsertMenuInfo(MenuInfoRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    if (!StringUtils.hasText(body.menuName())) errors.add(new ApiResponse.FieldError("menuName", "메뉴명은 필수입니다."));
    if (!StringUtils.hasText(body.screenId())) errors.add(new ApiResponse.FieldError("screenId", "화면ID는 필수입니다."));
    if (!StringUtils.hasText(body.url()) || !body.url().startsWith("/system/")) errors.add(new ApiResponse.FieldError("url", "/system/... 형식이어야 합니다."));
    if (!USE_YN.contains(body.useYn())) errors.add(new ApiResponse.FieldError("useYn", "Y 또는 N만 허용됩니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
    if (mapper.duplicateActiveScreen(body.screenId(), body.menuId()) > 0) throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "활성 화면ID가 중복됩니다.");
    mapper.upsertMenuInfo(body.menuId(), body.menuName(), body.screenId(), body.url(), body.icon(), body.businessDomain(), body.description(), body.useYn(), actor);
    Map<String,Object> after = mapper.menu(body.menuId());
    history("menu", body.menuId(), "UPSERT_INFO", Map.of(), after, actor, body.changeReason());
    return after;
  }

  public List<Map<String,Object>> listCodeGroups(Map<String,Object> params, HttpServletRequest request) { requireAdmin(request); return mapper.listCodeGroups(paging(params)); }

  @Transactional
  public Map<String,Object> upsertCodeGroup(CodeGroupRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    if (!StringUtils.hasText(body.groupId())) errors.add(new ApiResponse.FieldError("groupId", "코드그룹 ID는 필수입니다."));
    if (!StringUtils.hasText(body.groupName())) errors.add(new ApiResponse.FieldError("groupName", "명칭은 필수입니다."));
    if (!StringUtils.hasText(body.managingDepartment())) errors.add(new ApiResponse.FieldError("managingDepartment", "관리부서는 필수입니다."));
    if (!USE_YN.contains(body.useYn())) errors.add(new ApiResponse.FieldError("useYn", "Y 또는 N만 허용됩니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
    mapper.upsertCodeGroup(body.groupId(), body.groupName(), body.description(), body.managingDepartment(), body.useYn(), actor);
    List<Map<String,Object>> after = mapper.listCodeGroups(Map.of("groupId", body.groupId(), "offset", 0, "size", 1));
    history("code_group", body.groupId(), "UPSERT", Map.of(), after.isEmpty() ? Map.of() : after.get(0), actor, body.changeReason());
    return after.isEmpty() ? Map.of("groupId", body.groupId()) : after.get(0);
  }

  public List<Map<String,Object>> listCodeDetails(Map<String,Object> params, HttpServletRequest request) {
    requireAdmin(request);
    if (!StringUtils.hasText((String)params.get("groupId"))) throw new ApiException(List.of(new ApiResponse.FieldError("groupId", "코드그룹 ID는 필수입니다.")));
    return mapper.listCodeDetails(paging(params));
  }

  @Transactional
  public Map<String,Object> upsertCodeDetail(CodeDetailRequest body, HttpServletRequest request) {
    String actor = requireAdmin(request);
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    if (!StringUtils.hasText(body.groupId())) errors.add(new ApiResponse.FieldError("groupId", "코드그룹 ID는 필수입니다."));
    if (!StringUtils.hasText(body.codeValue())) errors.add(new ApiResponse.FieldError("codeValue", "코드값은 필수입니다."));
    if (!StringUtils.hasText(body.codeName())) errors.add(new ApiResponse.FieldError("codeName", "코드명은 필수입니다."));
    if (body.displayOrder() == null || body.displayOrder() < 1) errors.add(new ApiResponse.FieldError("displayOrder", "1 이상의 정수여야 합니다."));
    if (!USE_YN.contains(body.useYn())) errors.add(new ApiResponse.FieldError("useYn", "Y 또는 N만 허용됩니다."));
    if (body.validFrom() != null && body.validTo() != null && body.validFrom().isAfter(body.validTo())) errors.add(new ApiResponse.FieldError("validFrom", "시작일은 종료일보다 늦을 수 없습니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
    if (mapper.existsCodeGroup(body.groupId()) == 0) throw notFound("코드그룹");
    if (StringUtils.hasText(body.parentCodeValue()) && mapper.existsCodeDetail(body.groupId(), body.parentCodeValue()) == 0) throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "상위코드는 같은 그룹에 존재해야 합니다.");
    mapper.upsertCodeDetail(body.groupId(), body.codeValue(), body.codeName(), body.parentCodeValue(), body.displayOrder(), json(body.extraAttributes() == null ? Map.of() : body.extraAttributes()), body.useYn(), body.validFrom(), body.validTo(), actor);
    List<Map<String,Object>> after = mapper.listCodeDetails(Map.of("groupId", body.groupId(), "codeValue", body.codeValue(), "offset", 0, "size", 1));
    history("code_detail", body.groupId() + ":" + body.codeValue(), "UPSERT", Map.of(), after.isEmpty() ? Map.of() : after.get(0), actor, body.changeReason());
    return after.isEmpty() ? Map.of("groupId", body.groupId(), "codeValue", body.codeValue()) : after.get(0);
  }

  private void validateRoleInput(String prefix, UserRoleInput role) {
    List<ApiResponse.FieldError> errors = new ArrayList<>();
    String p = StringUtils.hasText(prefix) ? prefix + "." : "";
    if (!ROLE_CODES.contains(role.roleCode())) errors.add(new ApiResponse.FieldError(p + "roleCode", "R01~R09만 허용됩니다."));
    if (!ASSIGNMENT_TYPES.contains(role.assignmentType())) errors.add(new ApiResponse.FieldError(p + "assignmentType", "POSITION_BASED 또는 MANUAL만 허용됩니다."));
    if (!StringUtils.hasText(role.approvedBy())) errors.add(new ApiResponse.FieldError(p + "approvedBy", "승인자는 필수입니다."));
    if (role.validFrom() == null) errors.add(new ApiResponse.FieldError(p + "validFrom", "유효 시작일은 필수입니다."));
    if (role.validFrom() != null && role.validTo() != null && role.validFrom().isAfter(role.validTo())) errors.add(new ApiResponse.FieldError(p + "validFrom", "시작일은 종료일보다 늦을 수 없습니다."));
    if (!errors.isEmpty()) throw new ApiException(errors);
  }

  private void validateReason(String reason) {
    if (!StringUtils.hasText(reason)) throw new ApiException(List.of(new ApiResponse.FieldError("changeReason", "변경 사유는 필수입니다.")));
  }

  private Map<String,Object> paging(Map<String,Object> params) {
    Map<String,Object> result = new HashMap<>(params);
    int page = parseInt(params.get("page"), 0);
    int size = Math.min(Math.max(parseInt(params.get("size"), 20), 1), 100);
    result.put("offset", page * size);
    result.put("size", size);
    return result;
  }

  private int parseInt(Object value, int fallback) {
    if (value == null || !StringUtils.hasText(String.valueOf(value))) return fallback;
    return Integer.parseInt(String.valueOf(value));
  }

  private String sessionId(HttpServletRequest request) {
    if (request.getCookies() == null) return null;
    for (Cookie cookie : request.getCookies()) if (SESSION_COOKIE.equals(cookie.getName())) return cookie.getValue();
    return null;
  }

  private Map<String,Object> currentUserData(Map<String,Object> user) {
    Map<String,Object> data = new HashMap<>();
    data.put("user", user);
    data.put("menuRoutes", mapper.permissionMatrix("ROLE", "R09").stream().filter(row -> "Y".equals(String.valueOf(row.get("accessAllowedYn")))).map(row -> row.get("url")).toList());
    return data;
  }

  private String value(Map<String,Object> map, String key) { return String.valueOf(map.get(key)); }
  private String id(String prefix) { return prefix + "-" + UUID.randomUUID(); }
  private ApiException notFound(String entity) { return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", entity + "을(를) 찾을 수 없습니다."); }
  private void history(String entity,String entityId,String op,Object before,Object after,String actor,String reason) { mapper.history(id("H"), entity, entityId, op, json(before), json(after), actor, reason == null ? "" : reason); }
  private String json(Object value) { try { return objectMapper.writeValueAsString(value); } catch (JsonProcessingException ex) { return "{}"; } }
  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception ex) { throw new IllegalStateException(ex); }
  }
}
