package kr.ac.knue.cms1344.persistence;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper {
  @Select("select count(*) from internal_user u join user_role ur on ur.user_id=u.user_id and ur.status='ACTIVE' where u.login_id=#{loginId} and u.password_hash=#{passwordHash} and u.system_use_yn='Y' and ur.role_code='R09'")
  int canLogin(String loginId, String passwordHash);
  @Select("select u.user_id, u.login_id, u.system_use_yn, k.employee_no, k.person_name, k.org_code, array_agg(ur.role_code order by ur.role_code) as role_codes from internal_user u join korus_person_snapshot k on k.employee_no=u.employee_no left join user_role ur on ur.user_id=u.user_id and ur.status='ACTIVE' where u.login_id=#{loginId} group by u.user_id,k.employee_no,k.person_name,k.org_code")
  Map<String,Object> userByLogin(String loginId);
  @Select("select u.user_id, u.login_id, u.system_use_yn, k.employee_no, k.person_name, k.org_code, array_agg(ur.role_code order by ur.role_code) as role_codes from internal_user u join korus_person_snapshot k on k.employee_no=u.employee_no left join user_role ur on ur.user_id=u.user_id and ur.status='ACTIVE' where u.user_id=#{userId} group by u.user_id,k.employee_no,k.person_name,k.org_code")
  Map<String,Object> userById(String userId);
  @Select("select s.session_id, s.user_id, s.status from session s where s.session_id=#{sessionId} and s.status='AUTHENTICATED' and s.expires_at > now()")
  Map<String,Object> activeSession(String sessionId);
  @Insert("insert into session(session_id,user_id,issued_at,expires_at,status) values(#{sessionId},#{userId},now(),now()+ interval '8 hours','AUTHENTICATED')")
  void insertSession(String sessionId, String userId);
  @Update("update session set status='EXPIRED', invalidated_at=coalesce(invalidated_at,now()) where session_id=#{sessionId} and status='AUTHENTICATED'")
  int expireSession(String sessionId);
  @Update("update internal_user set last_login_at=now(), updated_at=now(), updated_by=#{userId} where user_id=#{userId}")
  void touchLogin(String userId);
  @Select("select count(*) from user_role where user_id=#{userId} and role_code='R09' and status='ACTIVE'")
  int isAdmin(String userId);

  @Select({"<script>","select u.user_id, k.employee_no, k.person_name, k.org_code as department_code, o.org_name as department_name, k.rank_name, k.employment_status, k.position_name, k.retired_at, k.last_synced_at, coalesce(u.system_use_yn,'N') as use_yn, coalesce(string_agg(distinct ur.role_code, ','),'') as role_codes from korus_person_snapshot k left join internal_user u on u.employee_no=k.employee_no left join organization o on o.org_code=k.org_code left join user_role ur on ur.user_id=u.user_id and ur.status='ACTIVE' where 1=1 ","<if test='employeeNo != null and employeeNo != \"\"'> and k.employee_no = #{employeeNo}</if>","<if test='name != null and name != \"\"'> and k.person_name like concat('%',#{name},'%')</if>","<if test='departmentCode != null and departmentCode != \"\"'> and k.org_code = #{departmentCode}</if>","<if test='rankName != null and rankName != \"\"'> and k.rank_name = #{rankName}</if>","<if test='employmentStatus != null and employmentStatus != \"\"'> and k.employment_status = #{employmentStatus}</if>","<if test='useYn != null and useYn != \"\"'> and u.system_use_yn = #{useYn}</if>","<if test='roleCode != null and roleCode != \"\"'> and exists(select 1 from user_role r where r.user_id=u.user_id and r.role_code=#{roleCode} and r.status='ACTIVE')</if>","group by u.user_id,k.employee_no,k.person_name,k.org_code,o.org_name,k.rank_name,k.employment_status,k.position_name,k.retired_at,k.last_synced_at,u.system_use_yn order by k.employee_no limit #{size} offset #{offset}","</script>"})
  List<Map<String,Object>> listUsers(Map<String,Object> params);
  @Select("select u.user_id, k.employee_no, k.person_name, k.org_code as department_code, o.org_name as department_name, k.rank_name, k.employment_status, k.position_name, k.retired_at, k.last_synced_at, u.system_use_yn as use_yn, coalesce(json_agg(json_build_object('assignmentId',ur.user_role_id,'roleCode',ur.role_code,'assignmentType',ur.assignment_type,'validFrom',ur.valid_from,'validTo',ur.valid_to,'status',ur.status)) filter (where ur.user_role_id is not null),'[]') as roles from internal_user u join korus_person_snapshot k on k.employee_no=u.employee_no left join organization o on o.org_code=k.org_code left join user_role ur on ur.user_id=u.user_id where u.user_id=#{userId} group by u.user_id,k.employee_no,k.person_name,k.org_code,o.org_name,k.rank_name,k.employment_status,k.position_name,k.retired_at,k.last_synced_at,u.system_use_yn")
  Map<String,Object> getUser(String userId);
  @Update("update internal_user set system_use_yn=#{useYn}, status=case when #{useYn}='Y' then 'ACTIVE' else 'INACTIVE' end, updated_at=now(), updated_by=#{actor} where user_id=#{userId}")
  int updateUserUsage(String userId, String useYn, String actor);
  @Update("update user_role set status='REVOKED', revoked_at=now(), revoked_by=#{actor}, change_reason=#{reason}, updated_at=now(), updated_by=#{actor} where user_id=#{userId} and status='ACTIVE'")
  void revokeActiveRolesForUser(String userId, String actor, String reason);
  @Insert("insert into user_role(user_role_id,user_id,role_code,assignment_type,approved_by,valid_from,valid_to,status,change_reason,created_by,updated_by) values(#{id},#{userId},#{roleCode},#{assignmentType},#{approvedBy},#{validFrom},#{validTo},'ACTIVE',#{reason},#{actor},#{actor})")
  void insertUserRole(String id,String userId,String roleCode,String assignmentType,String approvedBy,LocalDate validFrom,LocalDate validTo,String reason,String actor);

  @Select({"<script>","select * from organization where 1=1","<if test='orgCode != null and orgCode != \"\"'> and org_code=#{orgCode}</if>","<if test='orgName != null and orgName != \"\"'> and org_name like concat('%',#{orgName},'%')</if>","<if test='orgType != null and orgType != \"\"'> and org_type=#{orgType}</if>","<if test='useYn != null and useYn != \"\"'> and use_yn=#{useYn}</if>"," order by org_code limit #{size} offset #{offset}","</script>"})
  List<Map<String,Object>> listOrganizations(Map<String,Object> params);
  @Select("select o.*, r.parent_org_code, r.effective_start_date, r.effective_end_date from organization o left join organization_relation_history r on r.org_code=o.org_code and r.effective_end_date is null order by coalesce(r.parent_org_code,''), o.display_order, o.org_code")
  List<Map<String,Object>> orgTreeRows();
  @Select("select count(*) from organization where org_code=#{orgCode}") int existsOrganization(String orgCode);
  @Insert("insert into organization_relation_history(relation_id,org_code,parent_org_code,effective_start_date,effective_end_date,change_reason,created_by,updated_by) values(#{id},#{orgCode},#{parentOrgCode},#{start},#{end},#{reason},#{actor},#{actor})")
  void insertOrgRelation(String id,String orgCode,String parentOrgCode,LocalDate start,LocalDate end,String reason,String actor);

  @Select({"<script>","select * from role where 1=1","<if test='roleCode != null and roleCode != \"\"'> and role_code=#{roleCode}</if>","<if test='roleName != null and roleName != \"\"'> and role_name like concat('%',#{roleName},'%')</if>","<if test='useYn != null and useYn != \"\"'> and use_yn=#{useYn}</if>"," order by role_code limit #{size} offset #{offset}","</script>"})
  List<Map<String,Object>> listRoles(Map<String,Object> params);
  @Update("update role set role_name=coalesce(#{roleName},role_name), purpose=#{purpose}, assignment_criteria=#{criteria}, default_data_scope=#{scope}, use_yn=coalesce(#{useYn},use_yn), updated_at=now(), updated_by=#{actor} where role_code=#{roleCode}")
  int updateRole(String roleCode,String roleName,String purpose,String criteria,String scope,String useYn,String actor);

  @Select({"<script>","select ur.*, u.login_id, k.person_name from user_role ur join internal_user u on u.user_id=ur.user_id join korus_person_snapshot k on k.employee_no=u.employee_no where 1=1","<if test='userId != null and userId != \"\"'> and ur.user_id=#{userId}</if>","<if test='roleCode != null and roleCode != \"\"'> and ur.role_code=#{roleCode}</if>","<if test='assignmentType != null and assignmentType != \"\"'> and ur.assignment_type=#{assignmentType}</if>","<if test='validOn != null and validOn != \"\"'> and ur.valid_from &lt;= #{validOn}::date and (ur.valid_to is null or ur.valid_to &gt;= #{validOn}::date)</if>"," order by ur.created_at desc limit #{size} offset #{offset}","</script>"})
  List<Map<String,Object>> listUserRoles(Map<String,Object> params);
  @Select("select count(*) from user_role where user_id=#{userId} and role_code=#{roleCode} and valid_from=#{validFrom} and coalesce(valid_to, date '9999-12-31')=coalesce(#{validTo}, date '9999-12-31') and status='ACTIVE'")
  int duplicateUserRole(String userId,String roleCode,LocalDate validFrom,LocalDate validTo);
  @Update("update user_role set status='REVOKED', revoked_at=now(), revoked_by=#{actor}, change_reason=#{reason}, updated_at=now(), updated_by=#{actor} where user_role_id=#{assignmentId} and status='ACTIVE'")
  int revokeAssignment(String assignmentId,String actor,String reason);

  @Select("select m.menu_id, m.parent_menu_id, m.menu_level, m.menu_name, m.screen_id, m.url, m.display_order, coalesce(mp.access_allowed_yn,'N') as access_allowed_yn, coalesce(mp.function_permissions,'[]'::jsonb) as function_permissions from menu m left join menu_permission mp on mp.menu_id=m.menu_id and mp.target_type=#{targetType} and mp.target_id=#{targetId} order by m.menu_level,m.display_order")
  List<Map<String,Object>> permissionMatrix(String targetType,String targetId);
  @Select("select count(*) from menu where menu_id=#{menuId}") int existsMenu(String menuId);
  @Insert("insert into menu_permission(permission_id,target_type,target_id,menu_id,access_allowed_yn,function_permissions,created_by,updated_by) values(#{id},#{targetType},#{targetId},#{menuId},#{allowed},#{functions}::jsonb,#{actor},#{actor}) on conflict(target_type,target_id,menu_id) do update set access_allowed_yn=excluded.access_allowed_yn,function_permissions=excluded.function_permissions,updated_at=now(),updated_by=excluded.updated_by")
  void upsertPermission(String id,String targetType,String targetId,String menuId,String allowed,String functions,String actor);

  @Select("select * from menu order by menu_level, parent_menu_id nulls first, display_order") List<Map<String,Object>> menuTreeRows();
  @Select("select * from menu where menu_id=#{menuId}") Map<String,Object> menu(String menuId);
  @Update("update menu set parent_menu_id=#{parentMenuId}, menu_level=#{level}, display_order=#{displayOrder}, updated_at=now(), updated_by=#{actor} where menu_id=#{menuId}")
  int moveMenu(String menuId,String parentMenuId,int level,int displayOrder,String actor);
  @Update("update menu set display_order=#{displayOrder}, updated_at=now(), updated_by=#{actor} where menu_id=#{menuId}") int updateMenuOrder(String menuId,int displayOrder,String actor);
  @Select("select count(distinct parent_menu_id) from menu where menu_id = any(#{ids}::varchar[])") int distinctParentCount(String ids);

  @Select({"<script>","select * from menu where 1=1","<if test='menuName != null and menuName != \"\"'> and menu_name like concat('%',#{menuName},'%')</if>","<if test='screenId != null and screenId != \"\"'> and screen_id=#{screenId}</if>","<if test='useYn != null and useYn != \"\"'> and use_yn=#{useYn}</if>"," order by menu_level,display_order limit #{size} offset #{offset}","</script>"})
  List<Map<String,Object>> listMenuInfo(Map<String,Object> params);
  @Select("select count(*) from menu where screen_id=#{screenId} and use_yn='Y' and menu_id &lt;&gt; #{menuId}") int duplicateActiveScreen(String screenId,String menuId);
  @Insert("insert into menu(menu_id,parent_menu_id,menu_level,menu_name,screen_id,url,icon,business_domain,description,display_order,use_yn,created_by,updated_by) values(#{menuId},null,3,#{menuName},#{screenId},#{url},#{icon},#{domain},#{description},99,#{useYn},#{actor},#{actor}) on conflict(menu_id) do update set menu_name=excluded.menu_name,screen_id=excluded.screen_id,url=excluded.url,icon=excluded.icon,business_domain=excluded.business_domain,description=excluded.description,use_yn=excluded.use_yn,updated_at=now(),updated_by=excluded.updated_by")
  void upsertMenuInfo(String menuId,String menuName,String screenId,String url,String icon,String domain,String description,String useYn,String actor);

  @Select({"<script>","select * from code_group where 1=1","<if test='groupId != null and groupId != \"\"'> and group_id=#{groupId}</if>","<if test='groupName != null and groupName != \"\"'> and group_name like concat('%',#{groupName},'%')</if>","<if test='managingDepartment != null and managingDepartment != \"\"'> and managing_department=#{managingDepartment}</if>","<if test='useYn != null and useYn != \"\"'> and use_yn=#{useYn}</if>"," order by group_id limit #{size} offset #{offset}","</script>"})
  List<Map<String,Object>> listCodeGroups(Map<String,Object> params);
  @Select("select count(*) from code_group where group_id=#{groupId}") int existsCodeGroup(String groupId);
  @Insert("insert into code_group(group_id,group_name,description,managing_department,use_yn,created_by,updated_by) values(#{groupId},#{groupName},#{description},#{dept},#{useYn},#{actor},#{actor}) on conflict(group_id) do update set group_name=excluded.group_name,description=excluded.description,managing_department=excluded.managing_department,use_yn=excluded.use_yn,updated_at=now(),updated_by=excluded.updated_by")
  void upsertCodeGroup(String groupId,String groupName,String description,String dept,String useYn,String actor);

  @Select({"<script>","select * from code_detail where group_id=#{groupId}","<if test='codeValue != null and codeValue != \"\"'> and code_value=#{codeValue}</if>","<if test='codeName != null and codeName != \"\"'> and code_name like concat('%',#{codeName},'%')</if>","<if test='useYn != null and useYn != \"\"'> and use_yn=#{useYn}</if>"," order by display_order,code_value limit #{size} offset #{offset}","</script>"})
  List<Map<String,Object>> listCodeDetails(Map<String,Object> params);
  @Select("select count(*) from code_detail where group_id=#{groupId} and code_value=#{codeValue}") int existsCodeDetail(String groupId,String codeValue);
  @Insert("insert into code_detail(group_id,code_value,code_name,parent_code_value,display_order,extra_attributes,use_yn,valid_from,valid_to,created_by,updated_by) values(#{groupId},#{codeValue},#{codeName},#{parent},#{displayOrder},#{extra}::jsonb,#{useYn},#{validFrom},#{validTo},#{actor},#{actor}) on conflict(group_id,code_value) do update set code_name=excluded.code_name,parent_code_value=excluded.parent_code_value,display_order=excluded.display_order,extra_attributes=excluded.extra_attributes,use_yn=excluded.use_yn,valid_from=excluded.valid_from,valid_to=excluded.valid_to,updated_at=now(),updated_by=excluded.updated_by")
  void upsertCodeDetail(String groupId,String codeValue,String codeName,String parent,Integer displayOrder,String extra,String useYn,LocalDate validFrom,LocalDate validTo,String actor);

  @Insert("insert into change_history(history_id,entity_name,entity_id,operation_type,before_value,after_value,changed_by,change_reason) values(#{id},#{entity},#{entityId},#{op},#{before}::jsonb,#{after}::jsonb,#{actor},#{reason})")
  void history(String id,String entity,String entityId,String op,String before,String after,String actor,String reason);
}
