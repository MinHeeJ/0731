package kr.ac.knue.cms.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService service;

  public AdminController(AdminService service) { this.service = service; }

  @GetMapping("/users")
  public ApiResponse<List<Map<String,Object>>> listUsers(@RequestParam(required=false) String q, @RequestParam(required=false) String filter, @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="0") int page) {
    return service.listUsers(q, filter, size, page);
  }

  @PatchMapping("/users/{userId}/account")
  public ResponseEntity<ApiResponse<?>> updateUserAccount(@PathVariable String userId, @RequestBody Map<String,Object> body, HttpServletRequest request) {
    return service.updateUserAccount(userId, body, request);
  }

  @PutMapping("/users/{userId}/roles")
  public ResponseEntity<ApiResponse<?>> replaceRoles(@PathVariable String userId, @RequestBody Map<String,Object> body, HttpServletRequest request) {
    return service.replaceRoles(userId, body, request);
  }

  @GetMapping("/orgs")
  public ApiResponse<List<Map<String,Object>>> orgs(@RequestParam(required=false) String q, @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="0") int page) {
    return service.orgs(q, size, page);
  }

  @GetMapping("/orgs/tree")
  public ApiResponse<List<Map<String,Object>>> orgTree() { return service.orgTree(); }

  @PutMapping("/org-relations/{relationId}")
  public ResponseEntity<ApiResponse<?>> updateOrgRelation(@PathVariable long relationId, @RequestBody Map<String,Object> body, HttpServletRequest request) {
    return service.updateOrgRelation(relationId, body, request);
  }

  @GetMapping("/roles")
  public ApiResponse<List<Map<String,Object>>> roles(@RequestParam(required=false) String q, @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="0") int page) {
    return service.roles(q, size, page);
  }

  @PutMapping("/roles/{roleCode}")
  public ResponseEntity<ApiResponse<?>> updateRole(@PathVariable String roleCode, @RequestBody Map<String,Object> body, HttpServletRequest request) {
    return service.updateRole(roleCode, body, request);
  }

  @GetMapping("/user-roles")
  public ApiResponse<List<Map<String,Object>>> userRoles(@RequestParam(required=false) String q, @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="0") int page) {
    return service.userRoles(q, size, page);
  }

  @PostMapping("/user-roles")
  public ResponseEntity<ApiResponse<?>> grantUserRole(@RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.grantUserRole(row, request);
  }

  @PatchMapping("/user-roles/{assignmentId}")
  public ResponseEntity<ApiResponse<?>> updateUserRole(@PathVariable long assignmentId, @RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.updateUserRole(assignmentId, row, request);
  }

  @DeleteMapping("/user-roles/{assignmentId}")
  public ResponseEntity<ApiResponse<?>> revokeUserRole(@PathVariable long assignmentId, HttpServletRequest request) {
    return service.revokeUserRole(assignmentId, request);
  }

  @GetMapping("/menu-permissions")
  public ApiResponse<List<Map<String,Object>>> permissions(@RequestParam(required=false) String targetType, @RequestParam(required=false) String targetId) {
    return service.permissions(targetType, targetId);
  }

  @PutMapping("/menu-permissions")
  public ResponseEntity<ApiResponse<?>> savePermissions(@RequestBody Map<String,Object> body, HttpServletRequest request) {
    return service.savePermissions(body, request);
  }

  @GetMapping("/menus/tree")
  public ApiResponse<List<Map<String,Object>>> menuTree() { return service.menuTree(); }

  @PutMapping("/menus/{menuId}/structure")
  public ResponseEntity<ApiResponse<?>> updateMenuStructure(@PathVariable long menuId, @RequestBody Map<String,Object> body, HttpServletRequest request) {
    return service.updateMenuStructure(menuId, body, request);
  }

  @PutMapping("/menus/reorder")
  public ResponseEntity<ApiResponse<?>> reorderMenus(@RequestBody Map<String,Object> body, HttpServletRequest request) {
    return service.reorderMenus(body, request);
  }

  @GetMapping("/menus")
  public ApiResponse<List<Map<String,Object>>> menus(@RequestParam(required=false) String q, @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="0") int page) {
    return service.menus(q, size, page);
  }

  @PostMapping("/menus")
  public ResponseEntity<ApiResponse<?>> createMenu(@RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.createMenu(row, request);
  }

  @PutMapping("/menus/{menuId}")
  public ResponseEntity<ApiResponse<?>> updateMenu(@PathVariable long menuId, @RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.updateMenu(menuId, row, request);
  }

  @GetMapping("/code-groups")
  public ApiResponse<List<Map<String,Object>>> codeGroups(@RequestParam(required=false) String q, @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="0") int page) {
    return service.codeGroups(q, size, page);
  }

  @PostMapping("/code-groups")
  public ResponseEntity<ApiResponse<?>> createCodeGroup(@RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.createCodeGroup(row, request);
  }

  @PutMapping("/code-groups/{groupId}")
  public ResponseEntity<ApiResponse<?>> updateCodeGroup(@PathVariable String groupId, @RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.updateCodeGroup(groupId, row, request);
  }

  @GetMapping("/code-groups/{groupId}/codes")
  public ApiResponse<List<Map<String,Object>>> codeDetails(@PathVariable String groupId, @RequestParam(required=false) String q, @RequestParam(defaultValue="100") int size, @RequestParam(defaultValue="0") int page) {
    return service.codeDetails(groupId, q, size, page);
  }

  @PostMapping("/code-groups/{groupId}/codes")
  public ResponseEntity<ApiResponse<?>> createCodeDetail(@PathVariable String groupId, @RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.createCodeDetail(groupId, row, request);
  }

  @PutMapping("/code-groups/{groupId}/codes/{codeValue}")
  public ResponseEntity<ApiResponse<?>> updateCodeDetail(@PathVariable String groupId, @PathVariable String codeValue, @RequestBody Map<String,Object> row, HttpServletRequest request) {
    return service.updateCodeDetail(groupId, codeValue, row, request);
  }
}
