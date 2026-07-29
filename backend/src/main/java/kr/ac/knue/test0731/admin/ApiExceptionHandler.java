package kr.ac.knue.test0731.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiResponse<Void>> notFound() { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("NOT_FOUND", "대상을 찾을 수 없습니다")); }
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> business(BusinessException ex) { return ResponseEntity.badRequest().body(ApiResponse.fail("BUSINESS_ERROR", ex.getMessage())); }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> invalidPath(MethodArgumentTypeMismatchException ex) { return ResponseEntity.badRequest().body(ApiResponse.field("VALIDATION_ERROR", "입력값을 확인하세요", ex.getName(), "형식이 올바르지 않습니다")); }
    @ExceptionHandler({IllegalArgumentException.class})
    ResponseEntity<ApiResponse<Void>> badRequest(Exception ex) { return ResponseEntity.badRequest().body(ApiResponse.fail("BAD_REQUEST", ex.getMessage())); }
}
