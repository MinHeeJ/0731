package kr.ac.knue.test0731.admin;

public record ApiResponse<T>(boolean success, T data, ApiError error) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, data, null); }
    public static ApiResponse<Void> fail(String code, String message) { return new ApiResponse<>(false, null, new ApiError(code, message, java.util.List.of())); }
    public static ApiResponse<Void> field(String code, String message, String field, String reason) { return new ApiResponse<>(false, null, new ApiError(code, message, java.util.List.of(new ApiFieldError(field, reason)))); }
}
record ApiError(String code, String message, java.util.List<ApiFieldError> errors) {}
record ApiFieldError(String field, String reason) {}
