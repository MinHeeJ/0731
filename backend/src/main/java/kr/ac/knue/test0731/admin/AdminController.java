package kr.ac.knue.test0731.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.ok(Map.of("status", "UP", "service", "test0731-system-admin"));
    }

    @GetMapping("/admin/users")
    public ApiResponse<?> listUsers(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("users", keyword, page, size);
    }

    @GetMapping("/admin/users/{id}")
    public ApiResponse<?> getUsers(@PathVariable UUID id) {
        return getResource("users", id);
    }

    @PostMapping("/admin/users")
    public ResponseEntity<ApiResponse<?>> createUsers(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("users", role, item, bindingResult);
    }

    @PutMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<?>> updateUsers(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("users", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUsers(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("users", id, role, reason);
    }

    @GetMapping("/admin/organizations")
    public ApiResponse<?> listOrganizations(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("organizations", keyword, page, size);
    }

    @GetMapping("/admin/organizations/{id}")
    public ApiResponse<?> getOrganizations(@PathVariable UUID id) {
        return getResource("organizations", id);
    }

    @PostMapping("/admin/organizations")
    public ResponseEntity<ApiResponse<?>> createOrganizations(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("organizations", role, item, bindingResult);
    }

    @PutMapping("/admin/organizations/{id}")
    public ResponseEntity<ApiResponse<?>> updateOrganizations(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("organizations", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/organizations/{id}")
    public ResponseEntity<ApiResponse<?>> deleteOrganizations(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("organizations", id, role, reason);
    }

    @GetMapping("/admin/roles")
    public ApiResponse<?> listRoles(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("roles", keyword, page, size);
    }

    @GetMapping("/admin/roles/{id}")
    public ApiResponse<?> getRoles(@PathVariable UUID id) {
        return getResource("roles", id);
    }

    @PostMapping("/admin/roles")
    public ResponseEntity<ApiResponse<?>> createRoles(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("roles", role, item, bindingResult);
    }

    @PutMapping("/admin/roles/{id}")
    public ResponseEntity<ApiResponse<?>> updateRoles(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("roles", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/roles/{id}")
    public ResponseEntity<ApiResponse<?>> deleteRoles(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("roles", id, role, reason);
    }

    @GetMapping("/admin/user-roles")
    public ApiResponse<?> listUserRoles(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("user-roles", keyword, page, size);
    }

    @GetMapping("/admin/user-roles/{id}")
    public ApiResponse<?> getUserRoles(@PathVariable UUID id) {
        return getResource("user-roles", id);
    }

    @PostMapping("/admin/user-roles")
    public ResponseEntity<ApiResponse<?>> createUserRoles(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("user-roles", role, item, bindingResult);
    }

    @PutMapping("/admin/user-roles/{id}")
    public ResponseEntity<ApiResponse<?>> updateUserRoles(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("user-roles", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/user-roles/{id}")
    public ResponseEntity<ApiResponse<?>> deleteUserRoles(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("user-roles", id, role, reason);
    }

    @GetMapping("/admin/menu-permissions")
    public ApiResponse<?> listMenuPermissions(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("menu-permissions", keyword, page, size);
    }

    @GetMapping("/admin/menu-permissions/{id}")
    public ApiResponse<?> getMenuPermissions(@PathVariable UUID id) {
        return getResource("menu-permissions", id);
    }

    @PostMapping("/admin/menu-permissions")
    public ResponseEntity<ApiResponse<?>> createMenuPermissions(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("menu-permissions", role, item, bindingResult);
    }

    @PutMapping("/admin/menu-permissions/{id}")
    public ResponseEntity<ApiResponse<?>> updateMenuPermissions(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("menu-permissions", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/menu-permissions/{id}")
    public ResponseEntity<ApiResponse<?>> deleteMenuPermissions(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("menu-permissions", id, role, reason);
    }

    @GetMapping("/admin/menu-structures")
    public ApiResponse<?> listMenuStructures(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("menu-structures", keyword, page, size);
    }

    @GetMapping("/admin/menu-structures/{id}")
    public ApiResponse<?> getMenuStructures(@PathVariable UUID id) {
        return getResource("menu-structures", id);
    }

    @PostMapping("/admin/menu-structures")
    public ResponseEntity<ApiResponse<?>> createMenuStructures(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("menu-structures", role, item, bindingResult);
    }

    @PutMapping("/admin/menu-structures/{id}")
    public ResponseEntity<ApiResponse<?>> updateMenuStructures(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("menu-structures", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/menu-structures/{id}")
    public ResponseEntity<ApiResponse<?>> deleteMenuStructures(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("menu-structures", id, role, reason);
    }

    @GetMapping("/admin/menus")
    public ApiResponse<?> listMenus(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("menus", keyword, page, size);
    }

    @GetMapping("/admin/menus/{id}")
    public ApiResponse<?> getMenus(@PathVariable UUID id) {
        return getResource("menus", id);
    }

    @PostMapping("/admin/menus")
    public ResponseEntity<ApiResponse<?>> createMenus(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("menus", role, item, bindingResult);
    }

    @PutMapping("/admin/menus/{id}")
    public ResponseEntity<ApiResponse<?>> updateMenus(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("menus", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/menus/{id}")
    public ResponseEntity<ApiResponse<?>> deleteMenus(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("menus", id, role, reason);
    }

    @GetMapping("/admin/code-groups")
    public ApiResponse<?> listCodeGroups(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("code-groups", keyword, page, size);
    }

    @GetMapping("/admin/code-groups/{id}")
    public ApiResponse<?> getCodeGroups(@PathVariable UUID id) {
        return getResource("code-groups", id);
    }

    @PostMapping("/admin/code-groups")
    public ResponseEntity<ApiResponse<?>> createCodeGroups(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("code-groups", role, item, bindingResult);
    }

    @PutMapping("/admin/code-groups/{id}")
    public ResponseEntity<ApiResponse<?>> updateCodeGroups(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("code-groups", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/code-groups/{id}")
    public ResponseEntity<ApiResponse<?>> deleteCodeGroups(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("code-groups", id, role, reason);
    }

    @GetMapping("/admin/detail-codes")
    public ApiResponse<?> listDetailCodes(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return listResource("detail-codes", keyword, page, size);
    }

    @GetMapping("/admin/detail-codes/{id}")
    public ApiResponse<?> getDetailCodes(@PathVariable UUID id) {
        return getResource("detail-codes", id);
    }

    @PostMapping("/admin/detail-codes")
    public ResponseEntity<ApiResponse<?>> createDetailCodes(@RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return createResource("detail-codes", role, item, bindingResult);
    }

    @PutMapping("/admin/detail-codes/{id}")
    public ResponseEntity<ApiResponse<?>> updateDetailCodes(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @Valid @RequestBody AdminItem item,
                                                        BindingResult bindingResult) {
        return updateResource("detail-codes", id, role, item, bindingResult);
    }

    @DeleteMapping("/admin/detail-codes/{id}")
    public ResponseEntity<ApiResponse<?>> deleteDetailCodes(@PathVariable UUID id,
                                                        @RequestHeader(value = "X-Role", defaultValue = "SYSTEM_ADMIN") String role,
                                                        @RequestParam(required = false) String reason) {
        return deleteResource("detail-codes", id, role, reason);
    }

    private ApiResponse<?> listResource(String resource, String keyword, int page, int size) {
        validatePage(page, size);
        return ApiResponse.ok(service.list(resource, keyword, page, size));
    }

    private ApiResponse<?> getResource(String resource, UUID id) {
        return ApiResponse.ok(service.get(resource, id));
    }

    private ResponseEntity<ApiResponse<?>> createResource(String resource, String role, AdminItem item, BindingResult bindingResult) {
        ResponseEntity<ApiResponse<?>> blocked = validateWrite(role, bindingResult);
        if (blocked != null) {
            return blocked;
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.create(resource, item)));
    }

    private ResponseEntity<ApiResponse<?>> updateResource(String resource, UUID id, String role, AdminItem item, BindingResult bindingResult) {
        ResponseEntity<ApiResponse<?>> blocked = validateWrite(role, bindingResult);
        if (blocked != null) {
            return blocked;
        }
        return ResponseEntity.ok(ApiResponse.ok(service.update(resource, id, item)));
    }

    private ResponseEntity<ApiResponse<?>> deleteResource(String resource, UUID id, String role, String reason) {
        if (!"SYSTEM_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("FORBIDDEN", "권한이 없습니다"));
        }
        service.delete(resource, id, reason);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("deleted", true)));
    }

    private ResponseEntity<ApiResponse<?>> validateWrite(String role, BindingResult bindingResult) {
        if (!"SYSTEM_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("FORBIDDEN", "권한이 없습니다"));
        }
        if (bindingResult.hasFieldErrors()) {
            var fieldError = bindingResult.getFieldErrors().get(0);
            return ResponseEntity.badRequest().body(ApiResponse.field("VALIDATION_ERROR", "입력값을 확인하세요", fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return null;
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page/size 범위가 올바르지 않습니다");
        }
    }
}
