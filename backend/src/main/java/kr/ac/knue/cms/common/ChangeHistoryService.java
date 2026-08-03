package kr.ac.knue.cms.common;

import org.springframework.stereotype.Service;

@Service
public class ChangeHistoryService {
    private final ChangeHistoryMapper mapper;
    public ChangeHistoryService(ChangeHistoryMapper mapper) { this.mapper = mapper; }
    public void record(String entityName, String entityKey, String operationType, String beforeValue, String afterValue, String changedBy, String reason) {
        mapper.insert(entityName, entityKey, operationType, beforeValue, afterValue, changedBy == null ? "system" : changedBy, reason);
    }
}
