package kr.ac.knue.cms.systemconfig;

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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemConfigController.class)
@Import({SecurityConfig.class, SessionAuthenticationInterceptor.class})
class SystemConfigAuthenticationContractTest {
    @Autowired MockMvc mvc;
    @MockBean SystemConfigService service;
    @MockBean SessionService sessionService;

    @Test
    void get_system_config_requires_authenticated_session_before_read() throws Exception {
        mvc.perform(get("/api/system-config"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));

        verify(service, never()).list();
    }

    @Test
    void authenticated_non_r09_can_read_system_config() throws Exception {
        when(sessionService.resolve("S-R01")).thenReturn(new SessionUser("S-R01", "u1", "u1", "교원", List.of("R01")));

        mvc.perform(get("/api/system-config").cookie(new jakarta.servlet.http.Cookie("SESSION", "S-R01")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(service).list();
    }

    @Test
    void authenticated_non_r09_cannot_update_system_config() throws Exception {
        when(sessionService.resolve("S-R01")).thenReturn(new SessionUser("S-R01", "u1", "u1", "교원", List.of("R01")));

        mvc.perform(put("/api/system-config/PAGE_SIZE").cookie(new jakarta.servlet.http.Cookie("SESSION", "S-R01")).contentType("application/json").content("{\"setting_value\":\"50\"}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));

        verify(service, never()).update(anyString(), org.mockito.ArgumentMatchers.anyMap());
    }
}
