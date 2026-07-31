package kr.ac.knue.cms.common.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.knue.cms.common.domain.ApiResponse;
import kr.ac.knue.cms.common.domain.GenericSaveRequest;
import kr.ac.knue.cms.common.domain.SessionUser;
import kr.ac.knue.cms.common.domain.UsageUpdateRequest;
import kr.ac.knue.cms.common.domain.UserRolesRequest;
import kr.ac.knue.cms.common.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() { return ApiResponse.ok(service.health()); }

    @GetMapping("/admin/me/menus")
    public ApiResponse<Map<String, Object>> myMenus(HttpServletRequest request) { return ApiResponse.ok(Map.of("items", service.myMenus(user(request)))); }

    @GetMapping("/admin/users")
    public ApiResponse<Map<String, Object>> users(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String filter) { return ApiResponse.ok(service.page(service.users(filter, page, size), page, size)); }

    @PatchMapping("/admin/users/{userId}/usage")
    public ApiResponse<Map<String, Object>> updateUserUsage(@PathVariable String userId, @Valid @RequestBody UsageUpdateRequest body, HttpServletRequest request) { return ApiResponse.ok(service.updateUserUsage(userId, body, user(request))); }

    @PutMapping("/admin/users/{userId}/roles")
    public ApiResponse<Map<String, Object>> replaceUserRoles(@PathVariable String userId, @Valid @RequestBody UserRolesRequest body, HttpServletRequest request) { return ApiResponse.ok(service.replaceUserRoles(userId, body, user(request))); }

    @GetMapping("/admin/organizations")
    public ApiResponse<Map<String, Object>> organizations(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String filter) { return ApiResponse.ok(service.page(service.organizations(filter, page, size), page, size)); }

    @PutMapping("/admin/organizations/{orgCode}/relations")
    public ApiResponse<Map<String, Object>> saveOrganizationRelations(@PathVariable String orgCode, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.saveOrganizationRelation(orgCode, body, user(request))); }

    @GetMapping("/admin/roles")
    public ApiResponse<Map<String, Object>> roles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String filter) { return ApiResponse.ok(service.page(service.roles(filter, page, size), page, size)); }

    @PostMapping("/admin/roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRole(@RequestBody GenericSaveRequest body, HttpServletRequest request) { return ResponseEntity.status(201).body(ApiResponse.ok(service.saveRole(null, body, true, user(request)))); }

    @PutMapping("/admin/roles/{roleCode}")
    public ApiResponse<Map<String, Object>> updateRole(@PathVariable String roleCode, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.saveRole(roleCode, body, false, user(request))); }

    @GetMapping("/admin/user-roles")
    public ApiResponse<Map<String, Object>> userRoles(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String filter) { return ApiResponse.ok(service.page(service.userRoles(filter, page, size), page, size)); }

    @PostMapping("/admin/user-roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignUserRole(@RequestBody GenericSaveRequest body, HttpServletRequest request) { return ResponseEntity.status(201).body(ApiResponse.ok(service.assignUserRole(body, user(request)))); }

    @PutMapping("/admin/user-roles/{assignmentId}")
    public ApiResponse<Map<String, Object>> updateUserRole(@PathVariable Long assignmentId, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.updateUserRole(assignmentId, body, user(request))); }

    @DeleteMapping("/admin/user-roles/{assignmentId}")
    public ApiResponse<Map<String, Object>> revokeUserRole(@PathVariable Long assignmentId, HttpServletRequest request) { return ApiResponse.ok(service.revokeUserRole(assignmentId, user(request))); }

    @GetMapping("/admin/menu-permissions")
    public ApiResponse<Map<String, Object>> menuPermissions(@RequestParam(required = false) String targetType, @RequestParam(required = false) String targetId) { return ApiResponse.ok(Map.of("items", service.menuPermissions(targetType, targetId))); }

    @PutMapping("/admin/menu-permissions")
    public ApiResponse<Map<String, Object>> saveMenuPermissions(@RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.saveMenuPermission(body, user(request))); }

    @GetMapping("/admin/menus/tree")
    public ApiResponse<Map<String, Object>> menuTree() { return ApiResponse.ok(Map.of("items", service.menuTree())); }

    @PutMapping("/admin/menus/{menuId}/parent")
    public ApiResponse<Map<String, Object>> changeMenuParent(@PathVariable String menuId, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.changeMenuParent(menuId, body, user(request))); }

    @PutMapping("/admin/menus/reorder")
    public ApiResponse<Map<String, Object>> reorderMenus(@RequestBody List<GenericSaveRequest> body, HttpServletRequest request) { return ApiResponse.ok(service.reorderMenus(body, user(request))); }

    @GetMapping("/admin/menus")
    public ApiResponse<Map<String, Object>> menus(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String filter) { return ApiResponse.ok(service.page(service.menus(filter, page, size), page, size)); }

    @PostMapping("/admin/menus")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMenu(@RequestBody GenericSaveRequest body, HttpServletRequest request) { return ResponseEntity.status(201).body(ApiResponse.ok(service.saveMenu(null, body, true, user(request)))); }

    @PutMapping("/admin/menus/{menuId}")
    public ApiResponse<Map<String, Object>> updateMenu(@PathVariable String menuId, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.saveMenu(menuId, body, false, user(request))); }

    @GetMapping("/admin/code-groups")
    public ApiResponse<Map<String, Object>> codeGroups(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) String filter) { return ApiResponse.ok(service.page(service.codeGroups(filter, page, size), page, size)); }

    @PostMapping("/admin/code-groups")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCodeGroup(@RequestBody GenericSaveRequest body, HttpServletRequest request) { return ResponseEntity.status(201).body(ApiResponse.ok(service.saveCodeGroup(null, body, true, user(request)))); }

    @PutMapping("/admin/code-groups/{groupId}")
    public ApiResponse<Map<String, Object>> updateCodeGroup(@PathVariable String groupId, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.saveCodeGroup(groupId, body, false, user(request))); }

    @GetMapping("/admin/code-groups/{groupId}/codes")
    public ApiResponse<Map<String, Object>> detailCodes(@PathVariable String groupId) { return ApiResponse.ok(Map.of("items", service.detailCodes(groupId))); }

    @PostMapping("/admin/code-groups/{groupId}/codes")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDetailCode(@PathVariable String groupId, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ResponseEntity.status(201).body(ApiResponse.ok(service.saveDetailCode(groupId, null, body, true, user(request)))); }

    @PutMapping("/admin/code-groups/{groupId}/codes/{codeValue}")
    public ApiResponse<Map<String, Object>> updateDetailCode(@PathVariable String groupId, @PathVariable String codeValue, @RequestBody GenericSaveRequest body, HttpServletRequest request) { return ApiResponse.ok(service.saveDetailCode(groupId, codeValue, body, false, user(request))); }

    private SessionUser user(HttpServletRequest request) { return (SessionUser) request.getAttribute("sessionUser"); }
}
