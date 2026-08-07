package kr.ac.knue.cms.common;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> fields, Map<String, String> fieldErrors) {
    public static ApiError of(String message) { return new ApiError("ERROR", message, Map.of(), Map.of()); }
    public static ApiError fields(String message, Map<String, String> fields) { return new ApiError("VALIDATION_ERROR", message, fields, fields); }
}
