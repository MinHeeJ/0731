package kr.ac.knue.cms.common;

import java.util.Map;

public class BusinessException extends RuntimeException {
    private final int status;
    private final Map<String, String> fields;
    public BusinessException(int status, String message) { this(status, message, Map.of()); }
    public BusinessException(int status, String message, Map<String, String> fields) { super(message); this.status = status; this.fields = fields; }
    public int status() { return status; }
    public Map<String, String> fields() { return fields; }
}
