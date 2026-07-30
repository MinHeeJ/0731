package kr.ac.knue.cms.common.domain;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "계정을 입력하세요.") String loginId,
    @NotBlank(message = "비밀번호를 입력하세요.") String password
) {}
