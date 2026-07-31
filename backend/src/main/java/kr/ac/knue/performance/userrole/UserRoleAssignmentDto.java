package kr.ac.knue.performance.userrole;

import java.time.LocalDate;
import java.util.UUID;

public record UserRoleAssignmentDto(UUID assignmentId, UUID userId, String roleCode, String assignmentType, LocalDate validFrom, LocalDate validTo, UUID approvedByUserId, String status) {}
