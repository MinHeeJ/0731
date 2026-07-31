package kr.ac.knue.performance.common.web;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> fields) {
  public static ApiError of(String code, String message) {
    return new ApiError(code, message, Map.of());
  }
  public static ApiError field(String code, String message, String field, String reason) {
    return new ApiError(code, message, Map.of(field, reason));
  }
}
