package kr.ac.knue.performance.userrole;

import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper {
  List<UserRoleAssignmentDto> list(@Param("userId") UUID userId, @Param("roleCode") String roleCode, @Param("status") String status);
  void insert(@Param("request") CreateUserRoleRequest request, @Param("actor") UUID actor);
  int revoke(@Param("assignmentId") UUID assignmentId, @Param("actor") UUID actor);
  UserRoleAssignmentDto find(@Param("assignmentId") UUID assignmentId);
  int countRole(@Param("roleCode") String roleCode);
}
