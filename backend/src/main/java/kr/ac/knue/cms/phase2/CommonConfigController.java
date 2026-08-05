package kr.ac.knue.cms.phase2;

import kr.ac.knue.cms.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CommonConfigController {
    private final CommonConfigService service;

    public CommonConfigController(CommonConfigService service) {
        this.service = service;
    }

    @GetMapping("/api/common-configs")
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(service.list());
    }

    @PutMapping("/api/common-configs")
    public ApiResponse<List<Map<String, Object>>> save(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(service.save(body));
    }
}
