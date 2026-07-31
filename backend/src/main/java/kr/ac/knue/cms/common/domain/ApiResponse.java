package kr.ac.knue.cms.common.domain;

public record ApiResponse<T>(boolean success, T data, ApiError error) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message, java.util.List.of()));
    }

    public static <T> ApiResponse<T> fail(String code, String message, java.util.List<ApiError.FieldError> fieldErrors) {
        return new ApiResponse<>(false, null, new ApiError(code, message, fieldErrors));
    }
}
