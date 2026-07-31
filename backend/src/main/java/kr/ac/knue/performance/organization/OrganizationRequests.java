package kr.ac.knue.performance.organization;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public final class OrganizationRequests {
  private OrganizationRequests() {}
  public record Relation(@NotNull UUID organizationId, UUID parentOrganizationId, @NotNull LocalDate effectiveStartDate, LocalDate effectiveEndDate, String reason) {}
}
