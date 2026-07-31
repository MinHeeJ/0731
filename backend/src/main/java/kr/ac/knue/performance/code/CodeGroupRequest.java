package kr.ac.knue.performance.code;

import jakarta.validation.constraints.NotBlank;

public record CodeGroupRequest(@NotBlank String groupId, @NotBlank String groupName, String description, String managingDepartment, Boolean isActive, String reason) {}
