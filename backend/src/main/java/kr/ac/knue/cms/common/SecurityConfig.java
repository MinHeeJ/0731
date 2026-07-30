package kr.ac.knue.cms.common;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {
  private final SessionMapper sessionMapper;

  public SecurityConfig(SessionMapper sessionMapper) { this.sessionMapper = sessionMapper; }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new AuthInterceptor(sessionMapper)).addPathPatterns("/api/**")
      .excludePathPatterns("/api/health", "/api/auth/login", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html");
  }

  static class AuthInterceptor implements HandlerInterceptor {
    private final SessionMapper sessionMapper;
    AuthInterceptor(SessionMapper sessionMapper) { this.sessionMapper = sessionMapper; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
      String sessionId = null;
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if ("KNUESESSION".equals(cookie.getName())) sessionId = cookie.getValue();
        }
      }
      if (sessionId == null) {
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"UNAUTHENTICATED\",\"message\":\"인증이 필요합니다\",\"errors\":[]}}");
        return false;
      }
      SessionUser user = sessionMapper.findSessionUser(sessionId);
      if (user == null) {
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"UNAUTHENTICATED\",\"message\":\"세션이 만료되었습니다\",\"errors\":[]}}");
        return false;
      }
      request.setAttribute("sessionUser", user);
      if (request.getRequestURI().startsWith("/api/admin/") && !user.isAdmin()) {
        response.setStatus(403);
        response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"R09 시스템관리자 권한이 필요합니다\",\"errors\":[]}}");
        return false;
      }
      return true;
    }
  }
}
