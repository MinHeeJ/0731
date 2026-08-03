package kr.ac.knue.cms.menu;

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
public class MenuInformationController {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public MenuInformationController(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    @GetMapping("/api/menus")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(mapper.listMenus(filters));
    }

    @PostMapping("/api/menus")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        if (body.get("menuId") == null || body.get("menuName") == null) {
            throw new BusinessException(400, "메뉴 ID와 이름은 필수입니다.", Map.of("menuId", "필수값입니다.", "menuName", "필수값입니다."));
        }
        mapper.createMenu(body);
        history.record("menu", String.valueOf(body.get("menuId")), "CREATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listMenus(Map.of()));
    }

    @PutMapping("/api/menus/{menuId}")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> update(@PathVariable String menuId, @RequestBody Map<String, Object> body) {
        if (mapper.updateMenu(menuId, body) == 0) {
            throw new BusinessException(400, "존재하지 않는 메뉴입니다.", Map.of("menuId", "존재하지 않는 메뉴입니다."));
        }
        history.record("menu", menuId, "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listMenus(Map.of()));
    }
}
