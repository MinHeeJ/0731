package kr.ac.knue.cms.menu;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MenuStructureController {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public MenuStructureController(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    @GetMapping("/api/menus/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.ok(mapper.listMenus(Map.of()));
    }

    @PutMapping("/api/menus/{menuId}/parent")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> parent(@PathVariable String menuId, @RequestBody Map<String, Object> body) {
        String parentMenuId = (String) body.get("parentMenuId");
        if (menuId.equals(parentMenuId)) {
            throw new BusinessException(400, "자기 자신은 부모 메뉴가 될 수 없습니다.", Map.of("parentMenuId", "자기 자신 지정 불가"));
        }
        mapper.updateMenuParent(menuId, parentMenuId);
        history.record("menu", menuId, "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listMenus(Map.of()));
    }

    @PutMapping("/api/menus/reorder")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> reorder(@RequestBody Map<String, Object> body) {
        Object rows = body.get("items");
        if (!(rows instanceof List<?> list) || list.isEmpty()) {
            throw new BusinessException(400, "재정렬 대상은 필수입니다.", Map.of("items", "하나 이상 필요합니다."));
        }
        for (Object item : list) {
            Map<String, Object> row = (Map<String, Object>) item;
            mapper.updateMenuOrder(String.valueOf(row.get("menuId")), Integer.parseInt(String.valueOf(row.get("displayOrder"))));
        }
        history.record("menu", "reorder", "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listMenus(Map.of()));
    }
}
