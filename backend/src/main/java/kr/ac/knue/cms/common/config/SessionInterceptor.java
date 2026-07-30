package kr.ac.knue.cms.common.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.cms.common.service.SessionService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
public class SessionInterceptor implements HandlerInterceptor {
    private final SessionService sessionService;

    public SessionInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.equals("/swagger-ui.html")) {
            return true;
        }
        String sessionId = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
            .filter(cookie -> "SESSION".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
        return sessionService.findSession(sessionId)
            .map(user -> {
                if (path.startsWith("/api/admin") && !user.roleCodes().contains("R09")) {
                    return forbidden(response);
                }
                request.setAttribute("sessionUser", user);
                return true;
            })
            .orElseGet(() -> unauthorized(response));
    }

    private boolean forbidden(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"R09 시스템관리자 권한이 필요합니다.\",\"fieldErrors\":[]}}");
        } catch (Exception ignored) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        return false;
    }

    private boolean unauthorized(HttpServletResponse response) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"success\":false,\"data\":null,\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"인증 세션이 필요합니다.\",\"fieldErrors\":[]}}");
        } catch (Exception ignored) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return false;
    }
}
