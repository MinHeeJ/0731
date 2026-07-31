package kr.ac.knue.performance.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String loginId, @NotBlank String password) {}
