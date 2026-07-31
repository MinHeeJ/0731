package kr.ac.knue.cms.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.knue.cms.api.ApiException;
import kr.ac.knue.cms.api.ApiResponse;
import kr.ac.knue.cms.api.CurrentUser;
import kr.ac.knue.cms.api.RequestContext;
import kr.ac.knue.cms.persistence.CommonMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final CommonMapper mapper;

    public AdminService(CommonMapper mapper) {
        this.mapper = mapper;
    }
    public ApiResponse<Object> listUsers(String q,
                                         String roleCode,
                                         String useYn) {
        return ApiResponse.ok(mapper.listUsers(q, roleCode, useYn));
    }
    public ApiResponse<Object> updateUserAccount(String userId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        String useYn = str(body, "useYn");
        requireAllowed("useYn", useYn, List.of("Y", "N"));
        String status = "Y".equals(useYn) ? "ACTIVE" : "INACTIVE";
        int changed = mapper.updateUserAccount(userId, useYn, status);
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "사용자를 찾을 수 없습니다");
        mapper.insertHistory("app_user", userId, null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("userId", userId, "useYn", useYn, "status", status));
    }
    public ApiResponse<Object> replaceUserBusinessRoles(String userId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        Object values = body.get("roleCodes");
        if (!(values instanceof List<?> roles) || roles.isEmpty()) throw new ApiException(400, "VALIDATION_ERROR", "roleCodes는 필수입니다");
        mapper.revokeUserRoles(userId);
        for (Object role : roles) mapper.insertUserRole(userId, String.valueOf(role), actor.userId());
        mapper.insertHistory("user_role", userId, null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("userId", userId, "roleCodes", roles));
    }
    public ApiResponse<Object> listOrganizations(String q) {
        return ApiResponse.ok(mapper.listOrgs(q));
    }
    public ApiResponse<Object> getOrganizationTree(String q) {
        return ApiResponse.ok(mapper.listOrgTree(q));
    }
    public ApiResponse<Object> updateOrganizationRelation(Long relationId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        validateDateRange(str(body, "validFrom"), str(body, "validTo"));
        int changed = mapper.updateOrgRelation(relationId, str(body, "parentOrgCode"), str(body, "validFrom"), str(body, "validTo"), str(body, "reason"));
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "조직 관계를 찾을 수 없습니다");
        mapper.insertHistory("org_relation", String.valueOf(relationId), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("relationId", relationId));
    }
    public ApiResponse<Object> listRoles(String q, String useYn) {
        return ApiResponse.ok(mapper.listRoles(q, useYn));
    }
    public ApiResponse<Object> updateRole(String roleCode, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        if (body.containsKey("roleCode") && !roleCode.equals(str(body, "roleCode"))) throw new ApiException(400, "VALIDATION_ERROR", "역할코드는 변경할 수 없습니다");
        require(str(body, "roleName"), "roleName");
        int changed = mapper.updateRole(roleCode, str(body, "roleName"), str(body, "purpose"), str(body, "grantCriteria"), str(body, "defaultDataScope"), defaultValue(str(body, "useYn"), "Y"));
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "역할을 찾을 수 없습니다");
        mapper.insertHistory("role", roleCode, null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("roleCode", roleCode));
    }
    public ApiResponse<Object> listUserRoles(String q, String roleCode, String status) {
        return ApiResponse.ok(mapper.listUserRoles(q, roleCode, status));
    }
    public ApiResponse<Object> grantUserRole(Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        require(str(body, "userId"), "userId");
        require(str(body, "roleCode"), "roleCode");
        validateDateRange(str(body, "validFrom"), str(body, "validTo"));
        body.putIfAbsent("assignmentType", "MANUAL");
        mapper.grantUserRole(body);
        mapper.insertHistory("user_role", String.valueOf(body.get("assignmentId")), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(body);
    }
    public ApiResponse<Object> updateUserRole(Long assignmentId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        body.put("assignmentId", assignmentId);
        validateDateRange(str(body, "validFrom"), str(body, "validTo"));
        body.putIfAbsent("status", "ACTIVE");
        int changed = mapper.updateUserRole(body);
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "사용자 역할을 찾을 수 없습니다");
        mapper.insertHistory("user_role", String.valueOf(assignmentId), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("assignmentId", assignmentId));
    }
    public ApiResponse<Object> revokeUserRole(Long assignmentId, String reason) {
        CurrentUser actor = RequestContext.require();
        int changed = mapper.revokeUserRole(assignmentId);
        if (changed == 0) throw new ApiException(409, "CONFLICT", "이미 회수되었거나 존재하지 않는 역할입니다");
        mapper.insertHistory("user_role", String.valueOf(assignmentId), null, json(Map.of("status", "REVOKED")), actor.userId(), reason);
        return ApiResponse.ok(Map.of("assignmentId", assignmentId, "status", "REVOKED"));
    }
    public ApiResponse<Object> listMenuPermissions(String targetType, String targetId) {
        return ApiResponse.ok(mapper.listMenuPermissions(targetType, targetId));
    }
    public ApiResponse<Object> saveMenuPermissions(Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        String targetType = defaultValue(str(body, "targetType"), "ROLE");
        String targetId = str(body, "targetId");
        require(targetId, "targetId");
        Object entries = body.get("permissions");
        if (!(entries instanceof List<?> permissions)) throw new ApiException(400, "VALIDATION_ERROR", "permissions는 필수입니다");
        mapper.deleteMenuPermissions(targetType, targetId);
        for (Object entry : permissions) {
            if (entry instanceof Map<?, ?> row) {
                mapper.insertMenuPermission(targetType, targetId, number(row.get("menuId")).longValue(), Boolean.parseBoolean(String.valueOf(row.get("accessAllowed"))));
            }
        }
        mapper.insertHistory("menu_permission", targetType + ":" + targetId, null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("targetType", targetType, "targetId", targetId));
    }
    public ApiResponse<Object> getMenuTree() {
        return ApiResponse.ok(mapper.getMenuTree());
    }
    public ApiResponse<Object> updateMenuStructure(Long menuId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        Long parentMenuId = body.get("parentMenuId") == null ? null : number(body.get("parentMenuId")).longValue();
        if (parentMenuId != null && parentMenuId.equals(menuId)) throw new ApiException(400, "VALIDATION_ERROR", "자기 자신을 부모 메뉴로 지정할 수 없습니다");
        Integer displayOrder = body.get("displayOrder") == null ? 10 : number(body.get("displayOrder")).intValue();
        int changed = mapper.updateMenuStructure(menuId, parentMenuId, displayOrder);
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "메뉴를 찾을 수 없습니다");
        mapper.insertHistory("menu", String.valueOf(menuId), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("menuId", menuId));
    }
    public ApiResponse<Object> reorderMenus(Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        Object entries = body.get("orders");
        if (!(entries instanceof List<?> orders)) throw new ApiException(400, "VALIDATION_ERROR", "orders는 필수입니다");
        for (Object entry : orders) {
            if (entry instanceof Map<?, ?> row) mapper.updateMenuOrder(number(row.get("menuId")).longValue(), number(row.get("displayOrder")).intValue());
        }
        mapper.insertHistory("menu", "reorder", null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("count", orders.size()));
    }
    public ApiResponse<Object> listMenus(String q) {
        return ApiResponse.ok(mapper.listMenus(q));
    }
    public ApiResponse<Object> createMenu(Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        require(str(body, "menuName"), "menuName");
        body.putIfAbsent("useYn", "Y");
        body.putIfAbsent("displayOrder", 100);
        body.putIfAbsent("menuType", "SCREEN");
        mapper.createMenu(body);
        mapper.insertHistory("menu", String.valueOf(body.get("menuId")), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(body);
    }
    public ApiResponse<Object> updateMenu(Long menuId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        body.put("menuId", menuId);
        require(str(body, "menuName"), "menuName");
        int changed = mapper.updateMenu(body);
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "메뉴를 찾을 수 없습니다");
        mapper.insertHistory("menu", String.valueOf(menuId), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("menuId", menuId));
    }
    public ApiResponse<Object> listCodeGroups(String q, String useYn) {
        return ApiResponse.ok(mapper.listCodeGroups(q, useYn));
    }
    public ApiResponse<Object> createCodeGroup(Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        require(str(body, "groupId"), "groupId");
        require(str(body, "groupName"), "groupName");
        body.putIfAbsent("useYn", "Y");
        mapper.createCodeGroup(body);
        mapper.insertHistory("code_group", str(body, "groupId"), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(body);
    }
    public ApiResponse<Object> updateCodeGroup(String groupId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        if (body.containsKey("groupId") && !groupId.equals(str(body, "groupId"))) throw new ApiException(400, "VALIDATION_ERROR", "그룹ID는 변경할 수 없습니다");
        body.put("groupId", groupId);
        require(str(body, "groupName"), "groupName");
        int changed = mapper.updateCodeGroup(body);
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "코드그룹을 찾을 수 없습니다");
        mapper.insertHistory("code_group", groupId, null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("groupId", groupId));
    }
    public ApiResponse<Object> listCodeDetails(String groupId, String q) {
        return ApiResponse.ok(mapper.listCodeDetails(groupId, q));
    }
    public ApiResponse<Object> createCodeDetail(String groupId, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        body.put("groupId", groupId);
        require(str(body, "codeValue"), "codeValue");
        require(str(body, "codeName"), "codeName");
        validateDateRange(str(body, "validFrom"), str(body, "validTo"));
        body.putIfAbsent("sortOrder", 10);
        body.putIfAbsent("extraAttributes", "{}");
        body.putIfAbsent("useYn", "Y");
        mapper.createCodeDetail(body);
        mapper.insertHistory("code_detail", groupId + ":" + str(body, "codeValue"), null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(body);
    }
    public ApiResponse<Object> updateCodeDetail(String groupId, String codeValue, Map<String, Object> body) {
        CurrentUser actor = RequestContext.require();
        body.put("groupId", groupId);
        body.put("codeValue", codeValue);
        require(str(body, "codeName"), "codeName");
        validateDateRange(str(body, "validFrom"), str(body, "validTo"));
        body.putIfAbsent("extraAttributes", "{}");
        int changed = mapper.updateCodeDetail(body);
        if (changed == 0) throw new ApiException(404, "NOT_FOUND", "상세코드를 찾을 수 없습니다");
        mapper.insertHistory("code_detail", groupId + ":" + codeValue, null, json(body), actor.userId(), str(body, "reason"));
        return ApiResponse.ok(Map.of("groupId", groupId, "codeValue", codeValue));
    }

    private void validateDateRange(String from, String to) {
        if (from != null && to != null && LocalDate.parse(to).isBefore(LocalDate.parse(from))) {
            throw new ApiException(400, "VALIDATION_ERROR", "종료일은 시작일보다 빠를 수 없습니다");
        }
    }

    private void require(String value, String field) {
        if (value == null || value.isBlank()) throw new ApiException(400, "VALIDATION_ERROR", field + "는 필수입니다");
    }

    private void requireAllowed(String field, String value, List<String> allowed) {
        require(value, field);
        if (!allowed.contains(value)) throw new ApiException(400, "VALIDATION_ERROR", field + " 허용값이 아닙니다");
    }

    private String str(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String defaultValue(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private Number number(Object value) {
        if (value instanceof Number number) return number;
        return Long.parseLong(String.valueOf(value));
    }

    private String json(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new ApiException(400, "VALIDATION_ERROR", "JSON 형식이 올바르지 않습니다");
        }
    }
}
