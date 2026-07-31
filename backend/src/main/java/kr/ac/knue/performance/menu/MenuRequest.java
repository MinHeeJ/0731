package kr.ac.knue.performance.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MenuRequest(UUID parentMenuId, @NotBlank String menuName, @NotBlank String menuLevel, @NotNull Integer displayOrder, String screenId, String urlPath, String iconName, String businessCategory, String description, Boolean isActive, String reason) {}
