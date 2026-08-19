package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.common.ChangeHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommonSettingController.class)
@Import(CommonSettingService.class)
class CommonSettingContractTest {
    @Autowired MockMvc mvc;
    @MockBean CommonSettingMapper mapper;
    @MockBean ChangeHistoryService history;

    @Test
    void get_common_settings_returns_five_global_items_in_api_response_envelope() throws Exception {
        when(mapper.listGlobalSettings()).thenReturn(seedItems());

        mvc.perform(get("/api/system/common-settings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.items.length()").value(5))
            .andExpect(jsonPath("$.data.items[0].settingKey").value("sessionIdleMinutes"))
            .andExpect(jsonPath("$.data.items[0].unitCode").value("MINUTE"))
            .andExpect(jsonPath("$.data.items[4].settingKey").value("longRunningTaskNoticeSeconds"))
            .andExpect(jsonPath("$.data.items[4].unitCode").value("SECOND"));
    }

    @Test
    void put_common_settings_updates_all_values_and_records_change_history() throws Exception {
        when(mapper.listGlobalSettings()).thenReturn(seedItems(), updatedItems());
        when(mapper.updateSettingValue(anyString(), anyString(), any())).thenReturn(1);

        mvc.perform(put("/api/system/common-settings")
                .contentType("application/json")
                .content("{\"sessionIdleMinutes\":30,\"pageSize\":50,\"defaultSearchPeriodDays\":7,\"bulkQueryThresholdCount\":1000,\"longRunningTaskNoticeSeconds\":60,\"changeReason\":\"운영 기준 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items.length()").value(5))
            .andExpect(jsonPath("$.data.items[1].settingValue").value("50"));

        verify(mapper).updateSettingValue("sessionIdleMinutes", "30", "운영 기준 변경");
        verify(history).record(eq("system_common_setting"), eq("sessionIdleMinutes"), eq("UPDATE"), any(), eq("30"), any(), eq("운영 기준 변경"));
    }

    @Test
    void put_common_settings_rejects_non_integer_and_forbidden_scope_fields_as_field_errors() throws Exception {
        mvc.perform(put("/api/system/common-settings")
                .contentType("application/json")
                .content("{\"sessionIdleMinutes\":\"abc\",\"pageSize\":50,\"defaultSearchPeriodDays\":7,\"bulkQueryThresholdCount\":1000,\"longRunningTaskNoticeSeconds\":60,\"userId\":\"U10001\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields.sessionIdleMinutes").exists())
            .andExpect(jsonPath("$.error.fields.userId").exists());

        verify(mapper, never()).updateSettingValue(anyString(), anyString(), any());
        verify(history, never()).record(anyString(), anyString(), anyString(), any(), any(), any(), any());
    }

    @Test
    void put_common_settings_rejects_overlong_change_reason_and_unknown_payload_property() throws Exception {
        String longReason = "가".repeat(501);
        mvc.perform(put("/api/system/common-settings")
                .contentType("application/json")
                .content("{\"sessionIdleMinutes\":30,\"pageSize\":50,\"defaultSearchPeriodDays\":7,\"bulkQueryThresholdCount\":1000,\"longRunningTaskNoticeSeconds\":60,\"changeReason\":\"" + longReason + "\",\"unexpectedScope\":\"x\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.changeReason").exists())
            .andExpect(jsonPath("$.error.fields.unexpectedScope").exists());
    }

    static List<CommonSettingItem> seedItems() {
        return List.of(
            item("sessionIdleMinutes", "세션 유휴시간", "20", "MINUTE", 1),
            item("pageSize", "페이지당 조회건수", "20", "COUNT", 2),
            item("defaultSearchPeriodDays", "기본 검색기간", "30", "DAY", 3),
            item("bulkQueryThresholdCount", "대량조회 기준건수", "500", "COUNT", 4),
            item("longRunningTaskNoticeSeconds", "장시간작업 안내 기준", "30", "SECOND", 5)
        );
    }

    static List<CommonSettingItem> updatedItems() {
        return List.of(
            item("sessionIdleMinutes", "세션 유휴시간", "30", "MINUTE", 1),
            item("pageSize", "페이지당 조회건수", "50", "COUNT", 2),
            item("defaultSearchPeriodDays", "기본 검색기간", "7", "DAY", 3),
            item("bulkQueryThresholdCount", "대량조회 기준건수", "1000", "COUNT", 4),
            item("longRunningTaskNoticeSeconds", "장시간작업 안내 기준", "60", "SECOND", 5)
        );
    }

    static CommonSettingItem item(String key, String name, String value, String unit, int order) {
        CommonSettingItem item = new CommonSettingItem();
        item.setSettingKey(key);
        item.setSettingName(name);
        item.setSettingValue(value);
        item.setValueType("INTEGER");
        item.setUnitCode(unit);
        item.setScopeType("GLOBAL");
        item.setDisplayOrder(order);
        return item;
    }
}
