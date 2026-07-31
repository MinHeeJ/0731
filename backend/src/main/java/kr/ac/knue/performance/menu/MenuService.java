package kr.ac.knue.performance.menu;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import kr.ac.knue.performance.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuService {
  private static final Set<String> TARGET_TYPES = Set.of("ROLE", "ORG", "USER");
  private final MenuMapper menuMapper;

  public MenuService(MenuMapper menuMapper) {
    this.menuMapper = menuMapper;
  }

  public List<MenuDto> listMenus(UUID parentMenuId, String screenId, String menuName) {
    return menuMapper.list(parentMenuId, blank(screenId), blank(menuName));
  }

  @Transactional
  public MenuDto createMenu(MenuRequest request) {
    menuMapper.insert(request);
    return menuMapper.findLatestByName(request.menuName());
  }

  @Transactional
  public MenuDto updateMenu(UUID menuId, MenuRequest request) {
    if (menuId.equals(request.parentMenuId())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "자기 자신을 부모 메뉴로 지정할 수 없습니다.", Map.of("parentMenuId", "자기 자신은 선택할 수 없습니다."));
    }
    if (menuMapper.update(menuId, request) == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "NOT_FOUND", "메뉴를 찾을 수 없습니다.");
    }
    return menuMapper.find(menuId);
  }

  public List<MenuPermissionDto> listPermissions(String targetType, String targetId) {
    return menuMapper.listPermissions(blank(targetType), blank(targetId));
  }

  @Transactional
  public MenuPermissionDto savePermissions(SaveMenuPermissionsRequest request) {
    if (!TARGET_TYPES.contains(request.targetType())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "대상 유형을 확인해 주세요.", Map.of("targetType", "ROLE/ORG/USER 중 하나여야 합니다."));
    }
    for (SaveMenuPermissionsRequest.Permission permission : request.permissions()) {
      if (menuMapper.updatePermission(request.targetType(), request.targetId(), permission.menuId(), permission.allowed()) == 0) {
        menuMapper.insertPermission(request.targetType(), request.targetId(), permission.menuId(), permission.allowed());
      }
    }
    return menuMapper.listPermissions(request.targetType(), request.targetId()).get(0);
  }

  private String blank(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
