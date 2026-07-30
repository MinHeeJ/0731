package kr.ac.knue.cms.common;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiFixtureTest {
    @Test
    void openapi_contract_fixture_is_available_on_classpath() throws Exception {
        ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
        assertThat(resource.exists()).isTrue();
        assertThat(resource.getContentAsString(java.nio.charset.StandardCharsets.UTF_8)).contains("/api/auth/login", "/api/admin/users", "/api/health");
    }
}
