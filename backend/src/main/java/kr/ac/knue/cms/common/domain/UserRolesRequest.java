package kr.ac.knue.cms.common.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UserRolesRequest(
    @NotEmpty(message = "역할을 하나 이상 선택하세요.") List<String> roleCodes,
    @NotBlank(message = "변경 사유를 입력하세요.") String reason
) {}
