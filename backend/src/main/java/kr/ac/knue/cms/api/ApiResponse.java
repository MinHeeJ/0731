package kr.ac.knue.cms.api;

public record ApiResponse<T>(boolean success, T data, ApiError error) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message, java.util.List.of()));
    }

    public static ApiResponse<Void> fieldError(String field, String reason) {
        return new ApiResponse<>(false, null, new ApiError("VALIDATION_ERROR", "입력값을 확인하세요", java.util.List.of(new ApiError.FieldError(field, reason))));
    }
}
