package kr.ac.knue.cms.common;

import java.util.Map;

public class AdminSqlProvider {
  private static String like(String[] cols) {
    StringBuilder sb = new StringBuilder(" and (");
    for (int i = 0; i < cols.length; i++) {
      if (i > 0) sb.append(" or ");
      sb.append(cols[i]).append(" ilike '%' || #{q} || '%'");
    }
    return sb.append(")").toString();
  }
  public String users(Map<String,Object> p) {
    String sql = """
      select a.user_id as "userId", k.person_id as "personId", k.name, o.org_name as "orgName", k.org_code as "orgCode",
             k.rank_name as "rankName", k.position_name as "positionName", k.employment_status as "employmentStatus",
             k.retired_at as "retiredAt", k.last_synced_at as "lastSyncedAt", a.use_yn as "useYn", a.status,
             coalesce(string_agg(ur.role_code, ',' order by ur.role_code) filter (where ur.status='ACTIVE'), '') as "roleCodes"
      from app_user a
      left join korus_personnel_snapshot k on k.person_id=a.korus_person_id
      left join org o on o.org_code=k.org_code
      left join user_role ur on ur.user_id=a.user_id
      where 1=1
      """;
    if (has(p,"q")) sql += like(new String[]{"a.user_id","k.name","o.org_name","k.rank_name","k.employment_status"});
    if (has(p,"filter")) sql += " and a.use_yn=#{filter}";
    return sql + " group by a.user_id,k.person_id,k.name,o.org_name,k.org_code,k.rank_name,k.position_name,k.employment_status,k.retired_at,k.last_synced_at,a.use_yn,a.status order by k.name nulls last limit #{size} offset #{offset}";
  }
  public String simpleList(Map<String,Object> p) {
    String table=(String)p.get("table"); String order=(String)p.get("orderBy"); String sql="select * from "+table+" where 1=1";
    if (has(p,"q")) sql += " and cast("+order+" as text) ilike '%' || #{q} || '%'";
    return sql+" order by "+order+" limit #{size} offset #{offset}";
  }
  public String menuPermissions(Map<String,Object> p) {
    String sql="""
      select mp.permission_id as "permissionId", mp.target_type as "targetType", mp.target_id as "targetId",
             m.menu_id as "menuId", m.menu_name as "menuName", m.menu_type as "menuType", m.url,
             mp.access_allowed as "accessAllowed"
      from menu_permission mp join menu m on m.menu_id=mp.menu_id where 1=1
      """;
    if (has(p,"targetType")) sql += " and mp.target_type=#{targetType}";
    if (has(p,"targetId")) sql += " and mp.target_id=#{targetId}";
    return sql+" order by m.display_order, m.menu_id";
  }
  public String codeDetails(Map<String,Object> p) {
    String sql = "select * from code_detail where group_id=#{groupId}";
    if (has(p, "q")) sql += " and (code_value ilike '%' || #{q} || '%' or code_name ilike '%' || #{q} || '%')";
    return sql + " order by sort_order, code_value limit #{size} offset #{offset}";
  }
  private boolean has(Map<String,Object> p, String key) { Object v=p.get(key); return v!=null && !String.valueOf(v).isBlank(); }
}
