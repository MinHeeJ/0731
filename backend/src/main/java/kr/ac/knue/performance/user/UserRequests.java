package kr.ac.knue.performance.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public final class UserRequests {
  private UserRequests() {}
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Usage(@NotNull Boolean systemUseEnabled, String reason) {}
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Roles(@NotEmpty List<String> roleCodes, LocalDate validFrom, LocalDate validTo, String reason) {}
}
