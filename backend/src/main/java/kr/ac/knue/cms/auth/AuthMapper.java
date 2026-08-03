package kr.ac.knue.cms.auth;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface AuthMapper {
    Map<String, Object> findByLoginId(@Param("loginId") String loginId);
    List<String> findRoleCodesByUserId(@Param("userId") String userId);
    void insertSession(@Param("sessionId") String sessionId, @Param("userId") String userId, @Param("hours") int hours);
    Map<String, Object> findSession(@Param("sessionId") String sessionId);
    void revokeSession(@Param("sessionId") String sessionId);
}
