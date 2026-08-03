package kr.ac.knue.cms.role;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RoleController {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public RoleController(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    @GetMapping("/api/roles")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(mapper.listRoles(filters));
    }

    @PostMapping("/api/roles")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        if (body.get("roleCode") == null || String.valueOf(body.get("roleCode")).isBlank()) {
            throw new BusinessException(400, "역할코드는 필수입니다.", Map.of("roleCode", "필수값입니다."));
        }
        mapper.createRole(body);
        history.record("role", String.valueOf(body.get("roleCode")), "CREATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listRoles(Map.of("roleCode", String.valueOf(body.get("roleCode")))));
    }

    @PutMapping("/api/roles/{roleCode}")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> update(@PathVariable String roleCode, @RequestBody Map<String, Object> body) {
        if (mapper.updateRole(roleCode, body) == 0) {
            throw new BusinessException(400, "존재하지 않는 역할코드입니다.", Map.of("roleCode", "존재하지 않는 역할코드입니다."));
        }
        history.record("role", roleCode, "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listRoles(Map.of("roleCode", roleCode)));
    }
}
