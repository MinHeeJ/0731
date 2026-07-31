package kr.ac.knue.performance.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import kr.ac.knue.performance.auth.AuthMapper;
import kr.ac.knue.performance.auth.CurrentUser;
import kr.ac.knue.performance.auth.Hashing;
import kr.ac.knue.performance.common.web.ApiError;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {
  public static final String CURRENT_USER = "currentUser";
  private final AuthMapper authMapper;
  private final ObjectMapper objectMapper;
  public SessionAuthenticationFilter(AuthMapper authMapper, ObjectMapper objectMapper) {
    this.authMapper = authMapper;
    this.objectMapper = objectMapper;
  }
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui");
  }
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String token = sessionToken(request);
    if (token == null || token.isBlank()) {
      writeError(response, request, 401, "UNAUTHENTICATED", "인증 세션이 필요합니다.");
      return;
    }
    CurrentUser user = authMapper.findCurrentUserByTokenHash(Hashing.sha256(token));
    if (user == null || user.roleCodes() == null || !user.roleCodes().contains("R09")) {
      writeError(response, request, user == null ? 401 : 403, user == null ? "UNAUTHENTICATED" : "FORBIDDEN", user == null ? "인증 세션이 유효하지 않습니다." : "접근 권한이 없습니다.");
      return;
    }
    request.setAttribute(CURRENT_USER, user);
    chain.doFilter(request, response);
  }
  private String sessionToken(HttpServletRequest request) {
    String header = request.getHeader("Cookie");
    if (header != null) {
      for (String part : header.split(";")) {
        String value = part.trim();
        if (value.startsWith("SESSION=")) return value.substring("SESSION=".length());
      }
    }
    return Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
        .filter(cookie -> "SESSION".equals(cookie.getName())).map(Cookie::getValue).findFirst().orElse(null);
  }
  private void writeError(HttpServletResponse response, HttpServletRequest request, int status, String code, String message) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ApiError.of(code, message), String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE))));
  }
}
