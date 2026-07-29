package kr.ac.knue.common.api;

import java.util.List;

public record ApiResponse<T>(boolean success, T data, ApiError error, Pagination pagination) {
  public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, data, null, null); }
  public static ApiResponse<Object> fail(String code, String message) { return new ApiResponse<>(false, null, new ApiError(code, message, List.of()), null); }
  public static ApiResponse<Object> validation(List<FieldErrorItem> errors) { return new ApiResponse<>(false, null, new ApiError("VALIDATION_ERROR", "입력값을 확인해 주세요.", errors), null); }
  public record ApiError(String code, String message, List<FieldErrorItem> fieldErrors) {}
  public record FieldErrorItem(String field, String message) {}
  public record Pagination(int page, int size, long totalElements, int totalPages) {}
}
