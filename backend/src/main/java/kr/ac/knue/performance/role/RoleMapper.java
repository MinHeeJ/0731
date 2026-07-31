package kr.ac.knue.performance.role;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RoleMapper {
  List<RoleDto> list(@Param("roleCode") String roleCode, @Param("roleName") String roleName);
  int insert(RoleRequest request);
  int update(@Param("roleCode") String roleCode, @Param("request") RoleRequest request);
  RoleDto find(@Param("roleCode") String roleCode);
}
