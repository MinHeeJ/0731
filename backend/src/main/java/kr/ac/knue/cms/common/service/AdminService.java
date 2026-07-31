package kr.ac.knue.cms.common.service;

import kr.ac.knue.cms.common.domain.GenericSaveRequest;
import kr.ac.knue.cms.common.domain.SessionUser;
import kr.ac.knue.cms.common.domain.UsageUpdateRequest;
import kr.ac.knue.cms.common.domain.UserRolesRequest;
import kr.ac.knue.cms.common.mapper.CommonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminService {
    private final CommonMapper mapper;

    public AdminService(CommonMapper mapper) {
        this.mapper = mapper;
    }

    public Map<String, Object> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("application", "KNUE 교수업적평가 공통관리");
        data.put("decisionStatus", "clarification_required");
        data.put("persistence", "mybatis/postgresql/flyway");
        data.put("checks", mapper.listDeliveryStatus());
        return data;
    }

    public Map<String, Object> page(List<Map<String, Object>> items, int page, int size) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", items);
        data.put("page", page);
        data.put("size", size);
        data.put("count", items.size());
        return data;
    }

    public List<Map<String, Object>> users(String filter, int page, int size) { return mapper.searchUsers(clean(filter), offset(page, size), size); }
    public List<Map<String, Object>> organizations(String filter, int page, int size) { return mapper.searchOrganizations(clean(filter), offset(page, size), size); }
    public List<Map<String, Object>> roles(String filter, int page, int size) { return mapper.listRoles(clean(filter), offset(page, size), size); }
    public List<Map<String, Object>> userRoles(String filter, int page, int size) { return mapper.searchUserRoles(clean(filter), offset(page, size), size); }
    public List<Map<String, Object>> menuPermissions(String targetType, String targetId) { return mapper.getMenuPermissions(clean(targetType), clean(targetId)); }
    public List<Map<String, Object>> menuTree() { return mapper.getMenuTree(); }
    public List<Map<String, Object>> menus(String filter, int page, int size) { return mapper.searchMenus(clean(filter), offset(page, size), size); }
    public List<Map<String, Object>> codeGroups(String filter, int page, int size) { return mapper.searchCodeGroups(clean(filter), offset(page, size), size); }
    public List<Map<String, Object>> detailCodes(String groupId) { require(groupId, "groupId"); return mapper.listDetailCodes(groupId); }
    public List<Map<String, Object>> myMenus(SessionUser user) { ensureAdmin(user); return mapper.getMenusForRoles(user.roleCodes()); }

    @Transactional
    public Map<String, Object> updateUserUsage(String userId, UsageUpdateRequest request, SessionUser actor) {
        ensureAdmin(actor); require(userId, "userId");
        Map<String, Object> before = mapper.findUserById(userId);
        if (before == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        int updated = mapper.updateUserUsage(userId, request.systemUseYn());
        if (updated == 0) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        mapper.insertChangeHistory("user_account", userId, "UPDATE", String.valueOf(before), "systemUseYn=" + request.systemUseYn(), request.reason(), actor.userId());
        return Map.of("userId", userId, "systemUseYn", request.systemUseYn(), "changed", true);
    }

    @Transactional
    public Map<String, Object> replaceUserRoles(String userId, UserRolesRequest request, SessionUser actor) {
        ensureAdmin(actor); require(userId, "userId");
        if (mapper.findUserById(userId) == null) throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        mapper.deactivateUserRoles(userId);
        for (String roleCode : request.roleCodes()) {
            validateRoleCode(roleCode);
            mapper.insertUserRole(userId, roleCode, actor.userId());
        }
        mapper.insertChangeHistory("user_role_assignment", userId, "UPDATE", "previous active roles revoked", String.valueOf(request.roleCodes()), request.reason(), actor.userId());
        return Map.of("userId", userId, "roleCodes", request.roleCodes());
    }

    @Transactional
    public Map<String, Object> saveRole(String roleCode, GenericSaveRequest request, boolean create, SessionUser actor) {
        ensureAdmin(actor); validateRoleCode(roleCode == null ? request.text("roleCode") : roleCode);
        String effectiveCode = roleCode == null ? request.text("roleCode") : roleCode;
        String roleName = requiredText(request, "roleName");
        String purpose = requiredText(request, "purpose");
        String grantCriteria = requiredText(request, "grantCriteria");
        String defaultDataScope = requiredText(request, "defaultDataScope");
        if (mapper.updateRole(effectiveCode, roleName, purpose, grantCriteria, defaultDataScope) == 0) {
            mapper.insertRole(effectiveCode, roleName, purpose, grantCriteria, defaultDataScope);
        }
        history("role", effectiveCode, create ? "CREATE" : "UPDATE", request, actor);
        return Map.of("roleCode", effectiveCode);
    }

    @Transactional
    public Map<String, Object> saveOrganizationRelation(String orgCode, GenericSaveRequest request, SessionUser actor) {
        ensureAdmin(actor); require(orgCode, "orgCode");
        String startDate = requiredText(request, "startDate");
        String endDate = request.text("endDate");
        if (endDate != null && !endDate.isBlank() && endDate.compareTo(startDate) < 0) throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        int updated = mapper.updateOrganizationRelation(orgCode, request.text("parentOrgCode"), startDate, endDate);
        if (updated == 0) throw new IllegalArgumentException("조직을 찾을 수 없습니다.");
        history("organization", orgCode, "UPDATE", request, actor);
        return Map.of("orgCode", orgCode, "updated", true);
    }

    @Transactional
    public Map<String, Object> saveMenuPermission(GenericSaveRequest request, SessionUser actor) {
        ensureAdmin(actor);
        String targetType = requiredText(request, "targetType");
        String targetId = requiredText(request, "targetId");
        String menuId = requiredText(request, "menuId");
        String canRead = yn(request, "canRead");
        String canCreate = yn(request, "canCreate");
        String canUpdate = yn(request, "canUpdate");
        String canDisable = yn(request, "canDisable");
        String canAccess = yn(request, "canAccess");
        if (mapper.updateMenuPermission(targetType, targetId, menuId, canRead, canCreate, canUpdate, canDisable, canAccess) == 0) {
            mapper.insertMenuPermission(targetType, targetId, menuId, canRead, canCreate, canUpdate, canDisable, canAccess);
        }
        history("menu_permission", request.text("targetType") + ":" + request.text("targetId") + ":" + request.text("menuId"), "UPDATE", request, actor);
        return Map.of("saved", true);
    }

    @Transactional
    public Map<String, Object> saveMenu(String menuId, GenericSaveRequest request, boolean create, SessionUser actor) {
        ensureAdmin(actor); String effectiveId = menuId == null ? requiredText(request, "menuId") : menuId;
        String parentMenuId = request.text("parentMenuId");
        String menuName = requiredText(request, "menuName");
        String screenId = request.text("screenId");
        String routePath = request.text("routePath");
        String iconName = request.text("iconName");
        String businessArea = requiredText(request, "businessArea");
        String description = request.text("description");
        Integer sortOrder = intValue(request, "sortOrder", 0);
        String useYn = yn(request, "useYn");
        if (mapper.updateMenu(effectiveId, parentMenuId, menuName, screenId, routePath, iconName, businessArea, description, sortOrder, useYn) == 0) {
            mapper.insertMenu(effectiveId, parentMenuId, menuName, screenId, routePath, iconName, businessArea, description, sortOrder, useYn);
        }
        history("menu", effectiveId, create ? "CREATE" : "UPDATE", request, actor);
        return Map.of("menuId", effectiveId);
    }

    @Transactional
    public Map<String, Object> changeMenuParent(String menuId, GenericSaveRequest request, SessionUser actor) {
        ensureAdmin(actor); require(menuId, "menuId");
        String parentMenuId = request.text("parentMenuId");
        if (Objects.equals(menuId, parentMenuId)) throw new IllegalArgumentException("자기 자신을 상위 메뉴로 지정할 수 없습니다.");
        if (mapper.changeMenuParent(menuId, parentMenuId) == 0) throw new IllegalArgumentException("메뉴를 찾을 수 없습니다.");
        history("menu", menuId, "UPDATE", request, actor);
        return Map.of("menuId", menuId, "parentMenuId", parentMenuId);
    }

    @Transactional
    public Map<String, Object> reorderMenus(List<GenericSaveRequest> rows, SessionUser actor) {
        ensureAdmin(actor);
        for (GenericSaveRequest row : rows) mapper.reorderMenu(requiredText(row, "menuId"), intValue(row, "sortOrder", 0));
        mapper.insertChangeHistory("menu", "bulk-reorder", "UPDATE", null, String.valueOf(rows), "동일 계층 표시순서 재정렬", actor.userId());
        return Map.of("count", rows.size());
    }

    @Transactional
    public Map<String, Object> saveCodeGroup(String groupId, GenericSaveRequest request, boolean create, SessionUser actor) {
        ensureAdmin(actor); String effectiveId = groupId == null ? requiredText(request, "groupId") : groupId;
        String groupName = requiredText(request, "groupName");
        String description = request.text("description");
        String managingDepartment = requiredText(request, "managingDepartment");
        String useYn = yn(request, "useYn");
        if (mapper.updateCodeGroup(effectiveId, groupName, description, managingDepartment, useYn) == 0) {
            mapper.insertCodeGroup(effectiveId, groupName, description, managingDepartment, useYn);
        }
        history("code_group", effectiveId, create ? "CREATE" : "UPDATE", request, actor);
        return Map.of("groupId", effectiveId);
    }

    @Transactional
    public Map<String, Object> saveDetailCode(String groupId, String codeValue, GenericSaveRequest request, boolean create, SessionUser actor) {
        ensureAdmin(actor); require(groupId, "groupId"); String effectiveCode = codeValue == null ? requiredText(request, "codeValue") : codeValue;
        String codeName = requiredText(request, "codeName");
        String parentCodeValue = request.text("parentCodeValue");
        Integer sortOrder = intValue(request, "sortOrder", 0);
        String useYn = yn(request, "useYn");
        String validFrom = requiredText(request, "validFrom");
        String validTo = request.text("validTo");
        String extraAttributes = request.text("extraAttributes") == null ? "{}" : request.text("extraAttributes");
        if (mapper.updateDetailCode(groupId, effectiveCode, codeName, parentCodeValue, sortOrder, useYn, validFrom, validTo, extraAttributes) == 0) {
            mapper.insertDetailCode(groupId, effectiveCode, codeName, parentCodeValue, sortOrder, useYn, validFrom, validTo, extraAttributes);
        }
        history("detail_code", groupId + ":" + effectiveCode, create ? "CREATE" : "UPDATE", request, actor);
        return Map.of("groupId", groupId, "codeValue", effectiveCode);
    }

    @Transactional
    public Map<String, Object> assignUserRole(GenericSaveRequest request, SessionUser actor) {
        ensureAdmin(actor); validateRoleCode(requiredText(request, "roleCode"));
        mapper.assignUserRole(requiredText(request, "userId"), request.text("roleCode"), requiredText(request, "validFrom"), request.text("validTo"), request.text("approverId"), request.text("assignmentSource") == null ? "MANUAL" : request.text("assignmentSource"));
        history("user_role_assignment", request.text("userId"), "CREATE", request, actor);
        return Map.of("assigned", true);
    }

    @Transactional
    public Map<String, Object> updateUserRole(Long assignmentId, GenericSaveRequest request, SessionUser actor) {
        ensureAdmin(actor);
        if (mapper.updateUserRole(assignmentId, requiredText(request, "validFrom"), request.text("validTo"), request.text("approverId"), request.text("assignmentSource") == null ? "MANUAL" : request.text("assignmentSource")) == 0) throw new IllegalArgumentException("역할 부여 내역을 찾을 수 없습니다.");
        history("user_role_assignment", String.valueOf(assignmentId), "UPDATE", request, actor);
        return Map.of("assignmentId", assignmentId);
    }

    @Transactional
    public Map<String, Object> revokeUserRole(Long assignmentId, SessionUser actor) {
        ensureAdmin(actor);
        if (mapper.revokeUserRole(assignmentId) == 0) throw new IllegalArgumentException("역할 부여 내역을 찾을 수 없습니다.");
        mapper.insertChangeHistory("user_role_assignment", String.valueOf(assignmentId), "REVOKE", null, "status=REVOKED", "사용자 역할 회수", actor.userId());
        return Map.of("assignmentId", assignmentId, "status", "REVOKED");
    }

    private int offset(int page, int size) { return Math.max(page, 0) * Math.max(size, 1); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private void ensureAdmin(SessionUser user) { if (user == null || !user.roleCodes().contains("R09")) throw new IllegalArgumentException("R09 시스템관리자 권한이 필요합니다."); }
    private void require(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " 값이 필요합니다."); }
    private String requiredText(GenericSaveRequest request, String key) { String value = request.text(key); require(value, key); return value; }
    private String yn(GenericSaveRequest request, String key) { String value = request.text(key); if (value == null || value.isBlank()) return "Y"; if (!value.equals("Y") && !value.equals("N")) throw new IllegalArgumentException(key + " 값은 Y 또는 N이어야 합니다."); return value; }
    private int intValue(GenericSaveRequest request, String key, int defaultValue) { String value = request.text(key); return value == null || value.isBlank() ? defaultValue : Integer.parseInt(value); }
    private void validateRoleCode(String roleCode) { if (roleCode == null || !roleCode.matches("R0[1-9]")) throw new IllegalArgumentException("역할코드는 R01~R09만 허용됩니다."); }
    private void history(String entity, String id, String action, GenericSaveRequest request, SessionUser actor) { mapper.insertChangeHistory(entity, id, action, null, String.valueOf(request.asMap()), request.text("reason") == null ? "관리자 화면 저장" : request.text("reason"), actor.userId()); }
}
