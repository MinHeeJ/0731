package kr.ac.knue.performance.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.performance.auth.CurrentUser;
import kr.ac.knue.performance.common.security.SessionAuthenticationFilter;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping("/api/users")
  ApiResponse<List<UserDto>> list(@RequestParam(required = false) String staffNo, @RequestParam(required = false) String staffName, @RequestParam(required = false) String organizationCode, @RequestParam(required = false) String jobGrade, @RequestParam(required = false) String employmentStatus, @RequestParam(required = false) String roleCode, @RequestParam(required = false) Boolean systemUseEnabled, HttpServletRequest request) {
    return ApiResponse.ok(service.list(staffNo, staffName, organizationCode, jobGrade, employmentStatus, roleCode, systemUseEnabled), requestId(request));
  }

  @PatchMapping("/api/users/{userId}/usage")
  ApiResponse<UserDto> usage(@PathVariable UUID userId, @Valid @RequestBody UserRequests.Usage body, HttpServletRequest request) {
    return ApiResponse.ok(service.updateUsage(userId, body), requestId(request));
  }

  @PutMapping("/api/users/{userId}/roles")
  ApiResponse<UserDto> roles(@PathVariable UUID userId, @Valid @RequestBody UserRequests.Roles body, HttpServletRequest request) {
    return ApiResponse.ok(service.replaceRoles(userId, body, (CurrentUser) request.getAttribute(SessionAuthenticationFilter.CURRENT_USER)), requestId(request));
  }

  private String requestId(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE));
  }
}
