package kr.ac.knue.common.admin;

import kr.ac.knue.common.api.ApiResponse;
import kr.ac.knue.common.auth.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AdminController {
  private final AuthService auth; private final AdminService service;
  public AdminController(AuthService auth, AdminService service){this.auth=auth;this.service=service;}
  String by(String sid){ return auth.requireAdmin(sid).userId(); }
  @GetMapping("/health") ApiResponse<Map<String,String>> health(){ return ApiResponse.ok(Map.of("status","UP")); }
  @GetMapping("/admin/me") ApiResponse<Map<String,Object>> me(@CookieValue(name="KNUESESSION", required=false) String sid){ return ApiResponse.ok(service.menuContext(auth.requireAdmin(sid))); }
  @GetMapping("/admin/users") ApiResponse<List<Map<String,Object>>> users(@CookieValue(name="KNUESESSION", required=false) String sid, @RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.users(p)); }
  @PatchMapping("/admin/users/{userId}/access") ApiResponse<Map<String,Object>> access(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String userId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.updateUserAccess(userId,p,by(sid))); }
  @PutMapping("/admin/users/{userId}/roles") ApiResponse<List<Map<String,Object>>> replaceRoles(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String userId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.replaceRoles(userId,p,by(sid))); }
  @GetMapping("/admin/organizations") ApiResponse<List<Map<String,Object>>> orgs(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.organizations(p)); }
  @PutMapping("/admin/organizations/{organizationCode}/relation") ApiResponse<Map<String,Object>> orgRel(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String organizationCode,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.updateOrgRelation(organizationCode,p,by(sid))); }
  @GetMapping("/admin/roles") ApiResponse<List<Map<String,Object>>> roles(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.roles(p)); }
  @PostMapping("/admin/roles") ApiResponse<Map<String,Object>> createRole(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam(defaultValue="R09") String roleCode,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.createRole(roleCode,p,by(sid))); }
  @PutMapping("/admin/roles/{roleCode}") ApiResponse<Map<String,Object>> updateRole(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String roleCode,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.updateRole(roleCode,p,by(sid))); }
  @GetMapping("/admin/user-roles") ApiResponse<List<Map<String,Object>>> assignments(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.assignments(p)); }
  @PostMapping("/admin/user-roles") ApiResponse<Map<String,Object>> grant(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.grantAssignment(p,by(sid))); }
  @PutMapping("/admin/user-roles/{assignmentId}") ApiResponse<Map<String,Object>> updateAssignment(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable long assignmentId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.updateAssignment(assignmentId,p,by(sid))); }
  @DeleteMapping("/admin/user-roles/{assignmentId}") ApiResponse<Map<String,Object>> revoke(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable long assignmentId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.revokeAssignment(assignmentId,p,by(sid))); }
  @GetMapping("/admin/menu-permissions") ApiResponse<List<Map<String,Object>>> perms(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.permissions(p)); }
  @PutMapping("/admin/menu-permissions") ApiResponse<List<Map<String,Object>>> savePerms(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.savePermissions(p,by(sid))); }
  @GetMapping("/admin/menus/tree") ApiResponse<List<Map<String,Object>>> tree(@CookieValue(name="KNUESESSION", required=false) String sid){ by(sid); return ApiResponse.ok(service.menus(Map.of())); }
  @PatchMapping("/admin/menus/tree/reorder") ApiResponse<List<Map<String,Object>>> reorder(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.reorderMenus(p,by(sid))); }
  @GetMapping("/admin/menus") ApiResponse<List<Map<String,Object>>> menus(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.menus(p)); }
  @PostMapping("/admin/menus") ApiResponse<Map<String,Object>> createMenu(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.createMenu(p,by(sid))); }
  @PutMapping("/admin/menus/{menuId}") ApiResponse<Map<String,Object>> updateMenu(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String menuId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.updateMenu(menuId,p,by(sid))); }
  @GetMapping("/admin/code-groups") ApiResponse<List<Map<String,Object>>> groups(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.codeGroups(p)); }
  @PostMapping("/admin/code-groups/{groupId}") ApiResponse<Map<String,Object>> createGroup(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String groupId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.createCodeGroup(groupId,p,by(sid))); }
  @PostMapping("/admin/code-groups") ApiResponse<Map<String,Object>> createGroupBody(@CookieValue(name="KNUESESSION", required=false) String sid,@RequestParam String groupId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.createCodeGroup(groupId,p,by(sid))); }
  @PutMapping("/admin/code-groups/{groupId}") ApiResponse<Map<String,Object>> updateGroup(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String groupId,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.updateCodeGroup(groupId,p,by(sid))); }
  @GetMapping("/admin/code-groups/{groupId}/codes") ApiResponse<List<Map<String,Object>>> codes(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String groupId,@RequestParam Map<String,Object> p){ by(sid); return ApiResponse.ok(service.codes(groupId,p)); }
  @PostMapping("/admin/code-groups/{groupId}/codes/{codeValue}") ApiResponse<Map<String,Object>> createCodePath(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String groupId,@PathVariable String codeValue,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.createCode(groupId,codeValue,p,by(sid))); }
  @PostMapping("/admin/code-groups/{groupId}/codes") ApiResponse<Map<String,Object>> createCode(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String groupId,@RequestParam String codeValue,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.createCode(groupId,codeValue,p,by(sid))); }
  @PutMapping("/admin/code-groups/{groupId}/codes/{codeValue}") ApiResponse<Map<String,Object>> updateCode(@CookieValue(name="KNUESESSION", required=false) String sid,@PathVariable String groupId,@PathVariable String codeValue,@RequestBody Map<String,Object> p){ return ApiResponse.ok(service.updateCode(groupId,codeValue,p,by(sid))); }
}
