package kr.ac.knue.cms.auth;

import java.util.List;

public record SessionUser(String sessionId, String userId, String loginId, String displayName, List<String> roles) {
    public boolean hasRole(String roleCode) { return roles != null && roles.contains(roleCode); }
}
