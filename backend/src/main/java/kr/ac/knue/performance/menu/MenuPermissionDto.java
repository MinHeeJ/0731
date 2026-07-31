package kr.ac.knue.performance.menu;

import java.util.UUID;

public record MenuPermissionDto(UUID permissionId, String targetType, String targetId, UUID menuId, Boolean allowed) {}
