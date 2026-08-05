package kr.ac.knue.cms.phase2;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.AdminMapper;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommonConfigService {
    private static final Map<String, String> EXPECTED_UNITS = Map.of(
        "SESSION_IDLE_TIMEOUT_MINUTES", "MINUTE",
        "PAGE_SIZE", "COUNT",
        "DEFAULT_SEARCH_PERIOD_YEARS", "YEAR",
        "BULK_QUERY_THRESHOLD_COUNT", "COUNT",
        "LONG_TASK_NOTICE_THRESHOLD_SECONDS", "SECOND"
    );
    private final AdminMapper mapper;
    private final ChangeHistoryService history;

    public CommonConfigService(AdminMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    public List<Map<String, Object>> list() {
        return mapper.listCommonConfigs();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> save(Map<String, Object> body) {
        Object rawItems = body.get("items");
        List<Map<String, Object>> items;
        if (rawItems instanceof List<?> list) {
            items = (List<Map<String, Object>>) list;
        } else if (body.get("configKey") != null) {
            items = List.of(body);
        } else {
            throw new BusinessException(400, "설정 항목이 필요합니다.", Map.of("items", "하나 이상 필요합니다."));
        }
        if (items.isEmpty()) throw new BusinessException(400, "설정 항목이 필요합니다.", Map.of("items", "하나 이상 필요합니다."));
        for (int i = 0; i < items.size(); i++) {
            validate(items.get(i), i);
            mapper.upsertCommonConfig(items.get(i), Phase2Validation.text(body, "changeReason"));
            history.record("common_config", Phase2Validation.text(items.get(i), "configKey"), "UPDATE", null, items.get(i).toString(), SessionContext.currentUserId(), Phase2Validation.text(body, "changeReason"));
        }
        return mapper.listCommonConfigs();
    }

    private void validate(Map<String, Object> item, int index) {
        String key = Phase2Validation.text(item, "configKey");
        String value = Phase2Validation.text(item, "configValue");
        String unit = Phase2Validation.text(item, "unit");
        Map<String, String> fields = new LinkedHashMap<>();
        if (key.isBlank()) fields.put("items[" + index + "].configKey", "필수값입니다.");
        if (value.isBlank()) fields.put("items[" + index + "].configValue", "필수값입니다.");
        String expected = EXPECTED_UNITS.get(key);
        if (expected == null) fields.put("items[" + index + "].configKey", "허용되지 않는 설정키입니다.");
        else if (!unit.isBlank() && !expected.equals(unit)) fields.put("items[" + index + "].unit", expected + " 단위여야 합니다.");
        if (!value.matches("[1-9][0-9]*")) fields.put("items[" + index + "].configValue", (expected == null ? "설정" : expected) + " 단위는 양의 정수여야 합니다.");
        if (!fields.isEmpty()) throw new BusinessException(400, "설정값 형식을 확인해 주세요.", fields);
        item.put("unit", expected);
    }
}
