package kr.ac.knue.cms.authorization;
import org.springframework.stereotype.Service;
@Service public class AuthorizationService { public boolean canAccess(String userId, String path) { return true; } }
