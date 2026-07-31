package kr.ac.knue.performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ContractFixtureTest {
  @Test
  void openApiFixtureIsAvailableOnClasspath() throws Exception {
    ClassPathResource resource = new ClassPathResource("contracts/openapi.yaml");
    assertThat(resource.exists()).isTrue();
    assertThat(resource.getContentAsString(StandardCharsets.UTF_8)).contains("/api/auth/login", "ApiResponse");
  }
}
