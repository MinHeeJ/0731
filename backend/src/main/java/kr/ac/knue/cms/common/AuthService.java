package kr.ac.knue.cms.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {
  private final SessionMapper mapper;
  private final AdminMapper adminMapper;

  public AuthService(SessionMapper mapper, AdminMapper adminMapper) { this.mapper = mapper; this.adminMapper = adminMapper; }

  public ApiResponse<Map<String, Object>> health() { return ApiResponse.ok(Map.of("status", "UP", "service", "cms-common-foundation")); }

  public ResponseEntity<ApiResponse<Map<String, Object>>> login(LoginRequest request, HttpServletResponse response) {
    if (request.userId() == null || request.userId().isBlank()) return ResponseEntity.badRequest().body(ApiResponse.fieldError("VALIDATION_ERROR", "입력값을 확인하세요", "userId", "사용자 ID는 필수입니다"));
    if (request.password() == null || request.password().isBlank()) return ResponseEntity.badRequest().body(ApiResponse.fieldError("VALIDATION_ERROR", "입력값을 확인하세요", "password", "비밀번호는 필수입니다"));
    String hash = mapper.findPasswordHash(request.userId());
    if (hash == null || !hash.equals(sha256(request.password()))) {
      return ResponseEntity.status(401).body(ApiResponse.error("INVALID_CREDENTIALS", "사용자 ID 또는 비밀번호가 올바르지 않습니다"));
    }
    String sessionId = UUID.randomUUID().toString();
    mapper.insertSession(sessionId, request.userId(), LocalDateTime.now().plusHours(8));
    mapper.insertHistory("session", sessionId, "{}", "{\"operationId\":\"login\"}", request.userId(), "login");
    Cookie cookie = new Cookie("KNUESESSION", sessionId);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(8 * 60 * 60);
    response.addCookie(cookie);
    return ResponseEntity.ok(ApiResponse.ok(adminMapper.currentUser(request.userId())));
  }

  public ApiResponse<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
    SessionUser user = (SessionUser) request.getAttribute("sessionUser");
    if (user != null) {
      mapper.deleteSession(user.sessionId());
      mapper.insertHistory("session", user.sessionId(), "{\"operationId\":\"logout\"}", "{}", user.userId(), "logout");
    }
    Cookie cookie = new Cookie("KNUESESSION", "");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    return ApiResponse.ok(Map.of("status", "LOGGED_OUT"));
  }

  public ApiResponse<Map<String, Object>> me(HttpServletRequest request) {
    SessionUser user = (SessionUser) request.getAttribute("sessionUser");
    return ApiResponse.ok(adminMapper.currentUser(user.userId()));
  }

  public ApiResponse<List<Map<String, Object>>> myMenus(HttpServletRequest request) {
    SessionUser user = (SessionUser) request.getAttribute("sessionUser");
    return ApiResponse.ok(adminMapper.myMenus(user.userId()));
  }

  static String sha256(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("hash failure", e);
    }
  }
  public record LoginRequest(String userId, String password) {}
}
