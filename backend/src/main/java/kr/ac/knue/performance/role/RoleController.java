package kr.ac.knue.performance.role;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
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
public class RoleController {
  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  @GetMapping("/api/roles")
  ApiResponse<List<RoleDto>> list(@RequestParam(required = false) String roleCode, @RequestParam(required = false) String roleName, HttpServletRequest request) {
    return ApiResponse.ok(roleService.list(roleCode, roleName), requestId(request));
  }

  @PostMapping("/api/roles")
  ApiResponse<RoleDto> create(@Valid @RequestBody RoleRequest body, HttpServletRequest request) {
    return ApiResponse.ok(roleService.create(body), requestId(request));
  }

  @PutMapping("/api/roles/{roleCode}")
  ApiResponse<RoleDto> update(@PathVariable String roleCode, @Valid @RequestBody RoleRequest body, HttpServletRequest request) {
    return ApiResponse.ok(roleService.update(roleCode, body), requestId(request));
  }

  private String requestId(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE));
  }
}
