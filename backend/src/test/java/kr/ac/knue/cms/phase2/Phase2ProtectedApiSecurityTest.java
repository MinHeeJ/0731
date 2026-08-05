package kr.ac.knue.cms.phase2;

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

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {PositionController.class, CommonConfigController.class, NoticeController.class})
@Import({SecurityConfig.class, SessionAuthenticationInterceptor.class})
class Phase2ProtectedApiSecurityTest {
    @Autowired MockMvc mvc;
    @MockBean SessionService sessionService;
    @MockBean PositionManagementService positionService;
    @MockBean CommonConfigService commonConfigService;
    @MockBean NoticeService noticeService;

    @Test
    void phase2_protected_apis_return_401_without_session_before_services() throws Exception {
        mvc.perform(get("/api/positions"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/common-configs"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        mvc.perform(post("/api/notices").contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
        verify(positionService, never()).list(org.mockito.ArgumentMatchers.anyMap());
        verify(commonConfigService, never()).list();
        verify(noticeService, never()).create(org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void phase2_protected_apis_return_403_for_authenticated_user_without_r09() throws Exception {
        when(sessionService.resolve("SESSION-NO-R09")).thenReturn(new SessionUser("USER-10003", "USER-10003", "kim", "김교원", List.of("R01")));
        mvc.perform(get("/api/positions").cookie(new jakarta.servlet.http.Cookie("SESSION", "SESSION-NO-R09")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false));
        verify(sessionService).resolve("SESSION-NO-R09");
        verify(positionService, never()).list(org.mockito.ArgumentMatchers.anyMap());
    }
}
