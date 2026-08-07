package kr.ac.knue.cms.commonsetting;

import kr.ac.knue.cms.auth.SessionAuthenticationInterceptor;
import kr.ac.knue.cms.auth.SessionService;
import kr.ac.knue.cms.auth.SessionUser;
import kr.ac.knue.cms.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommonSettingsController.class)
@Import({SecurityConfig.class, SessionAuthenticationInterceptor.class})
class CommonSettingsAuthenticationContractTest {
    @Autowired MockMvc mvc;
    @MockBean CommonSettingsService service;
    @MockBean SessionService sessionService;

    @Test
    void update_requires_existing_system_auth_before_common_setting_side_effect() throws Exception {
        mvc.perform(put("/api/system/common-settings").contentType("application/json").content("{\"pageSize\":\"50\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(service, never()).update(anyMap());
    }

    @Test
    void update_forbidden_for_non_admin_existing_auth_and_keeps_existing_value() throws Exception {
        when(sessionService.resolve("S-R01")).thenReturn(new SessionUser("U10002", "staff", "staff", "일반사용자", List.of("R01")));

        mvc.perform(put("/api/system/common-settings")
                .cookie(new jakarta.servlet.http.Cookie("SESSION", "S-R01"))
                .contentType("application/json")
                .content("{\"pageSize\":\"50\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
        verify(service, never()).update(anyMap());
    }
}
