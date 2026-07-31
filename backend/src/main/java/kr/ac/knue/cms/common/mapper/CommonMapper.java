package kr.ac.knue.cms.common.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {
    Map<String, Object> findUserByLoginId(@Param("loginId") String loginId);
    Map<String, Object> findUserById(@Param("userId") String userId);
    List<String> findActiveRoleCodes(@Param("userId") String userId);
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("expiresAt") LocalDateTime expiresAt);
    void deleteSession(@Param("sessionId") String sessionId);
    Map<String, Object> findSession(@Param("sessionId") String sessionId);

    List<Map<String, Object>> listDeliveryStatus();
    List<Map<String, Object>> searchUsers(@Param("filter") String filter, @Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> searchOrganizations(@Param("filter") String filter, @Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> listRoles(@Param("filter") String filter, @Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> searchUserRoles(@Param("filter") String filter, @Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> getMenuPermissions(@Param("targetType") String targetType, @Param("targetId") String targetId);
    List<Map<String, Object>> getMenuTree();
    List<Map<String, Object>> searchMenus(@Param("filter") String filter, @Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> searchCodeGroups(@Param("filter") String filter, @Param("offset") int offset, @Param("size") int size);
    List<Map<String, Object>> listDetailCodes(@Param("groupId") String groupId);
    List<Map<String, Object>> getMenusForRoles(@Param("roles") List<String> roles);

    int updateUserUsage(@Param("userId") String userId, @Param("systemUseYn") String systemUseYn);
    void deactivateUserRoles(@Param("userId") String userId);
    void insertUserRole(@Param("userId") String userId, @Param("roleCode") String roleCode, @Param("actorId") String actorId);
    void insertChangeHistory(@Param("entityName") String entityName, @Param("entityId") String entityId, @Param("actionType") String actionType, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("reason") String reason, @Param("actorId") String actorId);

    int updateRole(@Param("roleCode") String roleCode, @Param("roleName") String roleName, @Param("purpose") String purpose, @Param("grantCriteria") String grantCriteria, @Param("defaultDataScope") String defaultDataScope);
    int insertRole(@Param("roleCode") String roleCode, @Param("roleName") String roleName, @Param("purpose") String purpose, @Param("grantCriteria") String grantCriteria, @Param("defaultDataScope") String defaultDataScope);
    int updateMenu(@Param("menuId") String menuId, @Param("parentMenuId") String parentMenuId, @Param("menuName") String menuName, @Param("screenId") String screenId, @Param("routePath") String routePath, @Param("iconName") String iconName, @Param("businessArea") String businessArea, @Param("description") String description, @Param("sortOrder") Integer sortOrder, @Param("useYn") String useYn);
    int insertMenu(@Param("menuId") String menuId, @Param("parentMenuId") String parentMenuId, @Param("menuName") String menuName, @Param("screenId") String screenId, @Param("routePath") String routePath, @Param("iconName") String iconName, @Param("businessArea") String businessArea, @Param("description") String description, @Param("sortOrder") Integer sortOrder, @Param("useYn") String useYn);
    int updateCodeGroup(@Param("groupId") String groupId, @Param("groupName") String groupName, @Param("description") String description, @Param("managingDepartment") String managingDepartment, @Param("useYn") String useYn);
    int insertCodeGroup(@Param("groupId") String groupId, @Param("groupName") String groupName, @Param("description") String description, @Param("managingDepartment") String managingDepartment, @Param("useYn") String useYn);
    int updateDetailCode(@Param("groupId") String groupId, @Param("codeValue") String codeValue, @Param("codeName") String codeName, @Param("parentCodeValue") String parentCodeValue, @Param("sortOrder") Integer sortOrder, @Param("useYn") String useYn, @Param("validFrom") String validFrom, @Param("validTo") String validTo, @Param("extraAttributes") String extraAttributes);
    int insertDetailCode(@Param("groupId") String groupId, @Param("codeValue") String codeValue, @Param("codeName") String codeName, @Param("parentCodeValue") String parentCodeValue, @Param("sortOrder") Integer sortOrder, @Param("useYn") String useYn, @Param("validFrom") String validFrom, @Param("validTo") String validTo, @Param("extraAttributes") String extraAttributes);
    int updateOrganizationRelation(@Param("orgCode") String orgCode, @Param("parentOrgCode") String parentOrgCode, @Param("startDate") String startDate, @Param("endDate") String endDate);
    int updateMenuPermission(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("menuId") String menuId, @Param("canRead") String canRead, @Param("canCreate") String canCreate, @Param("canUpdate") String canUpdate, @Param("canDisable") String canDisable, @Param("canAccess") String canAccess);
    int insertMenuPermission(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("menuId") String menuId, @Param("canRead") String canRead, @Param("canCreate") String canCreate, @Param("canUpdate") String canUpdate, @Param("canDisable") String canDisable, @Param("canAccess") String canAccess);
    int changeMenuParent(@Param("menuId") String menuId, @Param("parentMenuId") String parentMenuId);
    int reorderMenu(@Param("menuId") String menuId, @Param("sortOrder") Integer sortOrder);
    int assignUserRole(@Param("userId") String userId, @Param("roleCode") String roleCode, @Param("validFrom") String validFrom, @Param("validTo") String validTo, @Param("approverId") String approverId, @Param("assignmentSource") String assignmentSource);
    int updateUserRole(@Param("assignmentId") Long assignmentId, @Param("validFrom") String validFrom, @Param("validTo") String validTo, @Param("approverId") String approverId, @Param("assignmentSource") String assignmentSource);
    int revokeUserRole(@Param("assignmentId") Long assignmentId);
}
