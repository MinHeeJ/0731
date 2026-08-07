package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.common.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonSettingsServiceTest {
    CommonSettingMapper mapper = mock(CommonSettingMapper.class);
    CommonSettingsService service = new CommonSettingsService(mapper);

    @Test
    void list_reads_db_seeded_five_settings_with_dynamic_filter() {
        when(mapper.count(null)).thenReturn(5);
        when(mapper.list(null, 0, 20)).thenReturn(List.of(
            Map.of("settingKey", "sessionIdleTime", "settingValue", "30", "settingUnit", "분", "settingMeaning", "세션 유휴시간"),
            Map.of("settingKey", "pageSize", "settingValue", "20", "settingUnit", "건", "settingMeaning", "페이지당 조회건수"),
            Map.of("settingKey", "defaultSearchPeriod", "settingValue", "30", "settingUnit", "일", "settingMeaning", "기본 검색기간"),
            Map.of("settingKey", "bulkQueryThreshold", "settingValue", "1000", "settingUnit", "건", "settingMeaning", "대량조회 기준건수"),
            Map.of("settingKey", "longRunningTaskNoticeThreshold", "settingValue", "10", "settingUnit", "분", "settingMeaning", "장시간작업 안내 기준")
        ));

        CommonSettingsPage page = service.list(null, 0, 20);

        assertThat(page.items()).extracting(row -> row.get("settingKey")).containsExactly("sessionIdleTime", "pageSize", "defaultSearchPeriod", "bulkQueryThreshold", "longRunningTaskNoticeThreshold");
        assertThat(page.totalElements()).isEqualTo(5);
        verify(mapper).list(null, 0, 20);
    }

    @Test
    void update_persists_each_allowed_key_and_requeries_current_values() {
        when(mapper.count(null)).thenReturn(5);
        when(mapper.list(null, 0, 20)).thenReturn(List.of(Map.of("settingKey", "pageSize", "settingValue", "50", "settingUnit", "건", "settingMeaning", "페이지당 조회건수")));

        CommonSettingsPage page = service.update(Map.of(
            "sessionIdleTime", "45",
            "pageSize", "50",
            "defaultSearchPeriod", "14",
            "bulkQueryThreshold", "2000",
            "longRunningTaskNoticeThreshold", "15"
        ));

        verify(mapper).updateValue("sessionIdleTime", "45");
        verify(mapper).updateValue("pageSize", "50");
        verify(mapper).updateValue("defaultSearchPeriod", "14");
        verify(mapper).updateValue("bulkQueryThreshold", "2000");
        verify(mapper).updateValue("longRunningTaskNoticeThreshold", "15");
        assertThat(page.items()).hasSize(1);
    }

    @Test
    void invalid_page_size_and_default_search_period_keep_existing_values() {
        assertThatThrownBy(() -> service.update(Map.of("pageSize", "invalid-unit", "defaultSearchPeriod", "30days")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("기존 값은 유지");
        verify(mapper, never()).updateValue(anyString(), anyString());
    }

    @Test
    void user_or_business_override_fields_are_not_persisted() {
        assertThatThrownBy(() -> service.update(Map.of("pageSize", "30", "userId", "U10001", "businessId", "BIZ")))
            .isInstanceOf(BusinessException.class);
        verify(mapper, never()).updateValue(anyString(), anyString());
    }

    @Test
    void metadata_and_session_or_batch_strategy_fields_are_boundary_rejected() {
        assertThatThrownBy(() -> service.update(Map.of("sessionIdleTime", "40", "settingUnit", "시간", "sessionStorage", "redis")))
            .isInstanceOf(BusinessException.class);
        verify(mapper, never()).updateValue(anyString(), anyString());
    }
}
