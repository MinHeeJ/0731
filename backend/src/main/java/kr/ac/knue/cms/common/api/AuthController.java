package kr.ac.knue.cms.common.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.knue.cms.common.domain.ApiResponse;
import kr.ac.knue.cms.common.domain.LoginRequest;
import kr.ac.knue.cms.common.domain.SessionUser;
import kr.ac.knue.cms.common.service.SessionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final SessionService sessionService;

    public AuthController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> data = sessionService.login(request);
        ResponseCookie cookie = ResponseCookie.from("SESSION", String.valueOf(data.get("sessionId")))
            .httpOnly(true).path("/").maxAge(Duration.ofHours(8)).sameSite("Lax").build();
        return ResponseEntity.status(201).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponse.ok(data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logout(HttpServletRequest request, HttpServletResponse response) {
        sessionService.logout(readSessionCookie(request));
        ResponseCookie cookie = ResponseCookie.from("SESSION", "").httpOnly(true).path("/").maxAge(Duration.ZERO).sameSite("Lax").build();
        return ResponseEntity.status(201).header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponse.ok(Map.of("loggedOut", true)));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me(HttpServletRequest request) {
        SessionUser user = (SessionUser) request.getAttribute("sessionUser");
        return ApiResponse.ok(Map.of("userId", user.userId(), "loginId", user.loginId(), "userName", user.userName(), "roleCodes", user.roleCodes(), "expiresAt", user.expiresAt()));
    }

    private String readSessionCookie(HttpServletRequest request) {
        return Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
            .filter(cookie -> "SESSION".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst().orElse(null);
    }
}
