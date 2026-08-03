package kr.ac.knue.cms.authorization;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import kr.ac.knue.cms.common.SearchFilterValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class UserRoleController {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public UserRoleController(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    @GetMapping("/api/user-roles")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        SearchFilterValidator.requireOneOf(filters, "status", Set.of("ACTIVE", "REVOKED", "EXPIRED"));
        return ApiResponse.ok(mapper.listUserRoles(filters));
    }

    @PostMapping("/api/user-roles")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> grant(@RequestBody Map<String, Object> body) {
        if (body.get("userId") == null || body.get("roleCode") == null) {
            throw new BusinessException(400, "사용자와 역할은 필수입니다.", Map.of("userId", "필수값입니다.", "roleCode", "필수값입니다."));
        }
        mapper.grantUserRole(body, SessionContext.currentUserId());
        history.record("user_role", String.valueOf(body.get("userId")), "CREATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listUserRoles(Map.of("userId", String.valueOf(body.get("userId")))));
    }

    @PutMapping("/api/user-roles/{assignmentId}")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> update(@PathVariable long assignmentId, @RequestBody Map<String, Object> body) {
        if (mapper.updateUserRole(assignmentId, body) == 0) {
            throw new BusinessException(400, "POSITION 역할은 직접 변경할 수 없습니다.", Map.of("assignmentId", "수동 역할만 변경할 수 있습니다."));
        }
        history.record("user_role", String.valueOf(assignmentId), "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listUserRoles(Map.of()));
    }

    @DeleteMapping("/api/user-roles/{assignmentId}")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> revoke(@PathVariable long assignmentId, @RequestBody(required = false) Map<String, Object> body) {
        if (mapper.revokeUserRole(assignmentId) == 0) {
            throw new BusinessException(400, "POSITION 역할은 직접 회수할 수 없습니다.", Map.of("assignmentId", "수동 역할만 회수할 수 있습니다."));
        }
        String reason = body == null ? null : (String) body.get("changeReason");
        history.record("user_role", String.valueOf(assignmentId), "REVOKE", null, "{status:REVOKED}", SessionContext.currentUserId(), reason);
        return ApiResponse.ok(mapper.listUserRoles(Map.of()));
    }
}
