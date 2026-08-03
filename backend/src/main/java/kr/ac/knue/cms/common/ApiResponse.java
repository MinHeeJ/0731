package kr.ac.knue.cms.common;

public record ApiResponse<T>(boolean success, T data, ApiError error, Pagination pagination) {
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, data, null, null); }
    public static <T> ApiResponse<T> page(T data, Pagination pagination) { return new ApiResponse<>(true, data, null, pagination); }
    public static <T> ApiResponse<T> fail(ApiError error) { return new ApiResponse<>(false, null, error, null); }
}
