package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommonSettingService {
    private static final Map<String, SettingContract> CONTRACTS = new LinkedHashMap<>();
    private static final Set<String> FORBIDDEN_SCOPE_FIELDS = Set.of("userId", "businessCategory");

    static {
        CONTRACTS.put("sessionIdleMinutes", new SettingContract("MINUTE", "INTEGER"));
        CONTRACTS.put("pageSize", new SettingContract("COUNT", "INTEGER"));
        CONTRACTS.put("defaultSearchPeriodDays", new SettingContract("DAY", "INTEGER"));
        CONTRACTS.put("bulkQueryThresholdCount", new SettingContract("COUNT", "INTEGER"));
        CONTRACTS.put("longRunningTaskNoticeSeconds", new SettingContract("SECOND", "INTEGER"));
    }

    private final CommonSettingMapper mapper;
    private final ChangeHistoryService history;

    public CommonSettingService(CommonSettingMapper mapper, ChangeHistoryService history) {
        this.mapper = mapper;
        this.history = history;
    }

    public CommonSettingsResponse getCommonSettings() {
        List<CommonSettingItem> items = mapper.listGlobalSettings();
        return new CommonSettingsResponse(items);
    }

    @Transactional
    public CommonSettingsResponse updateCommonSettings(Map<String, Object> body) {
        Map<String, String> fields = validatePayloadShape(body);
        if (!fields.isEmpty()) {
            throw new BusinessException(400, "입력값을 확인해 주세요.", fields);
        }
        List<CommonSettingItem> beforeItems = mapper.listGlobalSettings();
        Map<String, CommonSettingItem> beforeByKey = beforeItems.stream()
            .collect(Collectors.toMap(CommonSettingItem::getSettingKey, Function.identity()));
        validateContractRows(beforeByKey, fields);
        if (!fields.isEmpty()) {
            throw new BusinessException(400, "입력값을 확인해 주세요.", fields);
        }

        String changeReason = stringValue(body.get("changeReason"));
        for (String settingKey : CONTRACTS.keySet()) {
            String nextValue = parseIntegerString(settingKey, body.get(settingKey));
            CommonSettingItem before = beforeByKey.get(settingKey);
            mapper.updateSettingValue(settingKey, nextValue, changeReason);
            history.record(
                "system_common_setting",
                settingKey,
                "UPDATE",
                before == null ? null : before.getSettingValue(),
                nextValue,
                SessionContext.currentUserId(),
                changeReason
            );
        }
        return getCommonSettings();
    }

    private Map<String, String> validatePayloadShape(Map<String, Object> body) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (body == null) {
            fields.put("body", "요청 본문은 필수입니다.");
            return fields;
        }
        for (String forbidden : FORBIDDEN_SCOPE_FIELDS) {
            if (body.containsKey(forbidden)) {
                fields.put(forbidden, "사용자별 또는 업무별 scope 설정은 허용되지 않습니다.");
            }
        }
        for (String key : body.keySet()) {
            if (!CONTRACTS.containsKey(key) && !"changeReason".equals(key)) {
                fields.put(key, "허용되지 않는 공통 환경설정 필드입니다.");
            }
        }
        for (String settingKey : CONTRACTS.keySet()) {
            if (!body.containsKey(settingKey)) {
                fields.put(settingKey, "필수값입니다.");
            } else {
                try {
                    parseIntegerString(settingKey, body.get(settingKey));
                } catch (BusinessException ex) {
                    fields.putAll(ex.fields());
                }
            }
        }
        String changeReason = stringValue(body.get("changeReason"));
        if (changeReason != null && changeReason.length() > 500) {
            fields.put("changeReason", "변경사유는 500자 이하여야 합니다.");
        }
        return fields;
    }

    private void validateContractRows(Map<String, CommonSettingItem> beforeByKey, Map<String, String> fields) {
        for (Map.Entry<String, SettingContract> entry : CONTRACTS.entrySet()) {
            CommonSettingItem row = beforeByKey.get(entry.getKey());
            if (row == null) {
                fields.put(entry.getKey(), "GLOBAL 공통 환경설정 seed가 없습니다.");
                continue;
            }
            SettingContract expected = entry.getValue();
            if (!"GLOBAL".equals(row.getScopeType())) {
                fields.put(entry.getKey(), "GLOBAL scope만 허용됩니다.");
            }
            if (!expected.unitCode().equals(row.getUnitCode()) || !expected.valueType().equals(row.getValueType())) {
                fields.put(entry.getKey(), "설정 항목의 단위 또는 값 유형이 계약과 다릅니다.");
            }
        }
    }

    private String parseIntegerString(String field, Object value) {
        String text = stringValue(value);
        if (text == null || !text.matches("-?\\d+")) {
            throw new BusinessException(400, "입력값을 확인해 주세요.", Map.of(field, "정수 값만 입력할 수 있습니다."));
        }
        return text;
    }

    private String stringValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return String.valueOf(value);
        return String.valueOf(value).trim();
    }

    private record SettingContract(String unitCode, String valueType) {
    }
}
