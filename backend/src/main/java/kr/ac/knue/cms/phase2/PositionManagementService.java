package kr.ac.knue.cms.phase2;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PositionManagementService {
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public PositionManagementService(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    public List<Map<String, Object>> list(Map<String, Object> filters) {
        return mapper.listPositions(filters);
    }

    @Transactional
    public List<Map<String, Object>> create(Map<String, Object> body) {
        validate(body);
        mapper.insertPosition(body);
        history.record("common_position", Phase2Validation.text(body, "positionCode"), "CREATE", null, body.toString(), SessionContext.currentUserId(), Phase2Validation.text(body, "changeReason"));
        return mapper.listPositions(Map.of("positionCode", Phase2Validation.text(body, "positionCode"), "userId", Phase2Validation.text(body, "userId"), "organizationCode", Phase2Validation.text(body, "organizationCode")));
    }

    @Transactional
    public List<Map<String, Object>> update(long positionId, Map<String, Object> body) {
        validate(body);
        int updated = mapper.updatePosition(positionId, body);
        if (updated == 0) throw new BusinessException(400, "존재하지 않는 보직입니다.", Map.of("positionId", "존재하지 않는 보직입니다."));
        history.record("common_position", String.valueOf(positionId), "UPDATE", null, body.toString(), SessionContext.currentUserId(), Phase2Validation.text(body, "changeReason"));
        return mapper.listPositions(Map.of("positionId", String.valueOf(positionId)));
    }

    private void validate(Map<String, Object> body) {
        Phase2Validation.require(body, "positionCode", "userId", "organizationCode", "effectiveStartDate", "effectiveEndDate");
        Phase2Validation.period(body, "effectiveStartDate", "effectiveEndDate");
        if (mapper.countUser(Phase2Validation.text(body, "userId")) == 0) {
            throw new BusinessException(400, "참조 식별자를 확인해 주세요.", Map.of("userId", "존재하지 않는 사용자입니다."));
        }
        if (mapper.countOrganization(Phase2Validation.text(body, "organizationCode")) == 0) {
            throw new BusinessException(400, "참조 식별자를 확인해 주세요.", Map.of("organizationCode", "존재하지 않는 조직입니다."));
        }
    }
}
