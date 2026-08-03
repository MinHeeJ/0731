package kr.ac.knue.cms.authorization;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import kr.ac.knue.cms.common.SearchFilterValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class MenuPermissionController {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;
    private final AuthorizationService authorizationService;

    public MenuPermissionController(AdminMapper mapper, ChangeHistoryService history, AuthorizationService authorizationService) {
        this.mapper = mapper;
        this.history = history;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/api/menu-permissions")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        SearchFilterValidator.requireOneOf(filters, "targetType", Set.of("ROLE", "ORGANIZATION", "USER"));
        return ApiResponse.ok(mapper.listMenuPermissions(filters));
    }

    @PutMapping("/api/menu-permissions")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> save(@RequestBody Map<String, Object> body) {
        Object rows = body.getOrDefault("permissions", List.of(body));
        if (!(rows instanceof List<?> list) || list.isEmpty()) {
            throw new BusinessException(400, "저장할 메뉴 권한은 필수입니다.", Map.of("permissions", "하나 이상 필요합니다."));
        }
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> permission)) {
                throw new BusinessException(400, "메뉴 권한 형식이 올바르지 않습니다.", Map.of("permissions", "객체 목록이어야 합니다."));
            }
            mapper.upsertMenuPermission((Map<String, Object>) permission);
        }
        history.record("menu_permission", String.valueOf(body.get("targetId")), "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        authorizationService.canAccess(SessionContext.currentUserId(), "/api/menu-permissions");
        return ApiResponse.ok(mapper.listMenuPermissions(Map.of()));
    }
}
