package kr.ac.knue.test0731.admin;

public record AdminResource(String slug, String tableName, String idColumn, String statusColumn, String defaultStatus) {
    public static AdminResource of(String slug) {
        return switch (slug) {
            case "users" -> new AdminResource(slug, "local_user_account", "local_user_account_id", "use_status", "ENABLED");
            case "organizations" -> new AdminResource(slug, "local_organization_setting", "local_organization_setting_id", "use_status", "ENABLED");
            case "roles" -> new AdminResource(slug, "role", "role_id", "use_status", "ENABLED");
            case "user-roles" -> new AdminResource(slug, "user_role_assignment", "user_role_assignment_id", "effective_status", "ACTIVE");
            case "menu-permissions" -> new AdminResource(slug, "menu_permission", "menu_permission_id", "permission_action", "READ");
            case "menu-structures" -> new AdminResource(slug, "menu", "menu_id", null, null);
            case "menus" -> new AdminResource(slug, "menu", "menu_id", "display_status", "VISIBLE");
            case "code-groups" -> new AdminResource(slug, "code_group", "code_group_id", "use_status", "ENABLED");
            case "detail-codes" -> new AdminResource(slug, "detail_code", "detail_code_id", "use_status", "ENABLED");
            default -> throw new IllegalArgumentException("지원하지 않는 관리 리소스입니다");
        };
    }
    public String requestStatus(AdminItem item) {
        if (statusColumn == null) { return null; }
        if ("effective_status".equals(statusColumn)) { return item.getEffectiveStatus() == null ? defaultStatus : item.getEffectiveStatus(); }
        if ("permission_action".equals(statusColumn)) { return item.getPermissionAction() == null ? defaultStatus : item.getPermissionAction(); }
        if ("display_status".equals(statusColumn)) { return item.getDisplayStatus() == null ? defaultStatus : item.getDisplayStatus(); }
        return item.getUseStatus() == null ? defaultStatus : item.getUseStatus();
    }
}
