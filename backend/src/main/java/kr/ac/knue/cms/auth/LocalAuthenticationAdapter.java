package kr.ac.knue.cms.auth;

import org.springframework.stereotype.Component;

@Component
public class LocalAuthenticationAdapter implements AuthenticationPort {
    @Override
    public boolean matches(String userId, String rawPassword, String storedPasswordHash) {
        return storedPasswordHash != null && storedPasswordHash.equals("{plain}" + rawPassword);
    }
}
