package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CommonSettingsController {
    private final CommonSettingsService service;

    public CommonSettingsController(CommonSettingsService service) {
        this.service = service;
    }

    @GetMapping("/api/system/common-settings")
    public ApiResponse<CommonSettingsPage> getCommonSettings(@RequestParam(required = false) String settingKey,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.list(settingKey, page, size));
    }

    @PutMapping("/api/system/common-settings")
    public ApiResponse<CommonSettingsPage> updateCommonSettings(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(service.update(request));
    }

    @PutMapping("/api/system/common-settings/session-idle-time")
    public ApiResponse<Map<String, Object>> sessionIdleTime(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(service.updateSingle("sessionIdleTime", request));
    }

    @PutMapping("/api/system/common-settings/page-size")
    public ApiResponse<Map<String, Object>> pageSize(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(service.updateSingle("pageSize", request));
    }

    @PutMapping("/api/system/common-settings/default-search-period")
    public ApiResponse<Map<String, Object>> defaultSearchPeriod(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(service.updateSingle("defaultSearchPeriod", request));
    }

    @PutMapping("/api/system/common-settings/bulk-query-threshold")
    public ApiResponse<Map<String, Object>> bulkQueryThreshold(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(service.updateSingle("bulkQueryThreshold", request));
    }

    @PutMapping("/api/system/common-settings/long-running-task-notice-threshold")
    public ApiResponse<Map<String, Object>> longRunningTaskNoticeThreshold(@RequestBody Map<String, Object> request) {
        return ApiResponse.ok(service.updateSingle("longRunningTaskNoticeThreshold", request));
    }

    @GetMapping("/api/system/common-settings/existing-system-auth")
    public ApiResponse<Map<String, Object>> existingSystemAuth() {
        return ApiResponse.ok(service.existingSystemAuth());
    }

    @GetMapping("/api/system/common-settings/setting-meaning")
    public ApiResponse<Map<String, Object>> settingMeaning(@RequestParam String settingKey) {
        return ApiResponse.ok(service.settingMeaning(settingKey));
    }

    @GetMapping("/api/system/common-settings/setting-unit")
    public ApiResponse<Map<String, Object>> settingUnit(@RequestParam String settingKey) {
        return ApiResponse.ok(service.settingUnit(settingKey));
    }

    @GetMapping("/api/system/common-settings/setting-value")
    public ApiResponse<Map<String, Object>> settingValue(@RequestParam String settingKey) {
        return ApiResponse.ok(service.settingValue(settingKey));
    }
}
