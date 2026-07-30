package kr.ac.knue.cms.common;

import java.util.List;

public record SessionUser(String sessionId, String userId, String name, List<String> roles) {
  public boolean isAdmin() { return roles != null && roles.contains("R09"); }
}
