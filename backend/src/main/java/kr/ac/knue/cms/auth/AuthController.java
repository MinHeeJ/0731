package kr.ac.knue.cms.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.ac.knue.cms.api.ApiResponse;
import kr.ac.knue.cms.api.CurrentUser;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        return authService.health();
    }

    @PostMapping("/auth/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(@CookieValue(name = "KNUESESSION", required = false) String sessionId, HttpServletResponse response) {
        return authService.logout(sessionId, response);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUser> me() {
        return authService.me();
    }

    @GetMapping("/me/menus")
    public ApiResponse<Object> myMenus() {
        return authService.myMenus();
    }

    public record LoginRequest(@NotBlank(message = "사용자 ID는 필수입니다") String userId,
                               @NotBlank(message = "비밀번호는 필수입니다") String password) {}
}
