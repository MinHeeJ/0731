package kr.ac.knue.cms.common;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.assertj.core.api.Assertions.assertThat;

class CommonFoundationContractTests {
  @Test
  void openapiFixtureContainsAllRequiredOperationGroups() throws Exception {
    String yaml = new String(new ClassPathResource("contracts/openapi.yaml").getInputStream().readAllBytes());
    assertThat(yaml).contains("operationId: login", "operationId: listUsers", "operationId: updateUserAccount");
    assertThat(yaml).contains("operationId: getOrganizationTree", "operationId: updateOrganizationRelation");
    assertThat(yaml).contains("operationId: listRoles", "operationId: grantUserRole", "operationId: saveMenuPermissions");
    assertThat(yaml).contains("operationId: getMenuTree", "operationId: createMenu", "operationId: createCodeGroup", "operationId: createCodeDetail");
  }
}
