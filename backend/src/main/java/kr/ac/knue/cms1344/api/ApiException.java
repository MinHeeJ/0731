package kr.ac.knue.cms1344.api;

import java.util.List;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
  private final HttpStatus status;
  private final String code;
  private final List<ApiResponse.FieldError> fieldErrors;

  public ApiException(HttpStatus status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
    this.fieldErrors = List.of();
  }

  public ApiException(List<ApiResponse.FieldError> fieldErrors) {
    super("입력값을 확인해 주세요.");
    this.status = HttpStatus.BAD_REQUEST;
    this.code = "VALIDATION_ERROR";
    this.fieldErrors = fieldErrors;
  }

  public HttpStatus status() { return status; }
  public String code() { return code; }
  public List<ApiResponse.FieldError> fieldErrors() { return fieldErrors; }
}
