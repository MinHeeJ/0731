package kr.ac.knue.cms.menu;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MyMenuController {
    private final MyMenuService service;

    public MyMenuController(MyMenuService service) {
        this.service = service;
    }

    @GetMapping("/api/menus/my")
    public ApiResponse<List<Map<String, Object>>> my() {
        return ApiResponse.ok(service.listMyMenus(SessionContext.currentUserId()));
    }
}
