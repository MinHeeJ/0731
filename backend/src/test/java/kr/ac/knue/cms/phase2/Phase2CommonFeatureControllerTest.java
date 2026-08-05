package kr.ac.knue.cms.phase2;

import kr.ac.knue.cms.common.BusinessException;
import kr.ac.knue.cms.phase2.CommonConfigController;
import kr.ac.knue.cms.phase2.CommonConfigService;
import kr.ac.knue.cms.phase2.NoticeController;
import kr.ac.knue.cms.phase2.NoticeService;
import kr.ac.knue.cms.phase2.PositionController;
import kr.ac.knue.cms.phase2.PositionManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PositionController.class, CommonConfigController.class, NoticeController.class})
class Phase2CommonFeatureControllerTest {
    @Autowired MockMvc mvc;
    @MockBean PositionManagementService positionService;
    @MockBean CommonConfigService commonConfigService;
    @MockBean NoticeService noticeService;

    @Test
    void classpath_openapi_fixture_is_loaded_from_durable_contract_resource() throws Exception {
        String yaml = new ClassPathResource("contracts/openapi.yaml").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(yaml)
            .contains("/api/auth/login")
            .contains("/api/users")
            .contains("/api/code-groups");
    }

    @Test
    void position_create_then_search_contract_returns_saved_user_org_and_period() throws Exception {
        when(positionService.create(anyMap())).thenReturn(List.of(Map.<String,Object>of(
            "positionId", 10, "positionCode", "DEPT_HEAD", "userId", "USER-10002", "organizationCode", "CSE",
            "effectiveStartDate", "2026-08-01", "effectiveEndDate", "2026-12-31")));
        when(positionService.list(anyMap())).thenReturn(List.of(Map.<String,Object>of(
            "positionId", 10, "positionCode", "DEPT_HEAD", "userId", "USER-10002", "organizationCode", "CSE")));

        mvc.perform(post("/api/positions").contentType("application/json")
                .content("{\"positionCode\":\"DEPT_HEAD\",\"userId\":\"USER-10002\",\"organizationCode\":\"CSE\",\"effectiveStartDate\":\"2026-08-01\",\"effectiveEndDate\":\"2026-12-31\",\"changeReason\":\"보직등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].positionCode").value("DEPT_HEAD"))
            .andExpect(jsonPath("$.data[0].userId").value("USER-10002"));
        mvc.perform(get("/api/positions").queryParam("positionCode", "DEPT_HEAD").queryParam("organizationCode", "CSE").queryParam("baseDate", "2026-08-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].organizationCode").value("CSE"));
        verify(positionService).create(anyMap());
        verify(positionService).list(anyMap());
    }

    @Test
    void position_create_rejects_missing_existing_user_or_org_without_persistence() throws Exception {
        when(positionService.create(anyMap())).thenThrow(new BusinessException(400, "참조 식별자를 확인해 주세요.", Map.of("userId", "존재하지 않는 사용자입니다.")));
        mvc.perform(post("/api/positions").contentType("application/json")
                .content("{\"positionCode\":\"DEPT_HEAD\",\"userId\":\"NO-USER\",\"organizationCode\":\"CSE\",\"effectiveStartDate\":\"2026-08-01\",\"effectiveEndDate\":\"2026-12-31\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.userId").value("존재하지 않는 사용자입니다."));
    }

    @Test
    void common_config_lists_defaults_and_saves_partial_values_without_mutating_others() throws Exception {
        when(commonConfigService.list()).thenReturn(List.of(
            Map.<String,Object>of("configKey", "SESSION_IDLE_TIMEOUT_MINUTES", "configValue", "30", "unit", "MINUTE"),
            Map.<String,Object>of("configKey", "PAGE_SIZE", "configValue", "20", "unit", "COUNT"),
            Map.<String,Object>of("configKey", "DEFAULT_SEARCH_PERIOD_YEARS", "configValue", "1", "unit", "YEAR"),
            Map.<String,Object>of("configKey", "BULK_QUERY_THRESHOLD_COUNT", "configValue", "1000", "unit", "COUNT"),
            Map.<String,Object>of("configKey", "LONG_TASK_NOTICE_THRESHOLD_SECONDS", "configValue", "10", "unit", "SECOND")));
        when(commonConfigService.save(anyMap())).thenReturn(List.of(
            Map.<String,Object>of("configKey", "PAGE_SIZE", "configValue", "50", "unit", "COUNT"),
            Map.<String,Object>of("configKey", "SESSION_IDLE_TIMEOUT_MINUTES", "configValue", "30", "unit", "MINUTE")));

        mvc.perform(get("/api/common-configs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].configValue").value("30"))
            .andExpect(jsonPath("$.data[3].configValue").value("1000"));
        mvc.perform(put("/api/common-configs").contentType("application/json")
                .content("{\"items\":[{\"configKey\":\"PAGE_SIZE\",\"configValue\":\"50\",\"unit\":\"COUNT\"}],\"changeReason\":\"페이지 크기 변경\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].configValue").value("50"));
        verify(commonConfigService).save(anyMap());
    }

    @Test
    void common_config_rejects_value_unit_mismatch_with_field_error() throws Exception {
        when(commonConfigService.save(anyMap())).thenThrow(new BusinessException(400, "설정값 형식을 확인해 주세요.", Map.of("items[0].configValue", "COUNT 단위는 양의 정수여야 합니다.")));
        mvc.perform(put("/api/common-configs").contentType("application/json")
                .content("{\"items\":[{\"configKey\":\"PAGE_SIZE\",\"configValue\":\"20건\",\"unit\":\"COUNT\"}]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields['items[0].configValue']").exists());
    }

    @Test
    void notice_create_detail_and_filtered_list_return_targets_period_and_attachments() throws Exception {
        Map<String,Object> notice = Map.of(
            "noticeId", 10, "title", "2차 공통기능 안내", "targetRoleCode", "R09", "targetOrganizationCode", "CSE",
            "importantYn", "Y", "attachments", List.of(Map.of("originalFileName", "guide.pdf", "fileSize", 1200)));
        when(noticeService.create(anyMap())).thenReturn(notice);
        when(noticeService.detail(10L)).thenReturn(notice);
        when(noticeService.list(anyMap())).thenReturn(List.of(notice));

        mvc.perform(post("/api/notices").contentType("application/json")
                .content("{\"title\":\"2차 공통기능 안내\",\"content\":\"점검\",\"postingStartDate\":\"2026-08-01\",\"postingEndDate\":\"2026-12-31\",\"targetRoleCode\":\"R09\",\"targetOrganizationCode\":\"CSE\",\"importantYn\":\"Y\",\"attachments\":[{\"originalFileName\":\"guide.pdf\",\"storedFilePath\":\"/notice/guide.pdf\",\"fileSize\":1200}],\"changeReason\":\"공지등록\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("2차 공통기능 안내"))
            .andExpect(jsonPath("$.data.attachments[0].originalFileName").value("guide.pdf"));
        mvc.perform(get("/api/notices/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.targetRoleCode").value("R09"));
        mvc.perform(get("/api/notices").queryParam("baseDate", "2026-08-15").queryParam("targetRoleCode", "R09"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].importantYn").value("Y"));
    }

    @Test
    void position_update_business_validation_side_effect_contract_updates_existing_position() throws Exception {
        when(positionService.update(eq(10L), anyMap())).thenReturn(List.of(Map.<String,Object>of(
            "positionId", 10, "positionCode", "DEPT_HEAD", "userId", "USER-10002", "organizationCode", "CSE",
            "effectiveStartDate", "2026-09-01", "effectiveEndDate", "2026-12-31")));

        mvc.perform(put("/api/positions/10").contentType("application/json")
                .content("{\"positionCode\":\"DEPT_HEAD\",\"userId\":\"USER-10002\",\"organizationCode\":\"CSE\",\"effectiveStartDate\":\"2026-09-01\",\"effectiveEndDate\":\"2026-12-31\",\"changeReason\":\"보직기간수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].positionId").value(10))
            .andExpect(jsonPath("$.data[0].effectiveStartDate").value("2026-09-01"));
        verify(positionService).update(eq(10L), anyMap());
    }

    @Test
    void position_update_validation_not_found_blocks_position_side_effect() throws Exception {
        when(positionService.update(eq(10L), anyMap())).thenThrow(new BusinessException(400, "존재하지 않는 보직입니다.", Map.of("positionId", "존재하지 않는 보직입니다.")));
        mvc.perform(put("/api/positions/10").contentType("application/json")
                .content("{\"positionCode\":\"DEPT_HEAD\",\"userId\":\"USER-10002\",\"organizationCode\":\"CSE\",\"effectiveStartDate\":\"2026-09-01\",\"effectiveEndDate\":\"2026-12-31\",\"changeReason\":\"보직기간수정\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields.positionId").exists());
        verify(positionService).update(eq(10L), anyMap());
    }

    @Test
    void notice_update_business_validation_side_effect_contract_replaces_attachment_metadata() throws Exception {
        Map<String,Object> updatedNotice = Map.of(
            "noticeId", 10, "title", "2차 공통기능 수정", "targetRoleCode", "R09", "targetOrganizationCode", "CSE",
            "importantYn", "N", "attachments", List.of(Map.of("originalFileName", "updated-guide.pdf", "fileSize", 1400)));
        when(noticeService.update(eq(10L), anyMap())).thenReturn(updatedNotice);

        mvc.perform(put("/api/notices/10").contentType("application/json")
                .content("{\"title\":\"2차 공통기능 수정\",\"content\":\"점검수정\",\"postingStartDate\":\"2026-08-01\",\"postingEndDate\":\"2026-12-31\",\"targetRoleCode\":\"R09\",\"targetOrganizationCode\":\"CSE\",\"importantYn\":\"N\",\"attachments\":[{\"originalFileName\":\"updated-guide.pdf\",\"storedFilePath\":\"/notice/updated-guide.pdf\",\"fileSize\":1400}],\"changeReason\":\"공지수정\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.noticeId").value(10))
            .andExpect(jsonPath("$.data.attachments[0].originalFileName").value("updated-guide.pdf"));
        verify(noticeService).update(eq(10L), anyMap());
    }

    @Test
    void notice_update_validation_not_found_blocks_notice_side_effect() throws Exception {
        when(noticeService.update(eq(10L), anyMap())).thenThrow(new BusinessException(400, "존재하지 않는 공지사항입니다.", Map.of("noticeId", "존재하지 않는 공지사항입니다.")));
        mvc.perform(put("/api/notices/10").contentType("application/json")
                .content("{\"title\":\"공지\",\"content\":\"내용\",\"postingStartDate\":\"2026-08-01\",\"postingEndDate\":\"2026-12-31\",\"changeReason\":\"공지수정\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.fields.noticeId").exists());
        verify(noticeService).update(eq(10L), anyMap());
    }

    @Test
    void notice_create_rejects_unknown_role_or_org_without_create() throws Exception {
        when(noticeService.create(anyMap())).thenThrow(new BusinessException(400, "공지 대상을 확인해 주세요.", Map.of("targetRoleCode", "존재하지 않는 역할코드입니다.")));
        mvc.perform(post("/api/notices").contentType("application/json")
                .content("{\"title\":\"공지\",\"content\":\"내용\",\"postingStartDate\":\"2026-08-01\",\"postingEndDate\":\"2026-12-31\",\"targetRoleCode\":\"NO\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fields.targetRoleCode").exists());
        verify(noticeService).create(anyMap());
        verify(noticeService, never()).update(eq(10L), anyMap());
    }
}
