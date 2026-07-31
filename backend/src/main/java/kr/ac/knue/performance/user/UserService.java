package kr.ac.knue.performance.user;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.performance.auth.CurrentUser;
import kr.ac.knue.performance.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
  private final UserMapper mapper;
  public UserService(UserMapper mapper) { this.mapper = mapper; }
  public List<UserDto> list(String staffNo, String staffName, String organizationCode, String jobGrade, String employmentStatus, String roleCode, Boolean systemUseEnabled) {
    return mapper.list(blank(staffNo), blank(staffName), blank(organizationCode), blank(jobGrade), blank(employmentStatus), blank(roleCode), systemUseEnabled).stream().map(this::toDto).toList();
  }
  @Transactional
  public UserDto updateUsage(UUID userId, UserRequests.Usage request) {
    if (mapper.updateUsage(userId, request.systemUseEnabled()) == 0) throw new ApiException(HttpStatus.BAD_REQUEST, "NOT_FOUND", "사용자를 찾을 수 없습니다.");
    return toDto(mapper.find(userId));
  }
  @Transactional
  public UserDto replaceRoles(UUID userId, UserRequests.Roles request, CurrentUser actor) {
    for (String roleCode : request.roleCodes()) if (mapper.countRole(roleCode) == 0) throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "역할코드를 확인해 주세요.", java.util.Map.of("roleCodes", "R01~R09 역할만 부여할 수 있습니다."));
    mapper.revokeRoles(userId);
    LocalDate validFrom = request.validFrom() == null ? LocalDate.now() : request.validFrom();
    for (String roleCode : request.roleCodes()) mapper.insertRole(userId, roleCode, validFrom, request.validTo(), actor.userId());
    return toDto(mapper.find(userId));
  }
  private UserDto toDto(UserMapper.UserRow row) { return new UserDto(row.userId(), row.staffNo(), row.staffName(), row.organizationCode(), row.positionName(), row.jobGrade(), row.employmentStatus(), mapper.roleCodes(row.userId()), row.systemUseEnabled(), row.retirementDate(), row.lastSyncedAt()); }
  private String blank(String value) { return value == null || value.isBlank() ? null : value; }
}
