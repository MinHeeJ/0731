package kr.ac.knue.cms.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> api(ApiException ex) {
        return ResponseEntity.status(ex.status()).body(ApiResponse.fail(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex) {
        List<ApiError.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ApiError.FieldError(e.getField(), e.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, new ApiError("VALIDATION_ERROR", "입력값을 확인하세요", errors)));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> generic(Exception ex) {
        log.error("Unhandled API exception", ex);
        return ResponseEntity.internalServerError().body(ApiResponse.fail("INTERNAL_ERROR", "요청 처리 중 오류가 발생했습니다"));
    }
}
