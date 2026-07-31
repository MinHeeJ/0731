package kr.ac.knue.cms.admin;

import kr.ac.knue.cms.api.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ApiResponse<Object> listUsers(@RequestParam(required = false, name = "q") String q,
                                         @RequestParam(required = false) String roleCode,
                                         @RequestParam(required = false) String useYn) {
        return adminService.listUsers(q, roleCode, useYn);
    }

    @PatchMapping("/users/{userId}/account")
    public ApiResponse<Object> updateUserAccount(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        return adminService.updateUserAccount(userId, body);
    }

    @PutMapping("/users/{userId}/roles")
    public ApiResponse<Object> replaceUserBusinessRoles(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        return adminService.replaceUserBusinessRoles(userId, body);
    }

    @GetMapping("/orgs")
    public ApiResponse<Object> listOrganizations(@RequestParam(required = false, name = "q") String q) {
        return adminService.listOrganizations(q);
    }

    @GetMapping("/orgs/tree")
    public ApiResponse<Object> getOrganizationTree(@RequestParam(required = false, name = "q") String q) {
        return adminService.getOrganizationTree(q);
    }

    @PutMapping("/org-relations/{relationId}")
    public ApiResponse<Object> updateOrganizationRelation(@PathVariable Long relationId, @RequestBody Map<String, Object> body) {
        return adminService.updateOrganizationRelation(relationId, body);
    }

    @GetMapping("/roles")
    public ApiResponse<Object> listRoles(@RequestParam(required = false, name = "q") String q, @RequestParam(required = false) String useYn) {
        return adminService.listRoles(q, useYn);
    }

    @PutMapping("/roles/{roleCode}")
    public ApiResponse<Object> updateRole(@PathVariable String roleCode, @RequestBody Map<String, Object> body) {
        return adminService.updateRole(roleCode, body);
    }

    @GetMapping("/user-roles")
    public ApiResponse<Object> listUserRoles(@RequestParam(required = false, name = "q") String q, @RequestParam(required = false) String roleCode, @RequestParam(required = false) String status) {
        return adminService.listUserRoles(q, roleCode, status);
    }

    @PostMapping("/user-roles")
    public ApiResponse<Object> grantUserRole(@RequestBody Map<String, Object> body) {
        return adminService.grantUserRole(body);
    }

    @PatchMapping("/user-roles/{assignmentId}")
    public ApiResponse<Object> updateUserRole(@PathVariable Long assignmentId, @RequestBody Map<String, Object> body) {
        return adminService.updateUserRole(assignmentId, body);
    }

    @DeleteMapping("/user-roles/{assignmentId}")
    public ApiResponse<Object> revokeUserRole(@PathVariable Long assignmentId, @RequestParam(required = false) String reason) {
        return adminService.revokeUserRole(assignmentId, reason);
    }

    @GetMapping("/menu-permissions")
    public ApiResponse<Object> listMenuPermissions(@RequestParam(defaultValue = "ROLE") String targetType, @RequestParam(defaultValue = "R09") String targetId) {
        return adminService.listMenuPermissions(targetType, targetId);
    }

    @PutMapping("/menu-permissions")
    public ApiResponse<Object> saveMenuPermissions(@RequestBody Map<String, Object> body) {
        return adminService.saveMenuPermissions(body);
    }

    @GetMapping("/menus/tree")
    public ApiResponse<Object> getMenuTree() {
        return adminService.getMenuTree();
    }

    @PutMapping("/menus/{menuId}/structure")
    public ApiResponse<Object> updateMenuStructure(@PathVariable Long menuId, @RequestBody Map<String, Object> body) {
        return adminService.updateMenuStructure(menuId, body);
    }

    @PutMapping("/menus/reorder")
    public ApiResponse<Object> reorderMenus(@RequestBody Map<String, Object> body) {
        return adminService.reorderMenus(body);
    }

    @GetMapping("/menus")
    public ApiResponse<Object> listMenus(@RequestParam(required = false, name = "q") String q) {
        return adminService.listMenus(q);
    }

    @PostMapping("/menus")
    public ApiResponse<Object> createMenu(@RequestBody Map<String, Object> body) {
        return adminService.createMenu(body);
    }

    @PutMapping("/menus/{menuId}")
    public ApiResponse<Object> updateMenu(@PathVariable Long menuId, @RequestBody Map<String, Object> body) {
        return adminService.updateMenu(menuId, body);
    }

    @GetMapping("/code-groups")
    public ApiResponse<Object> listCodeGroups(@RequestParam(required = false, name = "q") String q, @RequestParam(required = false) String useYn) {
        return adminService.listCodeGroups(q, useYn);
    }

    @PostMapping("/code-groups")
    public ApiResponse<Object> createCodeGroup(@RequestBody Map<String, Object> body) {
        return adminService.createCodeGroup(body);
    }

    @PutMapping("/code-groups/{groupId}")
    public ApiResponse<Object> updateCodeGroup(@PathVariable String groupId, @RequestBody Map<String, Object> body) {
        return adminService.updateCodeGroup(groupId, body);
    }

    @GetMapping("/code-groups/{groupId}/codes")
    public ApiResponse<Object> listCodeDetails(@PathVariable String groupId, @RequestParam(required = false, name = "q") String q) {
        return adminService.listCodeDetails(groupId, q);
    }

    @PostMapping("/code-groups/{groupId}/codes")
    public ApiResponse<Object> createCodeDetail(@PathVariable String groupId, @RequestBody Map<String, Object> body) {
        return adminService.createCodeDetail(groupId, body);
    }

    @PutMapping("/code-groups/{groupId}/codes/{codeValue}")
    public ApiResponse<Object> updateCodeDetail(@PathVariable String groupId, @PathVariable String codeValue, @RequestBody Map<String, Object> body) {
        return adminService.updateCodeDetail(groupId, codeValue, body);
    }
}
