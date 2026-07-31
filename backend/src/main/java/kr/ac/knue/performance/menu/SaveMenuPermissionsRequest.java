package kr.ac.knue.performance.menu;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SaveMenuPermissionsRequest(@NotBlank String targetType, @NotBlank String targetId, @NotEmpty List<@Valid Permission> permissions, String reason) {
  public record Permission(@NotNull UUID menuId, @NotNull Boolean allowed) {}
}
