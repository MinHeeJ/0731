package kr.ac.knue.cms.phase2;

import kr.ac.knue.cms.common.BusinessException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

final class Phase2Validation {
    private Phase2Validation() {}
    static String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : String.valueOf(value).trim();
    }
    static void require(Map<String, Object> body, String... keys) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String key : keys) if (text(body, key).isBlank()) fields.put(key, "필수값입니다.");
        if (!fields.isEmpty()) throw new BusinessException(400, "입력값을 확인해 주세요.", fields);
    }
    static void period(Map<String, Object> body, String startKey, String endKey) {
        String start = text(body, startKey);
        String end = text(body, endKey);
        if (start.isBlank() || end.isBlank()) return;
        if (LocalDate.parse(end).isBefore(LocalDate.parse(start))) {
            throw new BusinessException(400, "기간을 확인해 주세요.", Map.of(endKey, "종료일은 시작일보다 이전일 수 없습니다."));
        }
    }
}
