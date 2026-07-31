package kr.ac.knue.performance.menu;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MenuController {
  private final MenuService menuService;

  public MenuController(MenuService menuService) {
    this.menuService = menuService;
  }

  @GetMapping("/api/menus")
  ApiResponse<List<MenuDto>> menus(@RequestParam(required = false) UUID parentMenuId, @RequestParam(required = false) String screenId, @RequestParam(required = false) String menuName, HttpServletRequest request) {
    return ApiResponse.ok(menuService.listMenus(parentMenuId, screenId, menuName), requestId(request));
  }

  @PostMapping("/api/menus")
  ApiResponse<MenuDto> create(@Valid @RequestBody MenuRequest body, HttpServletRequest request) {
    return ApiResponse.ok(menuService.createMenu(body), requestId(request));
  }

  @PutMapping("/api/menus/{menuId}")
  ApiResponse<MenuDto> update(@PathVariable UUID menuId, @Valid @RequestBody MenuRequest body, HttpServletRequest request) {
    return ApiResponse.ok(menuService.updateMenu(menuId, body), requestId(request));
  }

  @GetMapping("/api/menu-permissions")
  ApiResponse<List<MenuPermissionDto>> perms(@RequestParam(required = false) String targetType, @RequestParam(required = false) String targetId, HttpServletRequest request) {
    return ApiResponse.ok(menuService.listPermissions(targetType, targetId), requestId(request));
  }

  @PutMapping("/api/menu-permissions")
  ApiResponse<MenuPermissionDto> save(@Valid @RequestBody SaveMenuPermissionsRequest body, HttpServletRequest request) {
    return ApiResponse.ok(menuService.savePermissions(body), requestId(request));
  }

  private String requestId(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE));
  }
}
