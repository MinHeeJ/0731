package kr.ac.knue.cms.user;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class UserManagementService {
    private final UserMapper mapper; private final ChangeHistoryService history;
    public UserManagementService(UserMapper mapper, ChangeHistoryService history) { this.mapper = mapper; this.history = history; }
    public List<Map<String,Object>> list(Map<String,Object> filters) { return mapper.listUsers(filters); }
    @Transactional public Map<String,Object> updateUsage(String userId, String useYn, String reason) {
        if (!Set.of("Y","N").contains(useYn)) throw new BusinessException(400, "사용여부는 Y 또는 N이어야 합니다.", Map.of("useYn", "Y 또는 N만 입력할 수 있습니다."));
        Map<String,Object> before = mapper.findUser(userId); if (before == null) throw new BusinessException(400, "사용자를 찾을 수 없습니다.", Map.of("userId", "존재하지 않는 사용자입니다."));
        mapper.updateUsage(userId, useYn, reason); Map<String,Object> after = mapper.findUser(userId);
        history.record("user_account", userId, "UPDATE", json(before), json(after), SessionContext.currentUserId(), reason); return after;
    }
    @Transactional public Map<String,Object> replaceRoles(String userId, List<String> roleCodes, String reason) {
        Map<String,Object> before = mapper.findUser(userId); if (before == null) throw new BusinessException(400, "사용자를 찾을 수 없습니다.", Map.of("userId", "존재하지 않는 사용자입니다."));
        for (String roleCode : roleCodes) if (mapper.countRole(roleCode) == 0) throw new BusinessException(400, "존재하지 않는 역할코드입니다.", Map.of("roleCodes", roleCode + "는 존재하지 않습니다."));
        mapper.revokeManualRoles(userId); for (String roleCode : roleCodes) mapper.insertManualRole(userId, roleCode, SessionContext.currentUserId());
        Map<String,Object> after = mapper.findUser(userId); history.record("user_role", userId, "UPDATE", json(before), json(after), SessionContext.currentUserId(), reason); return after;
    }
    private String json(Object value) { return value == null ? null : value.toString().replace('=', ':'); }
}
