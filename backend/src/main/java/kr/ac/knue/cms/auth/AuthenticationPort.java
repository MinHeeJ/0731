package kr.ac.knue.cms.auth;

public interface AuthenticationPort {
    SessionUser authenticate(String loginId, String password);
}
