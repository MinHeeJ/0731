package kr.ac.knue.cms1344.api;

import java.util.List;

public record ApiResponse(boolean success, Object data, ApiError error) {
  public static ApiResponse ok(Object data) { return new ApiResponse(true, data, null); }
  public static ApiResponse fail(String code, String message) { return new ApiResponse(false, null, new ApiError(code, message, List.of())); }
  public static ApiResponse validation(List<FieldError> fields) { return new ApiResponse(false, null, new ApiError("VALIDATION_ERROR", "입력값을 확인해 주세요.", fields)); }
  public record ApiError(String code, String message, List<FieldError> fieldErrors) { }
  public record FieldError(String field, String message) { }
}
