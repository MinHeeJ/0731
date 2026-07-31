package kr.ac.knue.performance.common.error;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import kr.ac.knue.performance.common.web.ApiError;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiResponse<Void>> api(ApiException ex, HttpServletRequest request) {
    return ResponseEntity.status(ex.status()).body(ApiResponse.fail(new ApiError(ex.code(), ex.getMessage(), ex.fields()), requestId(request)));
  }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) fields.put(error.getField(), error.getDefaultMessage());
    return ResponseEntity.badRequest().body(ApiResponse.fail(new ApiError("VALIDATION_ERROR", "입력값을 확인해 주세요.", fields), requestId(request)));
  }
  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> unknown(Exception ex, HttpServletRequest request) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(ApiError.of("INTERNAL_ERROR", "요청 처리 중 오류가 발생했습니다."), requestId(request)));
  }
  private String requestId(HttpServletRequest request) { return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE)); }
}
