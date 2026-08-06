package kr.ac.knue.cms.systemconfig;

import kr.ac.knue.cms.common.BusinessException;
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

@WebMvcTest(SystemConfigController.class)
class SystemConfigControllerTest {
    @Autowired MockMvc mvc;
    @MockBean SystemConfigService service;

    @Test
    void list_system_configs_returns_five_seed_items_for_authenticated_contract() throws Exception {
        when(service.list()).thenReturn(List.of(
            Map.of("settingKey", "SESSION_IDLE_TIMEOUT", "settingName", "세션 유휴시간", "settingValue", "30", "unit", "분", "defaultValue", "30", "description", "세션 유휴시간"),
            Map.of("settingKey", "PAGE_SIZE", "settingName", "페이지당 조회건수", "settingValue", "20", "unit", "건", "defaultValue", "20", "description", "페이지당 조회건수"),
            Map.of("settingKey", "DEFAULT_SEARCH_PERIOD", "settingName", "기본 검색기간", "settingValue", "30", "unit", "일", "defaultValue", "30", "description", "기본 검색기간"),
            Map.of("settingKey", "BULK_QUERY_THRESHOLD", "settingName", "대량조회 기준건수", "settingValue", "1000", "unit", "건", "defaultValue", "1000", "description", "대량조회 기준건수"),
            Map.of("settingKey", "LONG_TASK_NOTICE_THRESHOLD", "settingName", "장시간작업 안내 기준", "settingValue", "10", "unit", "초", "defaultValue", "10", "description", "장시간작업 안내 기준")
        ));

        mvc.perform(get("/api/system-config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.length()").value(5))
            .andExpect(jsonPath("$.data[1].settingKey").value("PAGE_SIZE"))
            .andExpect(jsonPath("$.data[1].unit").value("건"));
    }

    @Test
    void get_system_config_returns_page_size_and_missing_key_404() throws Exception {
        when(service.get("PAGE_SIZE")).thenReturn(Map.of("settingKey", "PAGE_SIZE", "settingValue", "20", "defaultValue", "20"));
        when(service.get("NO_SUCH_KEY")).thenThrow(new BusinessException(404, "환경설정 항목을 찾을 수 없습니다.", Map.of("settingKey", "존재하지 않거나 비활성 항목입니다.")));

        mvc.perform(get("/api/system-config/PAGE_SIZE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.settingKey").value("PAGE_SIZE"));
        mvc.perform(get("/api/system-config/NO_SUCH_KEY"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error.fields.settingKey").exists());
    }

    @Test
    void update_system_config_saves_single_value_and_returns_updated_row() throws Exception {
        when(service.update(eq("PAGE_SIZE"), anyMap())).thenReturn(Map.of("settingKey", "PAGE_SIZE", "settingValue", "50", "updatedBy", "admin"));

        mvc.perform(put("/api/system-config/PAGE_SIZE").contentType("application/json").content("{\"setting_value\":\"50\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.settingValue").value("50"))
            .andExpect(jsonPath("$.data.updatedBy").value("admin"));

        verify(service).update(eq("PAGE_SIZE"), anyMap());
    }

    @Test
    void update_system_config_validation_errors_do_not_call_successful_persistence_path() throws Exception {
        when(service.update(eq("PAGE_SIZE"), anyMap()))
            .thenThrow(new BusinessException(400, "설정값 범위를 확인해 주세요.", Map.of("settingValue", "10 이상 100 이하로 입력해 주세요.")));

        mvc.perform(put("/api/system-config/PAGE_SIZE").contentType("application/json").content("{\"setting_value\":\"5\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.settingValue").exists());
    }

    @Test
    void bulk_update_system_configs_is_all_or_nothing_contract() throws Exception {
        when(service.bulkUpdate(anyMap())).thenReturn(List.of(
            Map.of("settingKey", "PAGE_SIZE", "settingValue", "50"),
            Map.of("settingKey", "SESSION_IDLE_TIMEOUT", "settingValue", "60")
        ));

        mvc.perform(put("/api/system-config").contentType("application/json").content("{\"items\":[{\"setting_key\":\"PAGE_SIZE\",\"setting_value\":\"50\"},{\"setting_key\":\"SESSION_IDLE_TIMEOUT\",\"setting_value\":\"60\"}]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(2))
            .andExpect(jsonPath("$.data[0].settingValue").value("50"));

        verify(service).bulkUpdate(anyMap());
    }

    @Test
    void bulk_update_rejects_empty_items_before_service_side_effect() throws Exception {
        mvc.perform(put("/api/system-config").contentType("application/json").content("{\"items\":[]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.items").exists());

        verify(service, never()).bulkUpdate(anyMap());
    }
}
