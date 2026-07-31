package kr.ac.knue.performance.userrole;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.performance.auth.CurrentUser;
import kr.ac.knue.performance.common.security.SessionAuthenticationFilter;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserRoleController {
  private final UserRoleService userRoleService;

  public UserRoleController(UserRoleService userRoleService) {
    this.userRoleService = userRoleService;
  }

  @GetMapping("/api/user-roles")
  ApiResponse<List<UserRoleAssignmentDto>> list(@RequestParam(required = false) UUID userId, @RequestParam(required = false) String roleCode, @RequestParam(required = false) String status, HttpServletRequest request) {
    return ApiResponse.ok(userRoleService.list(userId, roleCode, status), requestId(request));
  }

  @PostMapping("/api/user-roles")
  ApiResponse<UserRoleAssignmentDto> grant(@Valid @RequestBody CreateUserRoleRequest body, HttpServletRequest request) {
    return ApiResponse.ok(userRoleService.grant(body, currentUser(request)), requestId(request));
  }

  @DeleteMapping("/api/user-roles/{assignmentId}")
  ApiResponse<UserRoleAssignmentDto> revoke(@PathVariable UUID assignmentId, HttpServletRequest request) {
    return ApiResponse.ok(userRoleService.revoke(assignmentId, currentUser(request)), requestId(request));
  }

  private CurrentUser currentUser(HttpServletRequest request) {
    return (CurrentUser) request.getAttribute(SessionAuthenticationFilter.CURRENT_USER);
  }

  private String requestId(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE));
  }
}
