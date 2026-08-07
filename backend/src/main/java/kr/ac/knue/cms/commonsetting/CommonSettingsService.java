package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.common.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class CommonSettingsService {
    private static final List<String> KEYS = List.of("sessionIdleTime", "pageSize", "defaultSearchPeriod", "bulkQueryThreshold", "longRunningTaskNoticeThreshold");
    private static final Set<String> FORBIDDEN_FIELDS = Set.of("userId", "businessId", "userOverride", "businessOverride", "settingUnit", "settingMeaning", "sessionStorage", "tokenPolicy");
    private static final Pattern DIGITS = Pattern.compile("^[0-9]+$");
    private final CommonSettingMapper mapper;

    public CommonSettingsService(CommonSettingMapper mapper) {
        this.mapper = mapper;
    }

    public CommonSettingsPage list(String settingKey, int page, int size) {
        if (page < 0) throw new BusinessException(400, "조회 조건을 확인해 주세요.", Map.of("page", "0 이상이어야 합니다."));
        if (size < 1) throw new BusinessException(400, "조회 조건을 확인해 주세요.", Map.of("size", "1 이상이어야 합니다."));
        if (settingKey != null && !settingKey.isBlank() && !KEYS.contains(settingKey)) {
            throw new BusinessException(400, "조회 조건을 확인해 주세요.", Map.of("settingKey", "허용되지 않는 공통 환경설정 항목입니다."));
        }
        int offset = page * size;
        return new CommonSettingsPage(mapper.list(settingKey, offset, size), page, size, mapper.count(settingKey));
    }

    @Transactional
    public CommonSettingsPage update(Map<String, Object> request) {
        Map<String, String> fields = validatePayload(request);
        if (!fields.isEmpty()) {
            throw new BusinessException(400, "공통 환경설정 값을 확인해 주세요. 기존 값은 유지됩니다.", fields);
        }
        for (String key : KEYS) {
            if (request.containsKey(key)) {
                mapper.updateValue(key, String.valueOf(request.get(key)).trim());
            }
        }
        return list(null, 0, 20);
    }

    @Transactional
    public Map<String, Object> updateSingle(String key, Map<String, Object> request) {
        if (!KEYS.contains(key)) throw new BusinessException(400, "허용되지 않는 공통 환경설정 항목입니다.", Map.of("settingKey", "허용되지 않는 공통 환경설정 항목입니다."));
        Map<String, String> fields = validatePayload(request);
        if (!request.containsKey(key)) fields.put(key, "필수값입니다.");
        for (String requestKey : request.keySet()) {
            if (!requestKey.equals(key)) fields.putIfAbsent(requestKey, "이 operation에서 변경할 수 없는 항목입니다.");
        }
        if (!fields.isEmpty()) {
            throw new BusinessException(400, "공통 환경설정 값을 확인해 주세요. 기존 값은 유지됩니다.", fields);
        }
        mapper.updateValue(key, String.valueOf(request.get(key)).trim());
        return mapper.findOne(key);
    }

    public Map<String, Object> existingSystemAuth() {
        return Map.of("authContext", "existingSystemAuth");
    }

    public Map<String, Object> settingMeaning(String settingKey) {
        Map<String, Object> row = requireSetting(settingKey);
        return Map.of("settingMeaning", row.get("settingMeaning"));
    }

    public Map<String, Object> settingUnit(String settingKey) {
        Map<String, Object> row = requireSetting(settingKey);
        return Map.of("settingUnit", row.get("settingUnit"));
    }

    public Map<String, Object> settingValue(String settingKey) {
        Map<String, Object> row = requireSetting(settingKey);
        return Map.of("settingValue", row.get("settingValue"));
    }

    public int countOverrideLikeRows() {
        return mapper.countOverrideLikeRows();
    }

    private Map<String, Object> requireSetting(String settingKey) {
        if (settingKey == null || settingKey.isBlank()) {
            throw new BusinessException(400, "settingKey는 필수입니다.", Map.of("settingKey", "필수값입니다."));
        }
        if (!KEYS.contains(settingKey)) {
            throw new BusinessException(400, "허용되지 않는 공통 환경설정 항목입니다.", Map.of("settingKey", "허용되지 않는 공통 환경설정 항목입니다."));
        }
        Map<String, Object> row = mapper.findOne(settingKey);
        if (row == null) throw new BusinessException(400, "공통 환경설정 항목을 찾을 수 없습니다.", Map.of("settingKey", "존재하지 않습니다."));
        return row;
    }

    private Map<String, String> validatePayload(Map<String, Object> request) {
        Map<String, String> fields = new LinkedHashMap<>();
        if (request == null || request.isEmpty()) {
            fields.put("settings", "변경할 공통 환경설정 값을 입력해 주세요.");
            return fields;
        }
        for (String key : request.keySet()) {
            if (!KEYS.contains(key)) {
                fields.put(key, FORBIDDEN_FIELDS.contains(key) ? "공통 환경설정은 사용자별·업무별 override 또는 metadata 변경을 저장하지 않습니다." : "허용되지 않는 요청 항목입니다.");
            }
        }
        for (String key : KEYS) {
            if (request.containsKey(key)) validateValue(key, request.get(key), fields);
        }
        return fields;
    }

    private void validateValue(String key, Object value, Map<String, String> fields) {
        String text = value == null ? "" : String.valueOf(value).trim();
        if (text.isEmpty()) {
            fields.put(key, "현재 값은 비워둘 수 없습니다.");
            return;
        }
        if (!DIGITS.matcher(text).matches()) {
            fields.put(key, key + " 항목 의미와 단위에 맞는 숫자 값을 입력해 주세요.");
        }
    }
}
