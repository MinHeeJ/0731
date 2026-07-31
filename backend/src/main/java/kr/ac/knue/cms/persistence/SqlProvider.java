package kr.ac.knue.cms.persistence;

import java.util.Map;

public class SqlProvider {
    private static boolean has(Object value) {
        return value != null && !String.valueOf(value).isBlank();
    }

    public String listUsers(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select u.user_id as "userId", p.person_id as "personId", p.name as "name", p.org_code as "orgCode", o.org_name as "orgName", p.position_name as "positionName", p.rank_name as "rankName", p.employment_status as "employmentStatus", p.retired_at as "retiredAt", p.last_synced_at as "lastSyncedAt", u.use_yn as "useYn", u.status as "status", coalesce(string_agg(distinct ur.role_code, ','), '') as "roleCodes"
            from app_user u left join korus_personnel_snapshot p on p.person_id = u.korus_person_id left join org o on o.org_code = p.org_code left join user_role ur on ur.user_id = u.user_id and ur.status = 'ACTIVE'
            where 1 = 1
            """);
        if (has(p.get("q"))) sql.append(" and (u.user_id ilike '%' || #{q} || '%' or p.name ilike '%' || #{q} || '%' or p.org_code ilike '%' || #{q} || '%' or p.rank_name ilike '%' || #{q} || '%')");
        if (has(p.get("roleCode"))) sql.append(" and exists (select 1 from user_role r where r.user_id = u.user_id and r.role_code = #{roleCode} and r.status = 'ACTIVE')");
        if (has(p.get("useYn"))) sql.append(" and u.use_yn = #{useYn}");
        sql.append(" group by u.user_id, p.person_id, p.name, p.org_code, o.org_name, p.position_name, p.rank_name, p.employment_status, p.retired_at, p.last_synced_at, u.use_yn, u.status order by p.name asc, u.user_id asc");
        return sql.toString();
    }

    public String listPersonnelSnapshots(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select person_id as "personId", name, org_code as "orgCode", position_name as "positionName", rank_name as "rankName", employment_status as "employmentStatus", retired_at as "retiredAt", last_synced_at as "lastSyncedAt"
            from korus_personnel_snapshot where 1=1
            """);
        if (has(p.get("q"))) sql.append(" and (person_id ilike '%' || #{q} || '%' or name ilike '%' || #{q} || '%')");
        return sql.append(" order by name").toString();
    }

    public String listOrgs(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select org_code as "orgCode", org_name as "orgName", org_type as "orgType", use_yn as "useYn", created_at as "createdAt", updated_at as "updatedAt"
            from org where 1=1
            """);
        if (has(p.get("q"))) sql.append(" and (org_code ilike '%' || #{q} || '%' or org_name ilike '%' || #{q} || '%')");
        return sql.append(" order by org_code").toString();
    }

    public String listOrgTree(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select r.relation_id as "relationId", o.org_code as "orgCode", o.org_name as "orgName", o.org_type as "orgType", r.parent_org_code as "parentOrgCode", r.valid_from as "validFrom", r.valid_to as "validTo", r.change_reason as "changeReason"
            from org o left join org_relation r on r.org_code = o.org_code where 1=1
            """);
        if (has(p.get("q"))) sql.append(" and (o.org_code ilike '%' || #{q} || '%' or o.org_name ilike '%' || #{q} || '%')");
        return sql.append(" order by coalesce(r.parent_org_code, ''), o.org_code").toString();
    }

    public String listRoles(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select role_code as "roleCode", role_name as "roleName", purpose, grant_criteria as "grantCriteria", default_data_scope as "defaultDataScope", use_yn as "useYn", created_at as "createdAt", updated_at as "updatedAt"
            from role where 1=1
            """);
        if (has(p.get("q"))) sql.append(" and (role_code ilike '%' || #{q} || '%' or role_name ilike '%' || #{q} || '%')");
        if (has(p.get("useYn"))) sql.append(" and use_yn = #{useYn}");
        return sql.append(" order by role_code").toString();
    }

    public String listUserRoles(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select ur.assignment_id as "assignmentId", ur.user_id as "userId", ps.name as "name", ur.role_code as "roleCode", r.role_name as "roleName", ur.assignment_type as "assignmentType", ur.valid_from as "validFrom", ur.valid_to as "validTo", ur.approver_id as "approverId", ur.status
            from user_role ur join role r on r.role_code = ur.role_code join app_user u on u.user_id = ur.user_id left join korus_personnel_snapshot ps on ps.person_id = u.korus_person_id where 1=1
            """);
        if (has(p.get("q"))) sql.append(" and (ur.user_id ilike '%' || #{q} || '%' or ps.name ilike '%' || #{q} || '%')");
        if (has(p.get("roleCode"))) sql.append(" and ur.role_code = #{roleCode}");
        if (has(p.get("status"))) sql.append(" and ur.status = #{status}");
        return sql.append(" order by ur.updated_at desc, ur.assignment_id desc").toString();
    }

    public String listMenuPermissions(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select coalesce(mp.permission_id, 0) as "permissionId", #{targetType} as "targetType", #{targetId} as "targetId", m.menu_id as "menuId", m.parent_menu_id as "parentMenuId", m.menu_name as "menuName", m.menu_type as "menuType", m.url, coalesce(mp.access_allowed, false) as "accessAllowed"
            from menu m left join menu_permission mp on mp.menu_id = m.menu_id
            """);
        if (has(p.get("targetType"))) sql.append(" and mp.target_type = #{targetType}");
        if (has(p.get("targetId"))) sql.append(" and mp.target_id = #{targetId}");
        sql.append(" where m.use_yn = 'Y' order by m.display_order, m.menu_id");
        return sql.toString();
    }

    public String listMenus(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select menu_id as "menuId", parent_menu_id as "parentMenuId", menu_type as "menuType", menu_name as "menuName", screen_id as "screenId", url, icon, business_category as "businessCategory", description, display_order as "displayOrder", use_yn as "useYn", updated_at as "updatedAt"
            from menu where 1=1
            """);
        if (has(p.get("q"))) sql.append(" and (menu_name ilike '%' || #{q} || '%' or screen_id ilike '%' || #{q} || '%' or url ilike '%' || #{q} || '%')");
        return sql.append(" order by display_order, menu_id").toString();
    }

    public String listCodeGroups(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select group_id as "groupId", group_name as "groupName", description, managing_department as "managingDepartment", use_yn as "useYn", created_at as "createdAt", updated_at as "updatedAt"
            from code_group where 1=1
            """);
        if (has(p.get("q"))) sql.append(" and (group_id ilike '%' || #{q} || '%' or group_name ilike '%' || #{q} || '%' or managing_department ilike '%' || #{q} || '%')");
        if (has(p.get("useYn"))) sql.append(" and use_yn = #{useYn}");
        return sql.append(" order by group_id").toString();
    }

    public String listCodeDetails(Map<String, Object> p) {
        StringBuilder sql = new StringBuilder("""
            select group_id as "groupId", code_value as "codeValue", code_name as "codeName", parent_code_value as "parentCodeValue", sort_order as "sortOrder", extra_attributes::text as "extraAttributes", valid_from as "validFrom", valid_to as "validTo", use_yn as "useYn", updated_at as "updatedAt"
            from code_detail where group_id = #{groupId}
            """);
        if (has(p.get("q"))) sql.append(" and (code_value ilike '%' || #{q} || '%' or code_name ilike '%' || #{q} || '%')");
        return sql.append(" order by sort_order, code_value").toString();
    }
}
