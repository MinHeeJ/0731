package kr.ac.knue.cms.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {
    private final AuthenticationPort authenticationPort;
    private final SessionService sessionService;
    private final String cookieName;

    public AuthController(AuthenticationPort authenticationPort, SessionService sessionService, @Value("${cms.auth.session-cookie-name:SESSION}") String cookieName) {
        this.authenticationPort = authenticationPort;
        this.sessionService = sessionService;
        this.cookieName = cookieName;
    }

    public record LoginRequest(@NotBlank(message = "로그인 ID는 필수입니다.") String loginId, @NotBlank(message = "비밀번호는 필수입니다.") String password) {}

    @PostMapping("/api/auth/login")
    public ApiResponse<SessionUser> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        SessionUser authenticated = authenticationPort.authenticate(request.loginId(), request.password());
        String sessionId = sessionService.create(authenticated.userId());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieName + "=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax");
        return ApiResponse.ok(new SessionUser(sessionId, authenticated.userId(), authenticated.loginId(), authenticated.displayName(), authenticated.roles()));
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<Map<String, String>> logout(@CookieValue(name = "SESSION", required = false) String sessionId, HttpServletResponse response) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new BusinessException(400, "로그아웃할 세션이 없습니다.", Map.of("SESSION", "필수 쿠키입니다."));
        }
        sessionService.revoke(sessionId);
        response.addHeader(HttpHeaders.SET_COOKIE, cookieName + "=; Max-Age=0; Path=/; HttpOnly; SameSite=Lax");
        return ApiResponse.ok(Map.of("status", "REVOKED"));
    }

    @GetMapping("/api/auth/me")
    public ApiResponse<SessionUser> me() {
        return ApiResponse.ok(SessionContext.get());
    }
}
