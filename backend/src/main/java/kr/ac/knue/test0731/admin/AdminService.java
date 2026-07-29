package kr.ac.knue.test0731.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {
    private static final UUID SYSTEM_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final AdminMapper mapper;
    private final ObjectMapper objectMapper;
    public AdminService(AdminMapper mapper, ObjectMapper objectMapper) { this.mapper = mapper; this.objectMapper = objectMapper; }
    public List<Map<String, Object>> list(String slug, String keyword, int page, int size) { return mapper.list(AdminResource.of(slug), blankToNull(keyword), size, page * size); }
    public Map<String, Object> get(String slug, UUID id) { Map<String, Object> found = mapper.find(AdminResource.of(slug), id); if (found == null) { throw new NotFoundException(); } return found; }
    @Transactional
    public Map<String, Object> create(String slug, AdminItem item) {
        AdminResource resource = AdminResource.of(slug); UUID id = UUID.randomUUID(); String status = resource.requestStatus(item);
        mapper.insert(resource, id, item.getName().trim(), status);
        Map<String, Object> after = mapper.find(resource, id);
        recordPersistenceContract(resource, id, "CREATE", null, after, item.getChangeReason().trim());
        return after;
    }
    @Transactional
    public Map<String, Object> update(String slug, UUID id, AdminItem item) {
        AdminResource resource = AdminResource.of(slug); Map<String, Object> before = mapper.find(resource, id);
        if (before == null) { throw new NotFoundException(); }
        mapper.update(resource, id, item.getName().trim(), resource.requestStatus(item));
        Map<String, Object> after = mapper.find(resource, id);
        recordPersistenceContract(resource, id, "UPDATE", before, after, item.getChangeReason().trim());
        return after;
    }
    @Transactional
    public void delete(String slug, UUID id, String reason) {
        AdminResource resource = AdminResource.of(slug); Map<String, Object> before = mapper.find(resource, id);
        if (before == null) { throw new NotFoundException(); }
        if (mapper.countReferences(resource, id) > 0) { throw new BusinessException("참조 중인 항목은 삭제할 수 없습니다"); }
        mapper.softDelete(resource, id);
        Map<String, Object> after = mapper.find(resource, id);
        recordPersistenceContract(resource, id, "DELETE", before, after, reason == null || reason.isBlank() ? "삭제" : reason.trim());
    }
    private void recordPersistenceContract(AdminResource resource, UUID targetId, String operationType, Map<String, Object> beforeValue, Map<String, Object> afterValue, String changeReason) {
        mapper.insertHistory(resource.tableName(), targetId, json(beforeValue), json(afterValue), SYSTEM_ADMIN_ID, changeReason);
        mapper.insertTransactionConstraint(resource.tableName(), targetId, operationType, SYSTEM_ADMIN_ID);
    }
    private String json(Object value) { if (value == null) { return null; } try { return objectMapper.writeValueAsString(value); } catch (JsonProcessingException e) { throw new IllegalStateException("변경 이력 직렬화 실패", e); } }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
class NotFoundException extends RuntimeException {}
class BusinessException extends RuntimeException { BusinessException(String message) { super(message); } }
