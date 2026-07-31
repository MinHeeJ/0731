package kr.ac.knue.performance.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
  AuthUser findLoginUser(@Param("loginId") String loginId);
  AuthIdentity findCurrentUserIdentityByTokenHash(@Param("tokenHash") String tokenHash);
  AuthIdentity findCurrentUserIdentityById(@Param("userId") UUID userId);
  List<String> findRoleCodesByUserId(@Param("userId") UUID userId);
  default CurrentUser findCurrentUserByTokenHash(String tokenHash) {
    AuthIdentity identity = findCurrentUserIdentityByTokenHash(tokenHash);
    return identity == null ? null : new CurrentUser(identity.userId(), identity.loginId(), findRoleCodesByUserId(identity.userId()));
  }
  default CurrentUser findCurrentUserById(UUID userId) {
    AuthIdentity identity = findCurrentUserIdentityById(userId);
    return identity == null ? null : new CurrentUser(identity.userId(), identity.loginId(), findRoleCodesByUserId(identity.userId()));
  }
  void insertSession(@Param("userId") UUID userId, @Param("tokenHash") String tokenHash, @Param("expiresAt") OffsetDateTime expiresAt);
  void logout(@Param("tokenHash") String tokenHash);
  record AuthUser(UUID userId, String loginId, String passwordHash, boolean systemUseEnabled, String status) {}
  record AuthIdentity(UUID userId, String loginId) {}
}
