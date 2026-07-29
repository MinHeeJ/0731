package kr.ac.knue.common.auth;

import kr.ac.knue.common.api.GlobalExceptionHandler.AppException;
import kr.ac.knue.common.admin.AdminMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class AuthService {
  private final AdminMapper mapper;
  public AuthService(AdminMapper mapper) { this.mapper = mapper; }
  public record LoginUser(String userId, List<String> roleCodes) {}
  public record LoginResult(String sessionId, LoginUser user) {}
  @Transactional
  public LoginResult login(String userId, String password) {
    Map<String,Object> account = mapper.findAccount(userId);
    if (account == null || !Objects.equals(account.get("passwordHash"), password)) throw new AppException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다.");
    if (!"ACTIVE".equals(account.get("status")) || !"Y".equals(account.get("systemUseYn"))) throw new AppException(HttpStatus.UNAUTHORIZED, "ACCOUNT_DISABLED", "사용할 수 없는 계정입니다.");
    String sid = UUID.randomUUID().toString();
    mapper.createSession(sid, userId, OffsetDateTime.now().plusHours(1));
    return new LoginResult(sid, new LoginUser(userId, mapper.findActiveRoleCodes(userId)));
  }
  @Transactional
  public void logout(String sessionId) { LoginUser user = requireUser(sessionId); mapper.updateSessionStatus(sessionId, "LOGGED_OUT"); mapper.insertHistory("session", sessionId, null, Map.of("status","LOGGED_OUT"), user.userId(), "로그아웃"); }
  public LoginUser requireUser(String sessionId) {
    if (sessionId == null || sessionId.isBlank()) throw new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다.");
    Map<String,Object> row = mapper.findActiveSession(sessionId);
    if (row == null) throw new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "유효하지 않은 세션입니다.");
    String userId = String.valueOf(row.get("userId"));
    return new LoginUser(userId, mapper.findActiveRoleCodes(userId));
  }
  public LoginUser requireAdmin(String sessionId) {
    LoginUser user = requireUser(sessionId);
    if (!user.roleCodes().contains("R09")) throw new AppException(HttpStatus.FORBIDDEN, "FORBIDDEN", "시스템관리자 권한이 필요합니다.");
    return user;
  }
}
