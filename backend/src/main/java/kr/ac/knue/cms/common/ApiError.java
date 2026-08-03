package kr.ac.knue.cms.common;

import java.util.Map;

public record ApiError(String message, Map<String, String> fields) {
    public static ApiError of(String message) { return new ApiError(message, Map.of()); }
    public static ApiError fields(String message, Map<String, String> fields) { return new ApiError(message, fields); }
}
