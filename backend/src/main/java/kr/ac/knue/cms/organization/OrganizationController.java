package kr.ac.knue.cms.organization;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import kr.ac.knue.cms.user.UserMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
public class OrganizationController {
    private final UserMapper mapper;
    private final ChangeHistoryService history;

    public OrganizationController(UserMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    @GetMapping("/api/organizations")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        return ApiResponse.ok(mapper.listOrganizations(filters));
    }

    @GetMapping("/api/organizations/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.ok(mapper.listOrganizations(Map.of()));
    }

    @PutMapping("/api/organizations/{organizationCode}/relations")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> relation(@PathVariable String organizationCode, @RequestBody Map<String, Object> body) {
        String startDate = String.valueOf(body.getOrDefault("effectiveStartDate", body.getOrDefault("effective_start_date", "")));
        String endDate = String.valueOf(body.getOrDefault("effectiveEndDate", body.getOrDefault("effective_end_date", "")));
        if (!endDate.isBlank() && LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))) {
            throw new BusinessException(400, "종료일은 시작일보다 이전일 수 없습니다.", Map.of("effectiveEndDate", "종료일은 시작일 이후여야 합니다."));
        }
        List<Map<String, Object>> before = mapper.listOrganizationRelations(organizationCode);
        mapper.insertOrganizationRelation(organizationCode, (String) body.get("parentOrganizationCode"), startDate, endDate, (String) body.get("changeReason"));
        List<Map<String, Object>> after = mapper.listOrganizationRelations(organizationCode);
        history.record("organization_relation", organizationCode, "UPDATE", before.toString(), after.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(after);
    }
}
