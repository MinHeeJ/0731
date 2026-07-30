package kr.ac.knue.cms1344.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Requests {
  private Requests() { }
  public record LoginRequest(String loginId, String password) { }
  public record UpdateUserUsageRequest(String useYn, String changeReason) { }
  public record UserRoleInput(String roleCode, String assignmentType, String approvedBy, LocalDate validFrom, LocalDate validTo) { }
  public record ReplaceUserRolesRequest(List<UserRoleInput> roles, String changeReason) { }
  public record OrganizationRelationRequest(String parentOrgCode, LocalDate effectiveStartDate, LocalDate effectiveEndDate, String changeReason) { }
  public record RolePolicyRequest(String roleCode, String roleName, String purpose, String assignmentCriteria, String defaultDataScope, String useYn) { }
  public record UserRoleAssignmentRequest(String userId, String roleCode, String assignmentType, String approvedBy, LocalDate validFrom, LocalDate validTo) { }
  public record RevokeRequest(String changeReason) { }
  public record MenuPermissionMatrixRequest(String targetType, String targetId, List<MenuPermissionInput> permissions) { }
  public record MenuPermissionInput(String menuId, String accessAllowedYn, List<String> functionPermissions) { }
  public record MenuMoveRequest(String parentMenuId, Integer displayOrder, String changeReason) { }
  public record MenuReorderRequest(String parentMenuId, List<String> orderedMenuIds, String changeReason) { }
  public record MenuInfoRequest(String menuId, String menuName, String screenId, String url, String icon, String businessDomain, String description, String useYn, String changeReason) { }
  public record CodeGroupRequest(String groupId, String groupName, String description, String managingDepartment, String useYn, String changeReason) { }
  public record CodeDetailRequest(String groupId, String codeValue, String codeName, String parentCodeValue, Integer displayOrder, Map<String,Object> extraAttributes, String useYn, LocalDate validFrom, LocalDate validTo, String changeReason) { }
}
