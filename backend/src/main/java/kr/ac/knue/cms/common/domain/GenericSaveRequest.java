package kr.ac.knue.cms.common.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenericSaveRequest extends LinkedHashMap<String, Object> {
    public String text(String key) {
        Object value = get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    public Map<String, Object> asMap() {
        return this;
    }
}
