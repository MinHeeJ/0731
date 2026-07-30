package kr.ac.knue.cms.common;

import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.*;

@Mapper
public interface SessionMapper {
  @Select("""
      select a.user_id as "userId", coalesce(k.name, a.user_id) as "name",
             coalesce(string_agg(ur.role_code, ',' order by ur.role_code) filter (where ur.status='ACTIVE'), '') as "roles"
      from session s
      join app_user a on a.user_id=s.user_id
      left join korus_personnel_snapshot k on k.person_id=a.korus_person_id
      left join user_role ur on ur.user_id=a.user_id and ur.status='ACTIVE'
      where s.session_id=#{sessionId} and s.expires_at > now() and a.use_yn='Y' and a.status='ACTIVE'
      group by a.user_id, k.name
      """)
  Map<String, Object> findUserOnly(@Param("sessionId") String sessionId);

  default SessionUser findSessionUser(String sessionId) {
    Map<String, Object> row = findUserOnly(sessionId);
    if (row == null) return null;
    String roles = String.valueOf(row.getOrDefault("roles", ""));
    java.util.List<String> roleList = roles.isBlank() ? java.util.List.of() : java.util.Arrays.asList(roles.split(","));
    return new SessionUser(sessionId, String.valueOf(row.get("userId")), String.valueOf(row.get("name")), roleList);
  }

  @Select("select password_hash from app_user where user_id=#{userId} and use_yn='Y' and status='ACTIVE'")
  String findPasswordHash(@Param("userId") String userId);

  @Insert("insert into session(session_id,user_id,expires_at,created_at,updated_at) values(#{sessionId},#{userId},#{expiresAt},now(),now())")
  void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("expiresAt") LocalDateTime expiresAt);

  @Delete("delete from session where session_id=#{sessionId}")
  void deleteSession(@Param("sessionId") String sessionId);

  @Insert("insert into change_history(entity_name,entity_id,before_value,after_value,actor_id,reason,created_at) values(#{entityName},#{entityId},cast(#{beforeValue} as jsonb),cast(#{afterValue} as jsonb),#{actorId},#{reason},now())")
  void insertHistory(@Param("entityName") String entityName, @Param("entityId") String entityId, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("actorId") String actorId, @Param("reason") String reason);
}
