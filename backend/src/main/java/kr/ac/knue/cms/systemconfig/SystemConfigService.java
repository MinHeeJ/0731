package kr.ac.knue.cms.systemconfig;

import kr.ac.knue.cms.auth.SessionContext;
import kr.ac.knue.cms.common.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemConfigService {
    private final SystemConfigMapper mapper;

    public SystemConfigService(SystemConfigMapper mapper) {
        this.mapper = mapper;
    }

    public List<Map<String, Object>> list() {
        return mapper.listActive();
    }

    public Map<String, Object> get(String settingKey) {
        return activeConfig(settingKey);
    }

    @Transactional
    public Map<String, Object> update(String settingKey, Map<String, Object> body) {
        Map<String, Object> row = activeConfig(settingKey);
        String value = requestedValue(body);
        validateValue(row, value);
        String updatedBy = SessionContext.currentUserId();
        if (mapper.updateValue(settingKey, value, updatedBy) == 0) {
            throw notFound();
        }
        return activeConfig(settingKey);
    }

    @Transactional
    public List<Map<String, Object>> bulkUpdate(Map<String, Object> body) {
        Object rawItems = body.get("items");
        if (!(rawItems instanceof List<?> rawList) || rawList.isEmpty()) {
            throw new BusinessException(400, "저장할 환경설정 항목이 필요합니다.", Map.of("items", "하나 이상 필요합니다."));
        }

        List<PendingUpdate> pending = new ArrayList<>();
        for (Object raw : rawList) {
            if (!(raw instanceof Map<?, ?> item)) {
                throw new BusinessException(400, "환경설정 항목 형식이 올바르지 않습니다.", Map.of("items", "객체 배열이어야 합니다."));
            }
            Map<String, Object> normalized = normalize(item);
            String settingKey = requestedKey(normalized);
            Map<String, Object> row = activeConfig(settingKey);
            String value = requestedValue(normalized);
            validateValue(row, value);
            pending.add(new PendingUpdate(settingKey, value));
        }

        String updatedBy = SessionContext.currentUserId();
        for (PendingUpdate update : pending) {
            if (mapper.updateValue(update.settingKey(), update.settingValue(), updatedBy) == 0) {
                throw notFound();
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (PendingUpdate update : pending) {
            result.add(activeConfig(update.settingKey()));
        }
        return result;
    }

    private Map<String, Object> activeConfig(String settingKey) {
        if (settingKey == null || settingKey.isBlank()) {
            throw new BusinessException(400, "환경설정 항목코드는 필수입니다.", Map.of("settingKey", "필수값입니다."));
        }
        Map<String, Object> row = mapper.findByKey(settingKey);
        if (row == null || !"Y".equals(String.valueOf(row.get("useYn")))) {
            throw notFound();
        }
        return row;
    }

    private String requestedKey(Map<String, Object> body) {
        Object value = body.getOrDefault("settingKey", body.get("setting_key"));
        if (value == null || String.valueOf(value).isBlank()) {
            throw new BusinessException(400, "환경설정 항목코드는 필수입니다.", Map.of("settingKey", "필수값입니다."));
        }
        return String.valueOf(value);
    }

    private String requestedValue(Map<String, Object> body) {
        Object value = body.getOrDefault("settingValue", body.get("setting_value"));
        if (value == null || String.valueOf(value).isBlank()) {
            throw new BusinessException(400, "설정값은 필수입니다.", Map.of("settingValue", "필수값입니다."));
        }
        return String.valueOf(value).trim();
    }

    private Map<String, Object> normalize(Map<?, ?> item) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        item.forEach((key, value) -> normalized.put(String.valueOf(key), value));
        return normalized;
    }

    private void validateValue(Map<String, Object> row, String value) {
        String type = String.valueOf(row.get("valueType"));
        if (!"INTEGER".equals(type)) {
            throw new BusinessException(400, "지원하지 않는 설정값 유형입니다.", Map.of("valueType", "지원하지 않는 유형입니다."));
        }
        int parsed;
        try {
            parsed = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(400, "설정값 유형을 확인해 주세요.", Map.of("settingValue", "정수로 입력해 주세요."));
        }
        Integer min = parseNullable(row.get("minValue"));
        Integer max = parseNullable(row.get("maxValue"));
        if ((min != null && parsed < min) || (max != null && parsed > max)) {
            String message = (min == null ? "" : min + " 이상 ") + (max == null ? "" : max + " 이하") + "로 입력해 주세요.";
            throw new BusinessException(400, "설정값 범위를 확인해 주세요.", Map.of("settingValue", message.trim()));
        }
    }

    private Integer parseNullable(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private BusinessException notFound() {
        return new BusinessException(404, "환경설정 항목을 찾을 수 없습니다.", Map.of("settingKey", "존재하지 않거나 비활성 항목입니다."));
    }

    private record PendingUpdate(String settingKey, String settingValue) {}
}
