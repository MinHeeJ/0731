package kr.ac.knue.cms.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.cms.common.ApiError;
import kr.ac.knue.cms.common.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionAuthenticationInterceptor implements HandlerInterceptor {
    private final SessionService sessionService; private final ObjectMapper objectMapper;
    public SessionAuthenticationInterceptor(SessionService sessionService, ObjectMapper objectMapper) { this.sessionService = sessionService; this.objectMapper = objectMapper; }
    @Override public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) return true;
        SessionUser user = sessionService.resolve(cookie(request));
        if (user == null) { write(response, 401, "인증이 필요합니다."); return false; }
        boolean authenticatedOnlyRead = "GET".equals(request.getMethod()) && (path.equals("/api/system-config") || path.startsWith("/api/system-config/"));
        if (!user.hasRole("R09") && !authenticatedOnlyRead && !path.equals("/api/auth/me") && !path.equals("/api/auth/logout")) { write(response, 403, "권한이 없습니다."); return false; }
        SessionContext.set(user); return true;
    }
    @Override public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) { SessionContext.clear(); }
    private String cookie(HttpServletRequest request) { if (request.getCookies() == null) return null; for (var c: request.getCookies()) if ("SESSION".equals(c.getName())) return c.getValue(); return null; }
    private void write(HttpServletResponse response, int status, String message) throws Exception { response.setStatus(status); response.setContentType(MediaType.APPLICATION_JSON_VALUE); objectMapper.writeValue(response.getWriter(), ApiResponse.fail(ApiError.of(message))); }
}
