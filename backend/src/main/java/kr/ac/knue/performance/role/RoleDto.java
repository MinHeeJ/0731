package kr.ac.knue.performance.role;

public record RoleDto(String roleCode, String roleName, String purpose, String grantCriteria, String defaultDataScope, boolean isActive) {}
