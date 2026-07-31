package kr.ac.knue.performance.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import kr.ac.knue.performance.common.security.SessionAuthenticationFilter;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/api/auth/login")
  ApiResponse<CurrentUser> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest servletRequest, HttpServletResponse response) {
    return ApiResponse.ok(authService.login(loginRequest, response), requestId(servletRequest));
  }

  @PostMapping("/api/auth/logout")
  ApiResponse<CurrentUser> logout(HttpServletRequest request, HttpServletResponse response) {
    CurrentUser currentUser = (CurrentUser) request.getAttribute(SessionAuthenticationFilter.CURRENT_USER);
    String token = sessionToken(request);
    return ApiResponse.ok(authService.logout(token, currentUser, response), requestId(request));
  }

  @GetMapping("/api/auth/me")
  ApiResponse<CurrentUser> me(HttpServletRequest request) {
    return ApiResponse.ok((CurrentUser) request.getAttribute(SessionAuthenticationFilter.CURRENT_USER), requestId(request));
  }

  private String requestId(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE));
  }

  private String sessionToken(HttpServletRequest request) {
    String cookieHeader = request.getHeader("Cookie");
    if (cookieHeader != null) {
      for (String part : cookieHeader.split(";")) {
        String value = part.trim();
        if (value.startsWith("SESSION=")) {
          return value.substring("SESSION=".length());
        }
      }
    }
    return Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
        .filter(cookie -> "SESSION".equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
