package kr.ac.knue.cms.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
public class SessionService {
    private final AuthMapper mapper; private final int sessionHours; private final SecureRandom random = new SecureRandom();
    public SessionService(AuthMapper mapper, @Value("${cms.auth.session-hours:8}") int sessionHours) { this.mapper = mapper; this.sessionHours = sessionHours; }
    public String create(String userId) { byte[] b = new byte[48]; random.nextBytes(b); String id = Base64.getUrlEncoder().withoutPadding().encodeToString(b); mapper.insertSession(id, userId, sessionHours); return id; }
    public SessionUser resolve(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) return null;
        Map<String, Object> row = mapper.findSession(sessionId);
        if (row == null) return null;
        String userId = (String) row.get("userId");
        return new SessionUser(sessionId, userId, (String) row.get("loginId"), (String) row.get("displayName"), mapper.findRoleCodesByUserId(userId));
    }
    public void revoke(String sessionId) { if (sessionId != null) mapper.revokeSession(sessionId); }
}
