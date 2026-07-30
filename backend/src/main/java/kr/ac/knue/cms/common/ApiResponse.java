package kr.ac.knue.cms.common;

public record ApiResponse<T>(boolean success, T data, ApiError error) {
  public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, data, null); }
  public static <T> ApiResponse<T> error(String code, String message) { return new ApiResponse<>(false, null, new ApiError(code, message, java.util.List.of())); }
  public static <T> ApiResponse<T> fieldError(String code, String message, String field, String reason) {
    return new ApiResponse<>(false, null, new ApiError(code, message, java.util.List.of(new ApiError.FieldError(field, reason))));
  }
}
record ApiError(String code, String message, java.util.List<FieldError> errors) {
  record FieldError(String field, String reason) {}
}
