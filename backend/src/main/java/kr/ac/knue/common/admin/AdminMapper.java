package kr.ac.knue.common.admin;

import org.apache.ibatis.annotations.*;
import java.time.OffsetDateTime;
import java.util.*;

@Mapper
public interface AdminMapper {
  @Select("select user_id as \"userId\", password_hash as \"passwordHash\", system_use_yn as \"systemUseYn\", status from user_account where user_id=#{userId}") Map<String,Object> findAccount(String userId);
  @Select("select role_code from user_role_assignment where user_id=#{userId} and status='ACTIVE' and valid_from<=current_date and (valid_to is null or valid_to>=current_date) order by role_code") List<String> findActiveRoleCodes(String userId);
  @Insert("insert into session(session_id,user_id,expires_at,status) values(#{sid},#{userId},#{expiresAt},'ACTIVE')") void createSession(String sid, String userId, OffsetDateTime expiresAt);
  @Select("select session_id as \"sessionId\", user_id as \"userId\" from session where session_id=#{sid} and status='ACTIVE' and expires_at>now()") Map<String,Object> findActiveSession(String sid);
  @Update("update session set status=#{status}, updated_at=now() where session_id=#{sid}") void updateSessionStatus(String sid, String status);
  @Insert("insert into change_history(entity_name,entity_key,before_value,after_value,changed_by,change_reason) values(#{entity},#{key},cast(#{beforeJson} as jsonb),cast(#{afterJson} as jsonb),#{changedBy},#{reason})") void insertHistoryJson(String entity, String key, String beforeJson, String afterJson, String changedBy, String reason);
  default void insertHistory(String entity, String key, Object beforeValue, Object afterValue, String changedBy, String reason) { insertHistoryJson(entity, key, Jsons.toJson(beforeValue), Jsons.toJson(afterValue), changedBy, reason); }

  @SelectProvider(type=SqlProvider.class, method="users") List<Map<String,Object>> searchUsers(Map<String,Object> p);
  @SelectProvider(type=SqlProvider.class, method="organizations") List<Map<String,Object>> organizations(Map<String,Object> p);
  @SelectProvider(type=SqlProvider.class, method="roles") List<Map<String,Object>> roles(Map<String,Object> p);
  @SelectProvider(type=SqlProvider.class, method="assignments") List<Map<String,Object>> assignments(Map<String,Object> p);
  @SelectProvider(type=SqlProvider.class, method="permissions") List<Map<String,Object>> permissions(Map<String,Object> p);
  @SelectProvider(type=SqlProvider.class, method="menus") List<Map<String,Object>> menus(Map<String,Object> p);
  @SelectProvider(type=SqlProvider.class, method="codeGroups") List<Map<String,Object>> codeGroups(Map<String,Object> p);
  @SelectProvider(type=SqlProvider.class, method="codes") List<Map<String,Object>> codes(Map<String,Object> p);
  @Select("select * from menu where menu_id=#{menuId}") Map<String,Object> rawMenu(String menuId);
  @Select("select menu_id as \"menuId\" from menu where parent_menu_id is not distinct from #{parentMenuId} order by display_order") List<String> siblingMenuIds(String parentMenuId);

  @Update("update user_account set system_use_yn=#{systemUseYn}, status=case when #{systemUseYn}='Y' then 'ACTIVE' else 'DISABLED' end, updated_at=now() where user_id=#{userId}") int updateUserAccess(String userId, String systemUseYn);
  @Update("update user_role_assignment set status='REVOKED', valid_to=current_date, updated_at=now() where user_id=#{userId} and grant_type='MANUAL' and status='ACTIVE'") int revokeManualRoles(String userId);
  @Insert("insert into user_role_assignment(user_id,role_code,grant_type,approver_user_id,valid_from,valid_to,status) values(#{userId},#{roleCode},#{grantType},#{approverUserId},cast(#{validFrom} as date),cast(#{validTo} as date),'ACTIVE')") void insertAssignment(Map<String,Object> p);
  @Update("update user_role_assignment set role_code=#{roleCode}, grant_type=#{grantType}, approver_user_id=#{approverUserId}, valid_from=cast(#{validFrom} as date), valid_to=cast(#{validTo} as date), updated_at=now() where assignment_id=#{assignmentId} and status <> 'REVOKED'") int updateAssignment(Map<String,Object> p);
  @Update("update user_role_assignment set status='REVOKED', updated_at=now() where assignment_id=#{assignmentId} and status <> 'REVOKED'") int revokeAssignment(long assignmentId);

  @InsertProvider(type=SqlProvider.class, method="insertRole") int insertRole(Map<String,Object> p);
  @UpdateProvider(type=SqlProvider.class, method="updateRole") int updateRole(Map<String,Object> p);
  @InsertProvider(type=SqlProvider.class, method="insertOrgRelation") int insertOrgRelation(Map<String,Object> p);
  @Update("update organization_relation_history set effective_end_date=cast(#{endDate} as date), updated_at=now() where organization_code=#{organizationCode} and effective_end_date is null") int closeOrgRelation(String organizationCode, String endDate);
  @Update("update menu_permission set access_yn=#{accessYn}, updated_at=now() where target_type=#{targetType} and target_id=#{targetId} and menu_id=#{menuId}") int updatePermission(Map<String,Object> p);
  @Insert("insert into menu_permission(target_type,target_id,menu_id,access_yn) values(#{targetType},#{targetId},#{menuId},#{accessYn})") int insertPermission(Map<String,Object> p);
  @Update("update menu set display_order=#{displayOrder}, updated_at=now() where menu_id=#{menuId}") int updateMenuOrder(String menuId, int displayOrder);
  @InsertProvider(type=SqlProvider.class, method="insertMenu") int insertMenu(Map<String,Object> p);
  @UpdateProvider(type=SqlProvider.class, method="updateMenu") int updateMenu(Map<String,Object> p);
  @InsertProvider(type=SqlProvider.class, method="insertCodeGroup") int insertCodeGroup(Map<String,Object> p);
  @UpdateProvider(type=SqlProvider.class, method="updateCodeGroup") int updateCodeGroup(Map<String,Object> p);
  @InsertProvider(type=SqlProvider.class, method="insertCode") int insertCode(Map<String,Object> p);
  @UpdateProvider(type=SqlProvider.class, method="updateCode") int updateCode(Map<String,Object> p);
}
