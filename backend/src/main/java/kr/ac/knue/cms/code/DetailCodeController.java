package kr.ac.knue.cms.code;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.ApiResponse;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import kr.ac.knue.cms.common.SearchFilterValidator;
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
import java.util.Set;

@RestController
public class DetailCodeController {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public DetailCodeController(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    @GetMapping("/api/code-groups/{groupId}/detail-codes")
    public ApiResponse<List<Map<String, Object>>> list(@PathVariable String groupId, @RequestParam Map<String, Object> filters) {
        SearchFilterValidator.requireOneOf(filters, "status", Set.of("ACTIVE", "INACTIVE"));
        return ApiResponse.ok(mapper.listDetailCodes(groupId, filters));
    }

    @GetMapping("/api/code-groups/{groupId}/detail-codes/tree")
    public ApiResponse<List<Map<String, Object>>> tree(@PathVariable String groupId, @RequestParam Map<String, Object> filters) {
        SearchFilterValidator.requireOneOf(filters, "status", Set.of("ACTIVE", "INACTIVE"));
        return ApiResponse.ok(mapper.listDetailCodes(groupId, filters));
    }

    @PostMapping("/api/code-groups/{groupId}/detail-codes")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> create(@PathVariable String groupId, @RequestBody Map<String, Object> body) {
        if (body.get("codeValue") == null || String.valueOf(body.get("codeValue")).isBlank()) {
            throw new BusinessException(400, "상세코드 값은 필수입니다.", Map.of("codeValue", "필수값입니다."));
        }
        mapper.createDetailCode(groupId, body);
        history.record("detail_code", groupId + ":" + body.get("codeValue"), "CREATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listDetailCodes(groupId, Map.of()));
    }

    @PutMapping("/api/code-groups/{groupId}/detail-codes/{codeValue}")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> update(@PathVariable String groupId, @PathVariable String codeValue, @RequestBody Map<String, Object> body) {
        if (mapper.updateDetailCode(groupId, codeValue, body) == 0) {
            throw new BusinessException(400, "존재하지 않는 상세코드입니다.", Map.of("codeValue", "존재하지 않는 상세코드입니다."));
        }
        history.record("detail_code", groupId + ":" + codeValue, "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listDetailCodes(groupId, Map.of()));
    }
}
