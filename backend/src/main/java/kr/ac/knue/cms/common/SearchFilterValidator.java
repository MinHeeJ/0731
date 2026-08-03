package kr.ac.knue.cms.common;

import java.util.Map;
import java.util.Set;

public final class SearchFilterValidator {
    private SearchFilterValidator() {
    }

    public static void requireOneOf(Map<String, Object> filters, String field, Set<String> allowedValues) {
        Object rawValue = filters.get(field);
        if (rawValue == null) {
            return;
        }
        String value = rawValue.toString().trim();
        if (value.isEmpty() || allowedValues.contains(value)) {
            return;
        }
        throw new BusinessException(400, "검색조건이 올바르지 않습니다.", Map.of(field, "허용값: " + String.join("|", allowedValues)));
    }
}
