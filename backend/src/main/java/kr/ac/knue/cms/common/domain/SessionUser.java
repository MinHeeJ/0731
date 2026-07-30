package kr.ac.knue.cms.common.domain;

import java.time.LocalDateTime;
import java.util.List;

public record SessionUser(String sessionId, String userId, String loginId, String userName, List<String> roleCodes, LocalDateTime expiresAt) {}
