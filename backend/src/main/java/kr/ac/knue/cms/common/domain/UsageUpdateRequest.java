package kr.ac.knue.cms.common.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UsageUpdateRequest(
    @NotBlank(message = "사용여부를 선택하세요.") @Pattern(regexp = "Y|N", message = "사용여부는 Y 또는 N이어야 합니다.") String systemUseYn,
    @NotBlank(message = "변경 사유를 입력하세요.") String reason
) {}
