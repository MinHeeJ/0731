package kr.ac.knue.performance.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  private final JdbcTemplate jdbcTemplate;
  public HealthController(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }
  @GetMapping("/api/health")
  ApiResponse<Map<String, String>> health(HttpServletRequest request) {
    jdbcTemplate.queryForObject("select 1", Integer.class);
    return ApiResponse.ok(Map.of("status", "UP", "database", "UP"), String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE)));
  }
}
