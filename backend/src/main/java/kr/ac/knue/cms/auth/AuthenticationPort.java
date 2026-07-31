package kr.ac.knue.cms.auth;

public interface AuthenticationPort {
    boolean matches(String userId, String rawPassword, String storedPasswordHash);
}
