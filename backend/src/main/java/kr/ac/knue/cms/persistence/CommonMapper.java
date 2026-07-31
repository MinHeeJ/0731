package kr.ac.knue.cms.persistence;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {
    @Select("""
        select u.user_id as "userId", u.password_hash as "passwordHash", u.status as "status", p.name as "name"
        from app_user u left join korus_personnel_snapshot p on p.person_id = u.korus_person_id
        where u.user_id = #{userId} and u.use_yn = 'Y' and u.status = 'ACTIVE'
        """)
    Map<String, Object> findLoginUser(@Param("userId") String userId);

    @Insert("insert into \"session\" (session_id, user_id, expires_at, created_at, updated_at) values (#{sessionId}, #{userId}, #{expiresAt}, now(), now())")
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("expiresAt") java.time.LocalDateTime expiresAt);

    @Delete("delete from \"session\" where session_id = #{sessionId}")
    int deleteSession(@Param("sessionId") String sessionId);

    @Select("""
        select u.user_id as "userId", p.name as "name"
        from "session" s join app_user u on u.user_id = s.user_id left join korus_personnel_snapshot p on p.person_id = u.korus_person_id
        where s.session_id = #{sessionId} and s.expires_at > now() and u.status = 'ACTIVE'
        """)
    Map<String, Object> findUserBySession(@Param("sessionId") String sessionId);

    @Select("select role_code from user_role where user_id = #{userId} and status = 'ACTIVE' and (valid_to is null or valid_to >= current_date) order by role_code")
    List<String> findRoleCodesByUserId(@Param("userId") String userId);

    @SelectProvider(type = SqlProvider.class, method = "listUsers")
    List<Map<String, Object>> listUsers(@Param("q") String q, @Param("roleCode") String roleCode, @Param("useYn") String useYn);

    @SelectProvider(type = SqlProvider.class, method = "listPersonnelSnapshots")
    List<Map<String, Object>> listPersonnelSnapshots(@Param("q") String q);

    @Update("update app_user set use_yn = #{useYn}, status = #{status}, updated_at = now() where user_id = #{userId}")
    int updateUserAccount(@Param("userId") String userId, @Param("useYn") String useYn, @Param("status") String status);

    @Update("update user_role set status = 'REVOKED', valid_to = current_date, updated_at = now() where user_id = #{userId} and status = 'ACTIVE'")
    int revokeUserRoles(@Param("userId") String userId);

    @Insert("insert into user_role (user_id, role_code, assignment_type, valid_from, approver_id, status, created_at, updated_at) values (#{userId}, #{roleCode}, 'MANUAL', current_date, #{actorId}, 'ACTIVE', now(), now())")
    void insertUserRole(@Param("userId") String userId, @Param("roleCode") String roleCode, @Param("actorId") String actorId);

    @SelectProvider(type = SqlProvider.class, method = "listOrgs")
    List<Map<String, Object>> listOrgs(@Param("q") String q);

    @SelectProvider(type = SqlProvider.class, method = "listOrgTree")
    List<Map<String, Object>> listOrgTree(@Param("q") String q);

    @Update("update org_relation set parent_org_code = #{parentOrgCode}, valid_from = cast(#{validFrom} as date), valid_to = cast(#{validTo} as date), change_reason = #{reason}, updated_at = now() where relation_id = #{relationId}")
    int updateOrgRelation(@Param("relationId") Long relationId, @Param("parentOrgCode") String parentOrgCode, @Param("validFrom") String validFrom, @Param("validTo") String validTo, @Param("reason") String reason);

    @SelectProvider(type = SqlProvider.class, method = "listRoles")
    List<Map<String, Object>> listRoles(@Param("q") String q, @Param("useYn") String useYn);

    @Update("update role set role_name = #{roleName}, purpose = #{purpose}, grant_criteria = #{grantCriteria}, default_data_scope = #{defaultDataScope}, use_yn = #{useYn}, updated_at = now() where role_code = #{roleCode}")
    int updateRole(@Param("roleCode") String roleCode, @Param("roleName") String roleName, @Param("purpose") String purpose, @Param("grantCriteria") String grantCriteria, @Param("defaultDataScope") String defaultDataScope, @Param("useYn") String useYn);

    @SelectProvider(type = SqlProvider.class, method = "listUserRoles")
    List<Map<String, Object>> listUserRoles(@Param("q") String q, @Param("roleCode") String roleCode, @Param("status") String status);

    @Insert("insert into user_role (user_id, role_code, assignment_type, valid_from, valid_to, approver_id, status, created_at, updated_at) values (#{userId}, #{roleCode}, #{assignmentType}, cast(#{validFrom} as date), cast(#{validTo} as date), #{approverId}, 'ACTIVE', now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "assignmentId", keyColumn = "assignment_id")
    int grantUserRole(Map<String, Object> row);

    @Update("update user_role set valid_from = cast(#{validFrom} as date), valid_to = cast(#{validTo} as date), approver_id = #{approverId}, status = #{status}, updated_at = now() where assignment_id = #{assignmentId}")
    int updateUserRole(Map<String, Object> row);

    @Update("update user_role set status = 'REVOKED', valid_to = current_date, updated_at = now() where assignment_id = #{assignmentId} and status <> 'REVOKED'")
    int revokeUserRole(@Param("assignmentId") Long assignmentId);

    @SelectProvider(type = SqlProvider.class, method = "listMenuPermissions")
    List<Map<String, Object>> listMenuPermissions(@Param("targetType") String targetType, @Param("targetId") String targetId);

    @Delete("delete from menu_permission where target_type = #{targetType} and target_id = #{targetId}")
    int deleteMenuPermissions(@Param("targetType") String targetType, @Param("targetId") String targetId);

    @Insert("insert into menu_permission (target_type, target_id, menu_id, access_allowed, created_at, updated_at) values (#{targetType}, #{targetId}, #{menuId}, #{accessAllowed}, now(), now())")
    void insertMenuPermission(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("menuId") Long menuId, @Param("accessAllowed") Boolean accessAllowed);

    @SelectProvider(type = SqlProvider.class, method = "listMenus")
    List<Map<String, Object>> listMenus(@Param("q") String q);

    @Select("""
        select menu_id as "menuId", parent_menu_id as "parentMenuId", menu_type as "menuType", menu_name as "menuName", screen_id as "screenId", url, icon, business_category as "businessCategory", description, display_order as "displayOrder", use_yn as "useYn"
        from menu where use_yn = 'Y' order by coalesce(parent_menu_id, 0), display_order, menu_id
        """)
    List<Map<String, Object>> getMenuTree();

    @Select("""
        select distinct m.menu_id as "menuId", m.parent_menu_id as "parentMenuId", m.menu_type as "menuType", m.menu_name as "menuName", m.screen_id as "screenId", m.url, m.display_order as "displayOrder"
        from menu m join menu_permission p on p.menu_id = m.menu_id and p.target_type = 'ROLE' and p.access_allowed = true
        join user_role ur on ur.role_code = p.target_id and ur.status = 'ACTIVE'
        where ur.user_id = #{userId} and m.use_yn = 'Y' order by m.display_order, m.menu_id
        """)
    List<Map<String, Object>> getMyMenus(@Param("userId") String userId);

    @Update("update menu set parent_menu_id = #{parentMenuId}, display_order = #{displayOrder}, updated_at = now() where menu_id = #{menuId}")
    int updateMenuStructure(@Param("menuId") Long menuId, @Param("parentMenuId") Long parentMenuId, @Param("displayOrder") Integer displayOrder);

    @Update("update menu set display_order = #{displayOrder}, updated_at = now() where menu_id = #{menuId}")
    int updateMenuOrder(@Param("menuId") Long menuId, @Param("displayOrder") Integer displayOrder);

    @Insert("insert into menu (parent_menu_id, menu_type, menu_name, screen_id, url, icon, business_category, description, display_order, use_yn, created_at, updated_at) values (#{parentMenuId}, #{menuType}, #{menuName}, #{screenId}, #{url}, #{icon}, #{businessCategory}, #{description}, #{displayOrder}, #{useYn}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "menuId", keyColumn = "menu_id")
    int createMenu(Map<String, Object> row);

    @Update("update menu set parent_menu_id = #{parentMenuId}, menu_type = #{menuType}, menu_name = #{menuName}, screen_id = #{screenId}, url = #{url}, icon = #{icon}, business_category = #{businessCategory}, description = #{description}, display_order = #{displayOrder}, use_yn = #{useYn}, updated_at = now() where menu_id = #{menuId}")
    int updateMenu(Map<String, Object> row);

    @SelectProvider(type = SqlProvider.class, method = "listCodeGroups")
    List<Map<String, Object>> listCodeGroups(@Param("q") String q, @Param("useYn") String useYn);

    @Insert("insert into code_group (group_id, group_name, description, managing_department, use_yn, created_at, updated_at) values (#{groupId}, #{groupName}, #{description}, #{managingDepartment}, #{useYn}, now(), now())")
    int createCodeGroup(Map<String, Object> row);

    @Update("update code_group set group_name = #{groupName}, description = #{description}, managing_department = #{managingDepartment}, use_yn = #{useYn}, updated_at = now() where group_id = #{groupId}")
    int updateCodeGroup(Map<String, Object> row);

    @SelectProvider(type = SqlProvider.class, method = "listCodeDetails")
    List<Map<String, Object>> listCodeDetails(@Param("groupId") String groupId, @Param("q") String q);

    @Insert("insert into code_detail (group_id, code_value, code_name, parent_code_value, sort_order, extra_attributes, valid_from, valid_to, use_yn, created_at, updated_at) values (#{groupId}, #{codeValue}, #{codeName}, #{parentCodeValue}, #{sortOrder}, cast(#{extraAttributes} as jsonb), cast(#{validFrom} as date), cast(#{validTo} as date), #{useYn}, now(), now())")
    int createCodeDetail(Map<String, Object> row);

    @Update("update code_detail set code_name = #{codeName}, parent_code_value = #{parentCodeValue}, sort_order = #{sortOrder}, extra_attributes = cast(#{extraAttributes} as jsonb), valid_from = cast(#{validFrom} as date), valid_to = cast(#{validTo} as date), use_yn = #{useYn}, updated_at = now() where group_id = #{groupId} and code_value = #{codeValue}")
    int updateCodeDetail(Map<String, Object> row);

    @Insert("insert into change_history (entity_name, entity_id, before_value, after_value, actor_id, reason, created_at) values (#{entityName}, #{entityId}, cast(#{beforeValue} as jsonb), cast(#{afterValue} as jsonb), #{actorId}, #{reason}, now())")
    void insertHistory(@Param("entityName") String entityName, @Param("entityId") String entityId, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("actorId") String actorId, @Param("reason") String reason);
}
