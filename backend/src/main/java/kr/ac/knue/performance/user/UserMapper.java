package kr.ac.knue.performance.user;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
  List<UserRow> list(@Param("staffNo") String staffNo, @Param("staffName") String staffName, @Param("organizationCode") String organizationCode, @Param("jobGrade") String jobGrade, @Param("employmentStatus") String employmentStatus, @Param("roleCode") String roleCode, @Param("systemUseEnabled") Boolean systemUseEnabled);
  UserRow find(@Param("userId") UUID userId);
  List<String> roleCodes(@Param("userId") UUID userId);
  int updateUsage(@Param("userId") UUID userId, @Param("enabled") boolean enabled);
  void revokeRoles(@Param("userId") UUID userId);
  void insertRole(@Param("userId") UUID userId, @Param("roleCode") String roleCode, @Param("validFrom") LocalDate validFrom, @Param("validTo") LocalDate validTo, @Param("actor") UUID actor);
  int countRole(@Param("roleCode") String roleCode);

  record UserRow(UUID userId, String staffNo, String staffName, String organizationCode, String positionName, String jobGrade, String employmentStatus, boolean systemUseEnabled, LocalDate retirementDate, OffsetDateTime lastSyncedAt) {}
}
