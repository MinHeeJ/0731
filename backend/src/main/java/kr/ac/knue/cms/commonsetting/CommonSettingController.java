package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CommonSettingController {
    private final CommonSettingService service;

    public CommonSettingController(CommonSettingService service) {
        this.service = service;
    }

    @GetMapping("/api/system/common-settings")
    public ApiResponse<CommonSettingsResponse> getCommonSettings() {
        return ApiResponse.ok(service.getCommonSettings());
    }

    @PutMapping("/api/system/common-settings")
    public ApiResponse<CommonSettingsResponse> updateCommonSettings(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(service.updateCommonSettings(body));
    }
}
