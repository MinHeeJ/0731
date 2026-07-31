package kr.ac.knue.cms.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.knue.cms.api.ApiException;
import kr.ac.knue.cms.api.ApiResponse;
import kr.ac.knue.cms.api.CurrentUser;
import kr.ac.knue.cms.api.RequestContext;
import kr.ac.knue.cms.persistence.CommonMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final CommonMapper mapper;
    private final AuthenticationPort authenticationPort;

    public AuthService(CommonMapper mapper, AuthenticationPort authenticationPort) {
        this.mapper = mapper;
        this.authenticationPort = authenticationPort;
    }

    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of("status", "UP", "service", "knue-common", "success", true));
    }

    public ApiResponse<Map<String, Object>> login(AuthController.LoginRequest request, HttpServletResponse response) {
        Map<String, Object> user = mapper.findLoginUser(request.userId());
        if (user == null || !authenticationPort.matches(request.userId(), request.password(), String.valueOf(user.get("passwordHash")))) {
            throw new ApiException(401, "INVALID_CREDENTIALS", "사용자 ID 또는 비밀번호가 올바르지 않습니다");
        }
        String sessionId = UUID.randomUUID().toString();
        mapper.insertSession(sessionId, request.userId(), LocalDateTime.now().plusHours(8));
        Cookie cookie = new Cookie("KNUESESSION", sessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(8 * 60 * 60);
        response.addCookie(cookie);
        mapper.insertHistory("session", sessionId, null, "{}", request.userId(), "login");
        return ApiResponse.ok(Map.of("userId", request.userId(), "name", user.get("name"), "roles", mapper.findRoleCodesByUserId(request.userId())));
    }

    public ApiResponse<Void> logout(String sessionId, HttpServletResponse response) {
        CurrentUser actor = RequestContext.require();
        if (sessionId != null) {
            mapper.deleteSession(sessionId);
        }
        Cookie cookie = new Cookie("KNUESESSION", "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        mapper.insertHistory("session", actor.userId(), "{}", null, actor.userId(), "logout");
        return ApiResponse.ok();
    }

    public ApiResponse<CurrentUser> me() {
        return ApiResponse.ok(RequestContext.require());
    }

    public ApiResponse<Object> myMenus() {
        CurrentUser user = RequestContext.require();
        return ApiResponse.ok(mapper.getMyMenus(user.userId()));
    }
}
