package kr.ac.knue.cms.common;

import org.apache.ibatis.annotations.*;
import java.util.*;

@Mapper
public interface AdminMapper {
  @Select("""
      select a.user_id as "userId", coalesce(k.name,a.user_id) as name, a.status, a.use_yn as "useYn",
             coalesce(string_agg(ur.role_code, ',' order by ur.role_code) filter (where ur.status='ACTIVE'), '') as "roles"
      from app_user a left join korus_personnel_snapshot k on k.person_id=a.korus_person_id
      left join user_role ur on ur.user_id=a.user_id
      where a.user_id=#{userId}
      group by a.user_id,k.name,a.status,a.use_yn
      """)
  Map<String,Object> currentUser(@Param("userId") String userId);

  @Select("""
      select distinct m.menu_id as "menuId", m.parent_menu_id as "parentMenuId", m.menu_type as "menuType", m.menu_name as "menuName", m.url, m.display_order as "displayOrder"
      from menu m join menu_permission mp on mp.menu_id=m.menu_id and mp.access_allowed=true
      join user_role ur on ur.role_code=mp.target_id and mp.target_type='ROLE' and ur.status='ACTIVE'
      where ur.user_id=#{userId} and m.use_yn='Y'
      order by m.display_order, m.menu_id
      """)
  List<Map<String,Object>> myMenus(@Param("userId") String userId);

  @SelectProvider(type=AdminSqlProvider.class, method="users")
  List<Map<String,Object>> listUsers(@Param("q") String q, @Param("filter") String filter, @Param("size") int size, @Param("offset") int offset);

  @SelectProvider(type=AdminSqlProvider.class, method="simpleList")
  List<Map<String,Object>> simpleList(@Param("table") String table, @Param("orderBy") String orderBy, @Param("q") String q, @Param("size") int size, @Param("offset") int offset);

  @Select("""
      select o.org_code as "orgCode", o.org_name as "orgName", o.org_type as "orgType", o.use_yn as "useYn",
             r.relation_id as "relationId", r.parent_org_code as "parentOrgCode", r.valid_from as "validFrom", r.valid_to as "validTo"
      from org o left join org_relation r on r.org_code=o.org_code
      order by o.org_code
      """)
  List<Map<String,Object>> orgTree();

  @Select("select * from menu order by display_order, menu_id")
  List<Map<String,Object>> menuTree();

  @SelectProvider(type=AdminSqlProvider.class, method="menuPermissions")
  List<Map<String,Object>> listMenuPermissions(@Param("targetType") String targetType, @Param("targetId") String targetId);

  @Update("update app_user set use_yn=#{useYn}, status=case when #{useYn}='Y' then 'ACTIVE' else 'INACTIVE' end, updated_at=now() where user_id=#{userId}")
  int updateUserAccount(@Param("userId") String userId, @Param("useYn") String useYn);

  @Delete("delete from user_role where user_id=#{userId} and assignment_type='MANUAL'")
  void deleteManualRoles(@Param("userId") String userId);

  @Insert("insert into user_role(user_id, role_code, assignment_type, valid_from, approver_id, status, created_at, updated_at) values(#{userId}, #{roleCode}, 'MANUAL', current_date, #{actorId}, 'ACTIVE', now(), now())")
  void insertManualRole(@Param("userId") String userId, @Param("roleCode") String roleCode, @Param("actorId") String actorId);

  @Update("update org_relation set parent_org_code=#{parentOrgCode}, valid_from=cast(#{validFrom} as date), valid_to=cast(#{validTo} as date), change_reason=#{reason}, updated_at=now() where relation_id=#{relationId}")
  int updateOrgRelation(@Param("relationId") long relationId, @Param("parentOrgCode") String parentOrgCode, @Param("validFrom") String validFrom, @Param("validTo") String validTo, @Param("reason") String reason);

  @Update("update role set role_name=#{roleName}, purpose=#{purpose}, grant_criteria=#{grantCriteria}, default_data_scope=#{defaultDataScope}, use_yn=#{useYn}, updated_at=now() where role_code=#{roleCode}")
  int updateRole(@Param("roleCode") String roleCode, @Param("roleName") String roleName, @Param("purpose") String purpose, @Param("grantCriteria") String grantCriteria, @Param("defaultDataScope") String defaultDataScope, @Param("useYn") String useYn);

  @Insert("insert into user_role(user_id, role_code, assignment_type, valid_from, valid_to, approver_id, status, created_at, updated_at) values(#{userId},#{roleCode},#{assignmentType},cast(#{validFrom} as date),cast(#{validTo} as date),#{approverId},'ACTIVE',now(),now())")
  @Options(useGeneratedKeys=true, keyProperty="assignmentId", keyColumn="assignment_id")
  int grantUserRole(Map<String,Object> row);

  @Update("update user_role set valid_from=cast(#{validFrom} as date), valid_to=cast(#{validTo} as date), approver_id=#{approverId}, status=#{status}, updated_at=now() where assignment_id=#{assignmentId}")
  int updateUserRole(Map<String,Object> row);

  @Update("update user_role set status='REVOKED', valid_to=current_date, updated_at=now() where assignment_id=#{assignmentId} and status <> 'REVOKED'")
  int revokeUserRole(@Param("assignmentId") long assignmentId);

  @Delete("delete from menu_permission where target_type=#{targetType} and target_id=#{targetId}")
  void deletePermissions(@Param("targetType") String targetType, @Param("targetId") String targetId);

  @Insert("insert into menu_permission(target_type,target_id,menu_id,access_allowed,created_at,updated_at) values(#{targetType},#{targetId},#{menuId},#{accessAllowed},now(),now())")
  void insertPermission(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("menuId") long menuId, @Param("accessAllowed") boolean accessAllowed);

  @Update("update menu set parent_menu_id=#{parentMenuId}, display_order=#{displayOrder}, updated_at=now() where menu_id=#{menuId}")
  int updateMenuStructure(@Param("menuId") long menuId, @Param("parentMenuId") Long parentMenuId, @Param("displayOrder") int displayOrder);

  @Update("update menu set display_order=#{displayOrder}, updated_at=now() where menu_id=#{menuId}")
  int updateMenuOrder(@Param("menuId") long menuId, @Param("displayOrder") int displayOrder);

  @Insert("insert into menu(parent_menu_id,menu_type,menu_name,screen_id,url,icon,business_category,description,display_order,use_yn,created_at,updated_at) values(#{parentMenuId},#{menuType},#{menuName},#{screenId},#{url},#{icon},#{businessCategory},#{description},#{displayOrder},#{useYn},now(),now())")
  @Options(useGeneratedKeys=true, keyProperty="menuId", keyColumn="menu_id")
  int createMenu(Map<String,Object> row);

  @Update("update menu set menu_name=#{menuName}, screen_id=#{screenId}, url=#{url}, icon=#{icon}, business_category=#{businessCategory}, description=#{description}, use_yn=#{useYn}, updated_at=now() where menu_id=#{menuId}")
  int updateMenu(Map<String,Object> row);

  @Insert("insert into code_group(group_id,group_name,description,managing_department,use_yn,created_at,updated_at) values(#{groupId},#{groupName},#{description},#{managingDepartment},#{useYn},now(),now())")
  int createCodeGroup(Map<String,Object> row);

  @Update("update code_group set group_name=#{groupName}, description=#{description}, managing_department=#{managingDepartment}, use_yn=#{useYn}, updated_at=now() where group_id=#{groupId}")
  int updateCodeGroup(Map<String,Object> row);

  @SelectProvider(type=AdminSqlProvider.class, method="codeDetails")
  List<Map<String,Object>> codeDetails(@Param("groupId") String groupId, @Param("q") String q, @Param("size") int size, @Param("offset") int offset);

  @Insert("insert into code_detail(group_id,code_value,code_name,parent_code_value,sort_order,extra_attributes,valid_from,valid_to,use_yn,created_at,updated_at) values(#{groupId},#{codeValue},#{codeName},#{parentCodeValue},#{sortOrder},cast(coalesce(#{extraAttributes}, '{}') as jsonb),cast(#{validFrom} as date),cast(#{validTo} as date),#{useYn},now(),now())")
  int createCodeDetail(Map<String,Object> row);

  @Update("update code_detail set code_name=#{codeName}, parent_code_value=#{parentCodeValue}, sort_order=#{sortOrder}, extra_attributes=cast(coalesce(#{extraAttributes}, '{}') as jsonb), valid_from=cast(#{validFrom} as date), valid_to=cast(#{validTo} as date), use_yn=#{useYn}, updated_at=now() where group_id=#{groupId} and code_value=#{codeValue}")
  int updateCodeDetail(Map<String,Object> row);
}
