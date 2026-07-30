package kr.ac.knue.cms.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) { this.service = service; }

  @GetMapping("/health")
  public ApiResponse<Map<String, Object>> health() { return service.health(); }

  @PostMapping("/auth/login")
  public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody AuthService.LoginRequest request, HttpServletResponse response) {
    return service.login(request, response);
  }

  @PostMapping("/auth/logout")
  public ApiResponse<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
    return service.logout(request, response);
  }

  @GetMapping("/me")
  public ApiResponse<Map<String, Object>> me(HttpServletRequest request) { return service.me(request); }

  @GetMapping("/me/menus")
  public ApiResponse<List<Map<String, Object>>> myMenus(HttpServletRequest request) { return service.myMenus(request); }
}
