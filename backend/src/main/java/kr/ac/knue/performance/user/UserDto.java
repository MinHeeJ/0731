package kr.ac.knue.performance.user;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserDto(UUID userId, String staffNo, String staffName, String organizationCode, String positionName, String jobGrade, String employmentStatus, List<String> roleCodes, boolean systemUseEnabled, LocalDate retirementDate, OffsetDateTime lastSyncedAt) {}
