package kr.ac.knue.cms.systemconfig;

import kr.ac.knue.cms.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SystemConfigServiceTest {
    private final SystemConfigMapper mapper = mock(SystemConfigMapper.class);
    private final SystemConfigService service = new SystemConfigService(mapper);

    @Test
    void validates_integer_range_before_update() {
        when(mapper.findByKey("PAGE_SIZE")).thenReturn(config("PAGE_SIZE", "20", "INTEGER", "10", "100", "Y"));

        assertThatThrownBy(() -> service.update("PAGE_SIZE", Map.of("setting_value", "5")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("범위");

        verify(mapper, never()).updateValue(anyString(), anyString(), anyString());
    }

    @Test
    void rejects_integer_string_error_and_inactive_rows() {
        when(mapper.findByKey("PAGE_SIZE")).thenReturn(config("PAGE_SIZE", "20", "INTEGER", "10", "100", "Y"));
        when(mapper.findByKey("DISABLED_KEY")).thenReturn(config("DISABLED_KEY", "20", "INTEGER", "10", "100", "N"));

        assertThatThrownBy(() -> service.update("PAGE_SIZE", Map.of("setting_value", "not-number")))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.update("DISABLED_KEY", Map.of("setting_value", "30")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    void bulk_update_validates_all_items_before_writing() {
        when(mapper.findByKey("PAGE_SIZE")).thenReturn(config("PAGE_SIZE", "20", "INTEGER", "10", "100", "Y"));
        when(mapper.findByKey("SESSION_IDLE_TIMEOUT")).thenReturn(config("SESSION_IDLE_TIMEOUT", "30", "INTEGER", "5", "480", "Y"));

        assertThatThrownBy(() -> service.bulkUpdate(Map.of("items", List.of(
            Map.of("setting_key", "PAGE_SIZE", "setting_value", "50"),
            Map.of("setting_key", "SESSION_IDLE_TIMEOUT", "setting_value", "481")
        )))).isInstanceOf(BusinessException.class);

        verify(mapper, never()).updateValue(anyString(), anyString(), anyString());
    }

    @Test
    void stores_default_value_when_restore_request_sends_default_value() {
        when(mapper.findByKey("LONG_TASK_NOTICE_THRESHOLD")).thenReturn(config("LONG_TASK_NOTICE_THRESHOLD", "20", "INTEGER", "3", "120", "Y"));
        when(mapper.updateValue("LONG_TASK_NOTICE_THRESHOLD", "10", "system")).thenReturn(1);
        when(mapper.findByKey("LONG_TASK_NOTICE_THRESHOLD")).thenReturn(config("LONG_TASK_NOTICE_THRESHOLD", "20", "INTEGER", "3", "120", "Y"), config("LONG_TASK_NOTICE_THRESHOLD", "10", "INTEGER", "3", "120", "Y"));

        Map<String, Object> updated = service.update("LONG_TASK_NOTICE_THRESHOLD", Map.of("setting_value", "10"));

        assertThat(updated.get("settingValue")).isEqualTo("10");
        verify(mapper).updateValue("LONG_TASK_NOTICE_THRESHOLD", "10", "system");
    }

    private static Map<String, Object> config(String key, String value, String type, String min, String max, String useYn) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("settingKey", key);
        row.put("settingValue", value);
        row.put("defaultValue", key.equals("LONG_TASK_NOTICE_THRESHOLD") ? "10" : value);
        row.put("valueType", type);
        row.put("minValue", min);
        row.put("maxValue", max);
        row.put("useYn", useYn);
        return row;
    }
}
