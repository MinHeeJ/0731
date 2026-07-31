package kr.ac.knue.performance.organization;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.UUID;

public record OrganizationDto(UUID organizationId, String organizationCode, String organizationName, String organizationType, UUID parentOrganizationId, @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate effectiveStartDate, @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate effectiveEndDate, UUID relationId) {}
