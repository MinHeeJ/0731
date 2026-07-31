package kr.ac.knue.performance.menu;

import java.util.UUID;

public record MenuDto(UUID menuId, UUID parentMenuId, String menuName, String menuLevel, Integer displayOrder, String screenId, String urlPath, String iconName, String businessCategory, String description, boolean isActive) {}
