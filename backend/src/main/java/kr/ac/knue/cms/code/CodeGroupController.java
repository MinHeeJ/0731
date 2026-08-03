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
public class CodeGroupController {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public CodeGroupController(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    @GetMapping("/api/code-groups")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam Map<String, Object> filters) {
        SearchFilterValidator.requireOneOf(filters, "status", Set.of("ACTIVE", "INACTIVE"));
        return ApiResponse.ok(mapper.listCodeGroups(filters));
    }

    @PostMapping("/api/code-groups")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        if (body.get("groupId") == null || String.valueOf(body.get("groupId")).isBlank()) {
            throw new BusinessException(400, "코드그룹 ID는 필수입니다.", Map.of("groupId", "필수값입니다."));
        }
        mapper.createCodeGroup(body);
        history.record("code_group", String.valueOf(body.get("groupId")), "CREATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listCodeGroups(Map.of("groupId", String.valueOf(body.get("groupId")))));
    }

    @PutMapping("/api/code-groups/{groupId}")
    @Transactional
    public ApiResponse<List<Map<String, Object>>> update(@PathVariable String groupId, @RequestBody Map<String, Object> body) {
        if (mapper.updateCodeGroup(groupId, body) == 0) {
            throw new BusinessException(400, "존재하지 않는 코드그룹입니다.", Map.of("groupId", "존재하지 않는 코드그룹입니다."));
        }
        history.record("code_group", groupId, "UPDATE", null, body.toString(), SessionContext.currentUserId(), (String) body.get("changeReason"));
        return ApiResponse.ok(mapper.listCodeGroups(Map.of("groupId", groupId)));
    }
}
