package kr.ac.knue.cms.common.service;

import jakarta.servlet.http.Cookie;
import kr.ac.knue.cms.common.domain.LoginRequest;
import kr.ac.knue.cms.common.domain.SessionUser;
import kr.ac.knue.cms.common.mapper.CommonMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final CommonMapper mapper;

    public SessionService(CommonMapper mapper) {
        this.mapper = mapper;
    }

    @Transactional
    public Map<String, Object> login(LoginRequest request) {
        Map<String, Object> user = mapper.findUserByLoginId(request.loginId());
        if (user == null || !String.valueOf(user.get("passwordHash")).equals("{noop}" + request.password())) {
            throw new IllegalArgumentException("계정 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!"Y".equals(user.get("systemUseYn"))) {
            throw new IllegalArgumentException("사용 중지된 계정입니다.");
        }
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(8);
        mapper.insertSession(sessionId, String.valueOf(user.get("userId")), expiresAt);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", sessionId);
        data.put("user", Map.of("userId", user.get("userId"), "loginId", user.get("loginId"), "userName", user.get("userName"), "roleCodes", mapper.findActiveRoleCodes(String.valueOf(user.get("userId")))));
        data.put("expiresAt", expiresAt);
        return data;
    }

    public Optional<SessionUser> findSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }
        Map<String, Object> row = mapper.findSession(sessionId);
        if (row == null) {
            return Optional.empty();
        }
        String userId = String.valueOf(row.get("userId"));
        return Optional.of(new SessionUser(
            String.valueOf(row.get("sessionId")),
            userId,
            String.valueOf(row.get("loginId")),
            String.valueOf(row.get("userName")),
            mapper.findActiveRoleCodes(userId),
            row.get("expiresAt") instanceof LocalDateTime time ? time : LocalDateTime.now().plusHours(1)
        ));
    }

    public void logout(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            mapper.deleteSession(sessionId);
        }
    }
}
