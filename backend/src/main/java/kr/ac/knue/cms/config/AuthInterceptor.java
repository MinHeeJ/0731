package kr.ac.knue.cms.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.cms.api.ApiException;
import kr.ac.knue.cms.api.CurrentUser;
import kr.ac.knue.cms.api.RequestContext;
import kr.ac.knue.cms.persistence.CommonMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final CommonMapper mapper;

    public AuthInterceptor(CommonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.equals("/api/health") || path.equals("/api/auth/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return true;
        }
        String sessionId = cookie(request, "KNUESESSION");
        if (sessionId == null || sessionId.isBlank()) {
            throw new ApiException(401, "UNAUTHORIZED", "인증이 필요합니다");
        }
        Map<String, Object> user = mapper.findUserBySession(sessionId);
        if (user == null) {
            throw new ApiException(401, "UNAUTHORIZED", "세션이 만료되었습니다");
        }
        String userId = String.valueOf(user.get("userId"));
        List<String> roles = mapper.findRoleCodesByUserId(userId);
        String name = user.get("name") == null ? userId : String.valueOf(user.get("name"));
        CurrentUser currentUser = new CurrentUser(userId, name, roles);
        RequestContext.set(currentUser);
        if (path.startsWith("/api/admin") && !currentUser.isAdmin()) {
            throw new ApiException(403, "FORBIDDEN", "R09 시스템관리자 권한이 필요합니다");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestContext.clear();
    }

    private String cookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies).filter(c -> c.getName().equals(name)).map(Cookie::getValue).findFirst().orElse(null);
    }
}
