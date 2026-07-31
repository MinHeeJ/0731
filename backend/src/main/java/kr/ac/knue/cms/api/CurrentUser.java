package kr.ac.knue.cms.api;

import java.util.List;

public record CurrentUser(String userId, String name, List<String> roles) {
    public boolean isAdmin() {
        return roles != null && roles.contains("R09");
    }
}
