package kr.ac.knue.cms1344.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiResponse> api(ApiException ex) {
    if ("VALIDATION_ERROR".equals(ex.code())) return ResponseEntity.status(ex.status()).body(ApiResponse.validation(ex.fieldErrors()));
    return ResponseEntity.status(ex.status()).body(ApiResponse.fail(ex.code(), ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse> validation(MethodArgumentNotValidException ex) {
    List<ApiResponse.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
      .map(e -> new ApiResponse.FieldError(e.getField(), e.getDefaultMessage()))
      .toList();
    return ResponseEntity.badRequest().body(ApiResponse.validation(errors));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse> unexpected(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("CONFLICT", "처리 중 오류가 발생했습니다."));
  }
}
