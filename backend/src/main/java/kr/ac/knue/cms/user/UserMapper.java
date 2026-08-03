package kr.ac.knue.cms.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    List<Map<String,Object>> listUsers(@Param("f") Map<String,Object> filters);
    Map<String,Object> findUser(@Param("userId") String userId);
    void updateUsage(@Param("userId") String userId, @Param("useYn") String useYn, @Param("reason") String reason);
    void revokeManualRoles(@Param("userId") String userId);
    void insertManualRole(@Param("userId") String userId, @Param("roleCode") String roleCode, @Param("approvedBy") String approvedBy);
    int countRole(@Param("roleCode") String roleCode);
    List<Map<String,Object>> listOrganizations(@Param("f") Map<String,Object> filters);
    List<Map<String,Object>> listOrganizationRelations(@Param("organizationCode") String organizationCode);
    void insertOrganizationRelation(@Param("organizationCode") String organizationCode, @Param("parentOrganizationCode") String parentOrganizationCode, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("reason") String reason);
}
