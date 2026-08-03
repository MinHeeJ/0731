package kr.ac.knue.cms.auth;

import kr.ac.knue.cms.common.BusinessException;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Component
public class LocalAuthenticationAdapter implements AuthenticationPort {
    private final AuthMapper mapper;
    public LocalAuthenticationAdapter(AuthMapper mapper) { this.mapper = mapper; }
    @Override
    public SessionUser authenticate(String loginId, String password) {
        Map<String, Object> user = mapper.findByLoginId(loginId);
        if (user == null || !hash(password).equals(user.get("passwordHash"))) throw new BusinessException(403, "아이디 또는 비밀번호가 올바르지 않습니다.");
        if (!"Y".equals(user.get("useYn")) || !"ACTIVE".equals(user.get("status"))) throw new BusinessException(403, "비활성 계정은 로그인할 수 없습니다.");
        String userId = (String) user.get("userId");
        return new SessionUser(null, userId, (String) user.get("loginId"), (String) user.get("displayName"), mapper.findRoleCodesByUserId(userId));
    }
    public static String hash(String raw) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
