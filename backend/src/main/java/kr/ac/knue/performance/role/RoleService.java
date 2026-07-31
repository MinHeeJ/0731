package kr.ac.knue.performance.role;

import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.ac.knue.performance.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {
  private static final Set<String> ROLE_CODES = Set.of("R01", "R02", "R03", "R04", "R05", "R06", "R07", "R08", "R09");
  private final RoleMapper roleMapper;

  public RoleService(RoleMapper roleMapper) {
    this.roleMapper = roleMapper;
  }

  public List<RoleDto> list(String roleCode, String roleName) {
    return roleMapper.list(blank(roleCode), blank(roleName));
  }

  @Transactional
  public RoleDto create(RoleRequest request) {
    validateRole(request.roleCode());
    if (roleMapper.update(request.roleCode(), request) == 0) {
      roleMapper.insert(request);
    }
    return roleMapper.find(request.roleCode());
  }

  @Transactional
  public RoleDto update(String roleCode, RoleRequest request) {
    if (!roleCode.equals(request.roleCode())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "역할코드는 변경할 수 없습니다.", Map.of("roleCode", "path와 body가 일치해야 합니다."));
    }
    roleMapper.update(roleCode, request);
    return roleMapper.find(roleCode);
  }

  private void validateRole(String roleCode) {
    if (!ROLE_CODES.contains(roleCode)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "R01~R09 역할만 사용할 수 있습니다.", Map.of("roleCode", "R01~R09 중 하나여야 합니다."));
    }
  }

  private String blank(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
