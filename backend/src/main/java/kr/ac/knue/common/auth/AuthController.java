package kr.ac.knue.common.auth;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kr.ac.knue.common.api.ApiResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  public AuthController(AuthService authService) { this.authService = authService; }
  public record LoginRequest(@NotBlank String userId, @NotBlank String password) {}
  @PostMapping("/login") ApiResponse<AuthService.LoginUser> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
    AuthService.LoginResult result = authService.login(request.userId(), request.password());
    response.addHeader("Set-Cookie", ResponseCookie.from("KNUESESSION", result.sessionId()).httpOnly(true).sameSite("Lax").path("/").maxAge(3600).build().toString());
    return ApiResponse.ok(result.user());
  }
  @PostMapping("/logout") ApiResponse<Map<String,String>> logout(@CookieValue(name="KNUESESSION", required=false) String sessionId, HttpServletResponse response) {
    authService.logout(sessionId);
    response.addHeader("Set-Cookie", ResponseCookie.from("KNUESESSION", "").httpOnly(true).sameSite("Lax").path("/").maxAge(0).build().toString());
    return ApiResponse.ok(Map.of("status", "LOGGED_OUT"));
  }
  @GetMapping("/me") ApiResponse<AuthService.LoginUser> me(@CookieValue(name="KNUESESSION", required=false) String sessionId) { return ApiResponse.ok(authService.requireUser(sessionId)); }
}
