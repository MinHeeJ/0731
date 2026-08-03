package kr.ac.knue.cms.health;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ContractMetadataMapper contractMetadataMapper;

    @Test
    void health_returns_api_response_envelope_with_db_contract_metadata() throws Exception {
        when(contractMetadataMapper.listTechnologyStack()).thenReturn(List.of(Map.of("area", "Backend", "contract", "Java 17", "canonicalId", "REQ-032")));
        when(contractMetadataMapper.listVersionBom()).thenReturn(List.of(Map.of("key", "spring_boot", "version", "3.3.x", "canonicalId", "REQ-045")));
        when(contractMetadataMapper.listRequiredOutputs()).thenReturn(List.of(Map.of("key", "health_endpoint", "value", "/api/health", "canonicalId", "REQ-046")));

        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.technologyStack[0].area").value("Backend"))
            .andExpect(jsonPath("$.data.versionBom[0].key").value("spring_boot"))
            .andExpect(jsonPath("$.data.requiredOutputs[0].value").value("/api/health"));
    }
}
