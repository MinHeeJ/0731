package kr.ac.knue.cms.common;
import org.junit.jupiter.api.Test;import java.util.Map;import static org.assertj.core.api.Assertions.assertThat;
class ApiResponseContractTest{@Test void error_envelope_contains_field_errors(){ApiResponse<Void> r=ApiResponse.fail(ApiError.fields("입력값을 확인해 주세요.", Map.of("loginId","필수")));assertThat(r.success()).isFalse();assertThat(r.error().fields()).containsKey("loginId");}}
