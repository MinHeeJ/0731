package kr.ac.knue.performance.userrole;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.performance.auth.CurrentUser;
import kr.ac.knue.performance.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRoleService {
  private final UserRoleMapper userRoleMapper;

  public UserRoleService(UserRoleMapper userRoleMapper) {
    this.userRoleMapper = userRoleMapper;
  }

  public List<UserRoleAssignmentDto> list(UUID userId, String roleCode, String status) {
    return userRoleMapper.list(userId, blank(roleCode), blank(status));
  }

  @Transactional
  public UserRoleAssignmentDto grant(CreateUserRoleRequest request, CurrentUser actor) {
    if (userRoleMapper.countRole(request.roleCode()) == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "역할코드를 확인해 주세요.", Map.of("roleCode", "R01~R09 역할만 부여할 수 있습니다."));
    }
    userRoleMapper.insert(request, actor.userId());
    List<UserRoleAssignmentDto> rows = userRoleMapper.list(request.userId(), request.roleCode(), "ACTIVE");
    return rows.get(rows.size() - 1);
  }

  @Transactional
  public UserRoleAssignmentDto revoke(UUID assignmentId, CurrentUser actor) {
    if (userRoleMapper.revoke(assignmentId, actor.userId()) == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "회수 가능한 역할을 찾을 수 없습니다.", Map.of("assignmentId", "ACTIVE 상태만 회수할 수 있습니다."));
    }
    return userRoleMapper.find(assignmentId);
  }

  private String blank(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
