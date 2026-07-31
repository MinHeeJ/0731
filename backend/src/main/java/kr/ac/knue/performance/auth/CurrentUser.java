package kr.ac.knue.performance.auth;

import java.util.List;
import java.util.UUID;

public record CurrentUser(UUID userId, String loginId, List<String> roleCodes) {}
