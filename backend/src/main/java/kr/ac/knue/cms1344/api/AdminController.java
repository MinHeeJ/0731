package kr.ac.knue.cms1344.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import kr.ac.knue.cms1344.api.Requests.*;
import kr.ac.knue.cms1344.common.AdminService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AdminController {
  private final AdminService service;
  public AdminController(AdminService service) { this.service = service; }

  @GetMapping("/health") ApiResponse health() { return ApiResponse.ok(service.health()); }
  @PostMapping("/auth/login") ApiResponse login(@RequestBody LoginRequest body, HttpServletResponse response) { return ApiResponse.ok(service.login(body, response)); }
  @PostMapping("/auth/logout") ApiResponse logout(HttpServletRequest request, HttpServletResponse response) { return ApiResponse.ok(service.logout(request, response)); }
  @GetMapping("/auth/me") ApiResponse me(HttpServletRequest request) { return ApiResponse.ok(service.me(request)); }

  @GetMapping("/users") ApiResponse users(@RequestParam Map<String,Object> params, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.listUsers(params, request))); }
  @GetMapping("/users/{userId}") ApiResponse user(@PathVariable String userId, HttpServletRequest request) { return ApiResponse.ok(service.getUser(userId, request)); }
  @PatchMapping("/users/{userId}/usage") ApiResponse userUsage(@PathVariable String userId, @RequestBody UpdateUserUsageRequest body, HttpServletRequest request) { return ApiResponse.ok(service.updateUserUsage(userId, body, request)); }
  @PutMapping("/users/{userId}/roles") ApiResponse userRoles(@PathVariable String userId, @RequestBody ReplaceUserRolesRequest body, HttpServletRequest request) { return ApiResponse.ok(service.replaceUserRoles(userId, body, request)); }

  @GetMapping("/organizations") ApiResponse organizations(@RequestParam Map<String,Object> params, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.listOrganizations(params, request))); }
  @GetMapping("/organizations/tree") ApiResponse organizationTree(HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.organizationTree(request))); }
  @PutMapping("/organizations/{orgCode}/relations") ApiResponse orgRelation(@PathVariable String orgCode, @RequestBody OrganizationRelationRequest body, HttpServletRequest request) { return ApiResponse.ok(service.upsertOrganizationRelation(orgCode, body, request)); }

  @GetMapping("/roles") ApiResponse roles(@RequestParam Map<String,Object> params, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.listRoles(params, request))); }
  @PutMapping("/roles") ApiResponse role(@RequestBody RolePolicyRequest body, HttpServletRequest request) { return ApiResponse.ok(service.upsertRolePolicy(body, request)); }
  @GetMapping("/user-roles") ApiResponse listUserRoles(@RequestParam Map<String,Object> params, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.listUserRoles(params, request))); }
  @PostMapping("/user-roles") ApiResponse assignUserRole(@RequestBody UserRoleAssignmentRequest body, HttpServletRequest request) { return ApiResponse.ok(service.assignUserRole(body, request)); }
  @DeleteMapping("/user-roles/{assignmentId}") ApiResponse revokeUserRole(@PathVariable String assignmentId, @RequestBody RevokeRequest body, HttpServletRequest request) { return ApiResponse.ok(service.revokeUserRole(assignmentId, body, request)); }

  @GetMapping("/menu-permissions/matrix") ApiResponse matrix(@RequestParam String targetType, @RequestParam String targetId, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.permissionMatrix(targetType, targetId, request))); }
  @PutMapping("/menu-permissions/matrix") ApiResponse saveMatrix(@RequestBody MenuPermissionMatrixRequest body, HttpServletRequest request) { return ApiResponse.ok(service.savePermissionMatrix(body, request)); }
  @GetMapping("/menus/tree") ApiResponse menus(HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.menuTree(request))); }
  @PatchMapping("/menus/{menuId}/move") ApiResponse move(@PathVariable String menuId, @RequestBody MenuMoveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.moveMenu(menuId, body, request)); }
  @PatchMapping("/menus/reorder") ApiResponse reorder(@RequestBody MenuReorderRequest body, HttpServletRequest request) { return ApiResponse.ok(service.reorderMenus(body, request)); }
  @GetMapping("/menu-info") ApiResponse menuInfo(@RequestParam Map<String,Object> params, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.listMenuInfo(params, request))); }
  @PutMapping("/menu-info") ApiResponse upsertMenuInfo(@RequestBody MenuInfoRequest body, HttpServletRequest request) { return ApiResponse.ok(service.upsertMenuInfo(body, request)); }

  @GetMapping("/code-groups") ApiResponse codeGroups(@RequestParam Map<String,Object> params, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.listCodeGroups(params, request))); }
  @PutMapping("/code-groups") ApiResponse upsertCodeGroup(@RequestBody CodeGroupRequest body, HttpServletRequest request) { return ApiResponse.ok(service.upsertCodeGroup(body, request)); }
  @GetMapping("/code-details") ApiResponse codeDetails(@RequestParam Map<String,Object> params, HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.listCodeDetails(params, request))); }
  @PutMapping("/code-details") ApiResponse upsertCodeDetail(@RequestBody CodeDetailRequest body, HttpServletRequest request) { return ApiResponse.ok(service.upsertCodeDetail(body, request)); }
}
