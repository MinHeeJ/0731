package kr.ac.knue.common.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(AppException.class)
  ResponseEntity<ApiResponse<Object>> app(AppException ex) { return ResponseEntity.status(ex.status()).body(ApiResponse.fail(ex.code(), ex.getMessage())); }
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Object>> invalid(MethodArgumentNotValidException ex) {
    List<ApiResponse.FieldErrorItem> fields = ex.getBindingResult().getFieldErrors().stream().map(e -> new ApiResponse.FieldErrorItem(e.getField(), e.getDefaultMessage())).toList();
    return ResponseEntity.badRequest().body(ApiResponse.validation(fields));
  }
  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ApiResponse<Object>> invalidConstraint(ConstraintViolationException ex) { return ResponseEntity.badRequest().body(ApiResponse.fail("VALIDATION_ERROR", "입력값을 확인해 주세요.")); }
  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Object>> unhandled(Exception ex) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("INTERNAL_ERROR", "처리 중 오류가 발생했습니다.")); }
  public static class AppException extends RuntimeException {
    private final HttpStatus status; private final String code;
    public AppException(HttpStatus status, String code, String message) { super(message); this.status = status; this.code = code; }
    public HttpStatus status() { return status; }
    public String code() { return code; }
  }
}
