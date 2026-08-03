package kr.ac.knue.cms.user;

import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class UserManagementController {
    private final UserManagementService service;

    public UserManagementController(UserManagementService service) {
        this.service = service;
    }

    @GetMapping("/api/users")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(service.list(filters));
    }

    @PatchMapping("/api/users/{userId}/usage")
    public ApiResponse<Map<String, Object>> usage(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        String useYn = String.valueOf(body.getOrDefault("useYn", body.getOrDefault("use_yn", "")));
        if (useYn.isBlank()) {
            throw new BusinessException(400, "사용여부는 필수입니다.", Map.of("useYn", "필수값입니다."));
        }
        return ApiResponse.ok(service.updateUsage(userId, useYn, (String) body.get("changeReason")));
    }

    @PutMapping("/api/users/{userId}/roles")
    public ApiResponse<Map<String, Object>> roles(@PathVariable String userId, @RequestBody Map<String, Object> body) {
        Object raw = body.getOrDefault("roleCodes", List.of());
        List<String> roleCodes = raw instanceof List<?> list
            ? list.stream().map(String::valueOf).toList()
            : Arrays.stream(String.valueOf(raw).split(",")).filter(s -> !s.isBlank()).toList();
        if (roleCodes.isEmpty()) {
            throw new BusinessException(400, "사용자 역할은 하나 이상 필요합니다.", Map.of("roleCodes", "하나 이상 필요합니다."));
        }
        return ApiResponse.ok(service.replaceRoles(userId, roleCodes, (String) body.get("changeReason")));
    }
}
