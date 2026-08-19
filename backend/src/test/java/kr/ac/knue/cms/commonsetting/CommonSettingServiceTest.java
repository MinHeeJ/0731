package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.common.ChangeHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommonSettingServiceTest {
    @Mock CommonSettingMapper mapper;
    @Mock ChangeHistoryService history;
    @InjectMocks CommonSettingService service;

    @Test
    void get_common_settings_returns_mapper_items_without_user_or_business_scope() {
        when(mapper.listGlobalSettings()).thenReturn(CommonSettingContractTest.seedItems());

        CommonSettingsResponse response = service.getCommonSettings();

        assertThat(response.items()).hasSize(5);
        assertThat(response.items()).extracting(CommonSettingItem::getSettingKey)
            .containsExactly("sessionIdleMinutes", "pageSize", "defaultSearchPeriodDays", "bulkQueryThresholdCount", "longRunningTaskNoticeSeconds");
        assertThat(response.items()).allMatch(item -> "GLOBAL".equals(item.getScopeType()));
    }

    @Test
    void update_common_settings_rejects_unit_or_value_type_mismatch_before_persistence_update() {
        List<CommonSettingItem> rows = CommonSettingContractTest.seedItems();
        rows.get(0).setUnitCode("COUNT");
        when(mapper.listGlobalSettings()).thenReturn(rows);

        assertThatThrownBy(() -> service.updateCommonSettings(validBody()))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).fields()).containsKey("sessionIdleMinutes"));

        verify(mapper, never()).updateSettingValue(anyString(), anyString(), any());
        verify(history, never()).record(anyString(), anyString(), anyString(), any(), any(), any(), any());
    }

    @Test
    void update_common_settings_rejects_user_or_business_scope_payload() {
        Map<String, Object> body = validBody();
        body.put("businessCategory", "교무");

        assertThatThrownBy(() -> service.updateCommonSettings(body))
            .isInstanceOf(BusinessException.class)
            .satisfies(ex -> assertThat(((BusinessException) ex).fields()).containsKey("businessCategory"));

        verify(mapper, never()).updateSettingValue(anyString(), anyString(), any());
    }

    @Test
    void update_common_settings_persists_each_setting_and_records_audit_rows() {
        when(mapper.listGlobalSettings()).thenReturn(CommonSettingContractTest.seedItems(), CommonSettingContractTest.updatedItems());
        when(mapper.updateSettingValue(anyString(), anyString(), any())).thenReturn(1);

        CommonSettingsResponse response = service.updateCommonSettings(validBody());

        assertThat(response.items()).hasSize(5);
        verify(mapper).updateSettingValue("pageSize", "50", "운영 기준 변경");
        verify(history).record("system_common_setting", "pageSize", "UPDATE", "20", "50", "system", "운영 기준 변경");
    }

    static Map<String, Object> validBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sessionIdleMinutes", 30);
        body.put("pageSize", 50);
        body.put("defaultSearchPeriodDays", 7);
        body.put("bulkQueryThresholdCount", 1000);
        body.put("longRunningTaskNoticeSeconds", 60);
        body.put("changeReason", "운영 기준 변경");
        return body;
    }
}
