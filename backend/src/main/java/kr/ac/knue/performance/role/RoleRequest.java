package kr.ac.knue.performance.role;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(@NotBlank String roleCode, @NotBlank String roleName, String purpose, String grantCriteria, String defaultDataScope, String reason) {}
