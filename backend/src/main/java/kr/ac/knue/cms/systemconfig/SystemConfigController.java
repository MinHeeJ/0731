package kr.ac.knue.cms.systemconfig;

import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SystemConfigController {
    private final SystemConfigService service;

    public SystemConfigController(SystemConfigService service) {
        this.service = service;
    }

    @GetMapping("/api/system-config")
    public ApiResponse<List<Map<String, Object>>> listSystemConfigs() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/api/system-config/{settingKey}")
    public ApiResponse<Map<String, Object>> getSystemConfig(@PathVariable String settingKey) {
        return ApiResponse.ok(service.get(settingKey));
    }

    @PutMapping("/api/system-config/{settingKey}")
    public ApiResponse<Map<String, Object>> updateSystemConfig(@PathVariable String settingKey, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(service.update(settingKey, body));
    }

    @PutMapping("/api/system-config")
    public ApiResponse<List<Map<String, Object>>> bulkUpdateSystemConfigs(@RequestBody Map<String, Object> body) {
        Object items = body.get("items");
        if (!(items instanceof List<?> list) || list.isEmpty()) {
            throw new BusinessException(400, "저장할 환경설정 항목이 필요합니다.", Map.of("items", "하나 이상 필요합니다."));
        }
        return ApiResponse.ok(service.bulkUpdate(body));
    }
}
