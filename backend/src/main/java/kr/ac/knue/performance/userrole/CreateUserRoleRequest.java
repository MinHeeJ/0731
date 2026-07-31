package kr.ac.knue.performance.userrole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record CreateUserRoleRequest(@NotNull UUID userId, @NotBlank String roleCode, @NotNull LocalDate validFrom, LocalDate validTo, String assignmentType, String reason) {}
