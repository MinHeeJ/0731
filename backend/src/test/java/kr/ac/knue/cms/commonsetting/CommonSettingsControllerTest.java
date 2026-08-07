package kr.ac.knue.cms.commonsetting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommonSettingsController.class)
class CommonSettingsControllerTest {
    @Autowired MockMvc mvc;
    @MockBean CommonSettingsService service;

    private static Map<String, Object> row(String key, String value, String unit, String meaning) {
        return Map.of("settingKey", key, "settingValue", value, "settingUnit", unit, "settingMeaning", meaning);
    }

    @Test
    void get_common_settings_returns_five_current_values_page_contract() throws Exception {
        when(service.list(null, 0, 20)).thenReturn(new CommonSettingsPage(List.of(
            row("sessionIdleTime", "30", "분", "세션 유휴시간"),
            row("pageSize", "20", "건", "페이지당 조회건수"),
            row("defaultSearchPeriod", "30", "일", "기본 검색기간"),
            row("bulkQueryThreshold", "1000", "건", "대량조회 기준건수"),
            row("longRunningTaskNoticeThreshold", "10", "분", "장시간작업 안내 기준")
        ), 0, 20, 5));

        mvc.perform(get("/api/system/common-settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items.length()").value(5))
            .andExpect(jsonPath("$.data.items[0].settingKey").value("sessionIdleTime"))
            .andExpect(jsonPath("$.data.items[4].settingKey").value("longRunningTaskNoticeThreshold"))
            .andExpect(jsonPath("$.data.totalElements").value(5));
        verify(service).list(null, 0, 20);
    }

    @Test
    void put_common_settings_updates_all_allowed_keys_and_returns_requery_contract() throws Exception {
        when(service.update(anyMap())).thenReturn(new CommonSettingsPage(List.of(row("pageSize", "50", "건", "페이지당 조회건수")), 0, 20, 5));

        mvc.perform(put("/api/system/common-settings").contentType("application/json").content("{\"sessionIdleTime\":\"45\",\"pageSize\":\"50\",\"defaultSearchPeriod\":\"14\",\"bulkQueryThreshold\":\"2000\",\"longRunningTaskNoticeThreshold\":\"15\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items[0].settingValue").value("50"));
        verify(service).update(anyMap());
    }

    @Test
    void put_single_setting_routes_to_matching_key_and_returns_common_setting() throws Exception {
        when(service.updateSingle(eq("bulkQueryThreshold"), anyMap())).thenReturn(row("bulkQueryThreshold", "2500", "건", "대량조회 기준건수"));

        mvc.perform(put("/api/system/common-settings/bulk-query-threshold").contentType("application/json").content("{\"bulkQueryThreshold\":\"2500\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.settingKey").value("bulkQueryThreshold"))
            .andExpect(jsonPath("$.data.settingValue").value("2500"));
        verify(service).updateSingle(eq("bulkQueryThreshold"), anyMap());
    }

    @Test
    void helper_operations_return_existing_auth_meaning_unit_and_value_contracts() throws Exception {
        when(service.existingSystemAuth()).thenReturn(Map.of("authContext", "existingSystemAuth"));
        when(service.settingMeaning("pageSize")).thenReturn(Map.of("settingMeaning", "페이지당 조회건수"));
        when(service.settingUnit("pageSize")).thenReturn(Map.of("settingUnit", "건"));
        when(service.settingValue("pageSize")).thenReturn(Map.of("settingValue", "20"));

        mvc.perform(get("/api/system/common-settings/existing-system-auth"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.authContext").value("existingSystemAuth"));
        mvc.perform(get("/api/system/common-settings/setting-meaning").queryParam("settingKey", "pageSize"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.settingMeaning").value("페이지당 조회건수"));
        mvc.perform(get("/api/system/common-settings/setting-unit").queryParam("settingKey", "pageSize"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.settingUnit").value("건"));
        mvc.perform(get("/api/system/common-settings/setting-value").queryParam("settingKey", "pageSize"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.settingValue").value("20"));
    }

    @Test
    void validation_error_returns_field_errors_and_does_not_call_update_when_service_rejects() throws Exception {
        when(service.update(anyMap())).thenThrow(new kr.ac.knue.cms.common.BusinessException(400, "공통 환경설정 값을 확인해 주세요. 기존 값은 유지됩니다.", Map.of("pageSize", "pageSize 항목 의미와 단위에 맞는 숫자 값을 입력해 주세요.")));

        mvc.perform(put("/api/system/common-settings").contentType("application/json").content("{\"pageSize\":\"invalid-unit\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields.pageSize").exists())
            .andExpect(jsonPath("$.error.fieldErrors.pageSize").exists());
        verify(service).update(anyMap());
    }

    @Test
    void override_fields_are_rejected_before_persistence_side_effect() throws Exception {
        when(service.update(anyMap())).thenThrow(new kr.ac.knue.cms.common.BusinessException(400, "공통 환경설정 값을 확인해 주세요. 기존 값은 유지됩니다.", Map.of("userId", "공통 환경설정은 사용자별·업무별 override 또는 metadata 변경을 저장하지 않습니다.")));

        mvc.perform(put("/api/system/common-settings").contentType("application/json").content("{\"pageSize\":\"30\",\"userId\":\"U10001\",\"businessId\":\"BIZ\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fieldErrors.userId").exists());
        verify(service).update(anyMap());
        verify(service, never()).updateSingle(eq("pageSize"), anyMap());
    }
}
