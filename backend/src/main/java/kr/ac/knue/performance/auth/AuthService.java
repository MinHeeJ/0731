package kr.ac.knue.performance.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import java.util.UUID;
import kr.ac.knue.performance.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final AuthMapper authMapper;
  public AuthService(AuthMapper authMapper) { this.authMapper = authMapper; }
  @Transactional
  public CurrentUser login(LoginRequest request, HttpServletResponse response) {
    AuthMapper.AuthUser user = authMapper.findLoginUser(request.loginId());
    if (user == null || !Hashing.sha256(request.password()).equals(user.passwordHash()) || !user.systemUseEnabled() || !"ACTIVE".equals(user.status())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "로그인 정보가 올바르지 않습니다.");
    }
    String token = UUID.randomUUID().toString() + UUID.randomUUID();
    authMapper.insertSession(user.userId(), Hashing.sha256(token), OffsetDateTime.now().plusHours(8));
    Cookie cookie = new Cookie("SESSION", token);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(8 * 60 * 60);
    response.addHeader("Set-Cookie", "SESSION=" + token + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + (8 * 60 * 60));
    return authMapper.findCurrentUserById(user.userId());
  }
  @Transactional
  public CurrentUser logout(String token, CurrentUser currentUser, HttpServletResponse response) {
    if (token != null) authMapper.logout(Hashing.sha256(token));
    response.addHeader("Set-Cookie", "SESSION=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0");
    return currentUser;
  }
}
