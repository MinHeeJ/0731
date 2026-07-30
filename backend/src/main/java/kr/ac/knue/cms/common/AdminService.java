package kr.ac.knue.cms.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;

@Service
public class AdminService {
  private final AdminMapper mapper;
  private final SessionMapper sessionMapper;

  public AdminService(AdminMapper mapper, SessionMapper sessionMapper) { this.mapper = mapper; this.sessionMapper = sessionMapper; }

  public ApiResponse<List<Map<String,Object>>> listUsers(String q, String filter, int size, int page) {
    return ApiResponse.ok(mapper.listUsers(q, filter, clamp(size), page * clamp(size)));
  }

  public ResponseEntity<ApiResponse<?>> updateUserAccount(String userId, Map<String,Object> body, HttpServletRequest request) {
    if (body.containsKey("name") || body.containsKey("orgCode") || body.containsKey("rankName")) return bad("korus", "KORUS 원천 필드는 수정할 수 없습니다");
    String useYn = str(body.get("useYn"));
    if (!List.of("Y","N").contains(useYn)) return bad("useYn", "Y 또는 N만 허용됩니다");
    int updated = mapper.updateUserAccount(userId, useYn);
    if (updated == 0) return notFound("userId");
    history(request, "app_user", userId, "updateUserAccount", str(body.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("userId", userId, "useYn", useYn)));
  }

  public ResponseEntity<ApiResponse<?>> replaceRoles(String userId, Map<String,Object> body, HttpServletRequest request) {
    Object roleCodes = body.get("roleCodes");
    if (!(roleCodes instanceof List<?> list) || list.isEmpty()) return bad("roleCodes", "하나 이상의 역할이 필요합니다");
    mapper.deleteManualRoles(userId);
    for (Object role : list) {
      String roleCode = str(role);
      if (!roleCode.matches("R0[1-9]")) return bad("roleCodes", "R01~R09만 허용됩니다");
      mapper.insertManualRole(userId, roleCode, actor(request));
    }
    history(request, "user_role", userId, "replaceUserBusinessRoles", str(body.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("userId", userId, "roleCodes", roleCodes)));
  }

  public ApiResponse<List<Map<String,Object>>> orgs(String q, int size, int page) {
    return ApiResponse.ok(mapper.simpleList("org", "org_code", q, clamp(size), page * clamp(size)));
  }

  public ApiResponse<List<Map<String,Object>>> orgTree() { return ApiResponse.ok(mapper.orgTree()); }

  public ResponseEntity<ApiResponse<?>> updateOrgRelation(long relationId, Map<String,Object> body, HttpServletRequest request) {
    String validFrom = str(body.get("validFrom")); String validTo = str(body.get("validTo"));
    if (validFrom.isBlank()) return bad("validFrom", "적용시작일은 필수입니다");
    if (!validTo.isBlank() && LocalDate.parse(validTo).isBefore(LocalDate.parse(validFrom))) return bad("validTo", "종료일은 시작일보다 빠를 수 없습니다");
    int updated = mapper.updateOrgRelation(relationId, blankToNull(str(body.get("parentOrgCode"))), validFrom, blankToNull(validTo), str(body.get("reason")));
    if (updated == 0) return notFound("relationId");
    history(request, "org_relation", String.valueOf(relationId), "updateOrganizationRelation", str(body.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("relationId", relationId)));
  }

  public ApiResponse<List<Map<String,Object>>> roles(String q, int size, int page) { return ApiResponse.ok(mapper.simpleList("role", "role_code", q, clamp(size), page * clamp(size))); }

  public ResponseEntity<ApiResponse<?>> updateRole(String roleCode, Map<String,Object> body, HttpServletRequest request) {
    if (body.containsKey("roleCode") && !roleCode.equals(str(body.get("roleCode")))) return bad("roleCode", "역할코드는 변경할 수 없습니다");
    if (str(body.get("roleName")).isBlank()) return bad("roleName", "역할명은 필수입니다");
    mapper.updateRole(roleCode, str(body.get("roleName")), str(body.get("purpose")), str(body.get("grantCriteria")), str(body.get("defaultDataScope")), defaultUseYn(body));
    history(request, "role", roleCode, "updateRole", str(body.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("roleCode", roleCode)));
  }

  public ApiResponse<List<Map<String,Object>>> userRoles(String q, int size, int page) { return ApiResponse.ok(mapper.simpleList("user_role", "assignment_id", q, clamp(size), page * clamp(size))); }

  public ResponseEntity<ApiResponse<?>> grantUserRole(Map<String,Object> row, HttpServletRequest request) {
    ResponseEntity<ApiResponse<?>> invalid = validateRolePeriod(row); if (invalid != null) return invalid;
    row.put("approverId", actor(request)); row.putIfAbsent("assignmentType", "MANUAL");
    mapper.grantUserRole(row); history(request, "user_role", str(row.get("assignmentId")), "grantUserRole", str(row.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(row));
  }

  public ResponseEntity<ApiResponse<?>> updateUserRole(long assignmentId, Map<String,Object> row, HttpServletRequest request) {
    row.put("assignmentId", assignmentId); row.putIfAbsent("status", "ACTIVE"); row.put("approverId", actor(request));
    ResponseEntity<ApiResponse<?>> invalid = validateRolePeriod(row); if (invalid != null) return invalid;
    mapper.updateUserRole(row); history(request, "user_role", String.valueOf(assignmentId), "updateUserRole", str(row.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(row));
  }

  public ResponseEntity<ApiResponse<?>> revokeUserRole(long assignmentId, HttpServletRequest request) {
    int updated = mapper.revokeUserRole(assignmentId); if (updated == 0) return notFound("assignmentId");
    history(request, "user_role", String.valueOf(assignmentId), "revokeUserRole", "회수");
    return ResponseEntity.ok(ApiResponse.ok(Map.of("assignmentId", assignmentId, "status", "REVOKED")));
  }

  public ApiResponse<List<Map<String,Object>>> permissions(String targetType, String targetId) { return ApiResponse.ok(mapper.listMenuPermissions(targetType, targetId)); }

  public ResponseEntity<ApiResponse<?>> savePermissions(Map<String,Object> body, HttpServletRequest request) {
    String targetType=str(body.get("targetType")); String targetId=str(body.get("targetId"));
    if (targetType.isBlank()) return bad("targetType", "대상 유형은 필수입니다");
    if (targetId.isBlank()) return bad("targetId", "대상 ID는 필수입니다");
    mapper.deletePermissions(targetType, targetId);
    Object items=body.get("items");
    if (items instanceof List<?> list) for (Object o:list) if (o instanceof Map<?,?> m) mapper.insertPermission(targetType, targetId, Long.parseLong(str(m.get("menuId"))), Boolean.parseBoolean(str(m.get("accessAllowed"))));
    history(request, "menu_permission", targetType+":"+targetId, "saveMenuPermissions", str(body.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("targetType", targetType, "targetId", targetId)));
  }

  public ApiResponse<List<Map<String,Object>>> menuTree() { return ApiResponse.ok(mapper.menuTree()); }

  public ApiResponse<List<Map<String,Object>>> menus(String q, int size, int page) {
    return ApiResponse.ok(mapper.simpleList("menu", "display_order", q, clamp(size), page * clamp(size)));
  }

  public ResponseEntity<ApiResponse<?>> createMenu(Map<String,Object> row, HttpServletRequest request) {
    Map<String,Object> values = new HashMap<>(row);
    ResponseEntity<ApiResponse<?>> invalid = validateMenu(values); if (invalid != null) return invalid;
    values.putIfAbsent("menuType", "SCREEN"); values.putIfAbsent("displayOrder", 0); values.putIfAbsent("useYn", "Y");
    mapper.createMenu(values); history(request, "menu", str(values.get("menuId")), "createMenu", str(values.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(values));
  }

  public ResponseEntity<ApiResponse<?>> updateMenu(long menuId, Map<String,Object> row, HttpServletRequest request) {
    Map<String,Object> values = new HashMap<>(row); values.put("menuId", menuId); values.putIfAbsent("useYn", "Y");
    ResponseEntity<ApiResponse<?>> invalid = validateMenu(values); if (invalid != null) return invalid;
    int updated = mapper.updateMenu(values); if (updated == 0) return notFound("menuId");
    history(request, "menu", String.valueOf(menuId), "updateMenu", str(values.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("menuId", menuId)));
  }

  public ApiResponse<List<Map<String,Object>>> codeGroups(String q, int size, int page) {
    return ApiResponse.ok(mapper.simpleList("code_group", "group_id", q, clamp(size), page * clamp(size)));
  }

  public ResponseEntity<ApiResponse<?>> createCodeGroup(Map<String,Object> row, HttpServletRequest request) {
    Map<String,Object> values = new HashMap<>(row);
    if (str(values.get("groupId")).isBlank()) return bad("groupId", "코드그룹 식별자는 필수입니다");
    if (str(values.get("groupName")).isBlank()) return bad("groupName", "코드그룹명은 필수입니다");
    values.putIfAbsent("useYn", "Y");
    mapper.createCodeGroup(values); history(request, "code_group", str(values.get("groupId")), "createCodeGroup", str(values.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(values));
  }

  public ResponseEntity<ApiResponse<?>> updateCodeGroup(String groupId, Map<String,Object> row, HttpServletRequest request) {
    Map<String,Object> values = new HashMap<>(row); values.put("groupId", groupId); values.putIfAbsent("useYn", "Y");
    if (str(values.get("groupName")).isBlank()) return bad("groupName", "코드그룹명은 필수입니다");
    int updated = mapper.updateCodeGroup(values); if (updated == 0) return notFound("groupId");
    history(request, "code_group", groupId, "updateCodeGroup", str(values.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("groupId", groupId)));
  }

  public ApiResponse<List<Map<String,Object>>> codeDetails(String groupId, String q, int size, int page) {
    return ApiResponse.ok(mapper.codeDetails(groupId, q, clamp(size), page * clamp(size)));
  }

  public ResponseEntity<ApiResponse<?>> createCodeDetail(String groupId, Map<String,Object> row, HttpServletRequest request) {
    Map<String,Object> values = new HashMap<>(row); values.put("groupId", groupId); values.putIfAbsent("sortOrder", 0); values.putIfAbsent("useYn", "Y");
    ResponseEntity<ApiResponse<?>> invalid = validateCode(values); if (invalid != null) return invalid;
    mapper.createCodeDetail(values); history(request, "code_detail", groupId + ":" + str(values.get("codeValue")), "createCodeDetail", str(values.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(values));
  }

  public ResponseEntity<ApiResponse<?>> updateCodeDetail(String groupId, String codeValue, Map<String,Object> row, HttpServletRequest request) {
    Map<String,Object> values = new HashMap<>(row); values.put("groupId", groupId); values.put("codeValue", codeValue); values.putIfAbsent("sortOrder", 0); values.putIfAbsent("useYn", "Y");
    ResponseEntity<ApiResponse<?>> invalid = validateCode(values); if (invalid != null) return invalid;
    int updated = mapper.updateCodeDetail(values); if (updated == 0) return notFound("codeValue");
    history(request, "code_detail", groupId + ":" + codeValue, "updateCodeDetail", str(values.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("groupId", groupId, "codeValue", codeValue)));
  }

  public ResponseEntity<ApiResponse<?>> updateMenuStructure(long menuId, Map<String,Object> body, HttpServletRequest request) {
    Long parent = body.get("parentMenuId") == null ? null : Long.valueOf(str(body.get("parentMenuId")));
    if (parent != null && parent == menuId) return bad("parentMenuId", "자기 자신을 부모로 지정할 수 없습니다");
    mapper.updateMenuStructure(menuId, parent, Integer.parseInt(str(body.getOrDefault("displayOrder", "0"))));
    history(request, "menu", String.valueOf(menuId), "updateMenuStructure", str(body.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("menuId", menuId)));
  }

  public ResponseEntity<ApiResponse<?>> reorderMenus(Map<String,Object> body, HttpServletRequest request) {
    Object items=body.get("items"); if (!(items instanceof List<?> list)) return bad("items", "정렬 항목은 필수입니다");
    for (Object o:list) if (o instanceof Map<?,?> m) mapper.updateMenuOrder(Long.parseLong(str(m.get("menuId"))), Integer.parseInt(str(m.get("displayOrder"))));
    history(request, "menu", "reorder", "reorderMenus", str(body.get("reason")));
    return ResponseEntity.ok(ApiResponse.ok(Map.of("count", list.size())));
  }




  private int clamp(int size){ return Math.max(1, Math.min(size, 100)); }
  private String actor(HttpServletRequest request){ return ((SessionUser)request.getAttribute("sessionUser")).userId(); }
  private void history(HttpServletRequest request,String entity,String id,String op,String reason){ sessionMapper.insertHistory(entity,id,"{}","{\"operationId\":\""+op+"\"}",actor(request),reason); }
  private String str(Object o){ return o==null?"":String.valueOf(o); }
  private String blankToNull(String s){ return s==null||s.isBlank()?null:s; }
  private String defaultUseYn(Map<String,Object> body){ String v=str(body.get("useYn")); return v.isBlank()?"Y":v; }
  private ResponseEntity<ApiResponse<?>> bad(String field,String reason){ return ResponseEntity.badRequest().body(ApiResponse.fieldError("VALIDATION_ERROR","입력값을 확인하세요",field,reason)); }
  private ResponseEntity<ApiResponse<?>> notFound(String field){ return ResponseEntity.status(404).body(ApiResponse.fieldError("NOT_FOUND","대상을 찾을 수 없습니다",field,"존재하지 않습니다")); }
  private ResponseEntity<ApiResponse<?>> validateRolePeriod(Map<String,Object> row){ if(str(row.get("userId")).isBlank())return bad("userId","사용자는 필수입니다"); if(!str(row.get("roleCode")).matches("R0[1-9]"))return bad("roleCode","R01~R09만 허용됩니다"); String from=str(row.get("validFrom")); String to=str(row.get("validTo")); if(from.isBlank())return bad("validFrom","유효시작일은 필수입니다"); if(!to.isBlank()&&LocalDate.parse(to).isBefore(LocalDate.parse(from)))return bad("validTo","종료일은 시작일보다 빠를 수 없습니다"); return null; }
  private ResponseEntity<ApiResponse<?>> validateMenu(Map<String,Object> row){ if(str(row.get("menuName")).isBlank())return bad("menuName","메뉴명은 필수입니다"); if(str(row.get("url")).isBlank()||!str(row.get("url")).startsWith("/admin/"))return bad("url","/admin/ 경로만 허용됩니다"); return null; }
  private ResponseEntity<ApiResponse<?>> validateCode(Map<String,Object> row){ if(str(row.get("groupId")).isBlank())return bad("groupId","코드그룹 식별자는 필수입니다"); if(str(row.get("codeValue")).isBlank())return bad("codeValue","코드값은 필수입니다"); if(str(row.get("codeName")).isBlank())return bad("codeName","코드명은 필수입니다"); String from=str(row.get("validFrom")); String to=str(row.get("validTo")); if(!from.isBlank()&&!to.isBlank()&&LocalDate.parse(to).isBefore(LocalDate.parse(from)))return bad("validTo","종료일은 시작일보다 빠를 수 없습니다"); return null; }
}
