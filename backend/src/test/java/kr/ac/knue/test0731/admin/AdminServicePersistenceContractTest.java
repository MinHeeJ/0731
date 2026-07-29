package kr.ac.knue.test0731.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminServicePersistenceContractTest {
    @Test
    void createRecordsChangeHistoryAndMappingTransactionLockConstraintInOrder() {
        AdminMapper mapper = mock(AdminMapper.class);
        AdminService service = new AdminService(mapper, new ObjectMapper());
        AdminItem item = new AdminItem();
        item.setName("계약 검증 사용자");
        item.setChangeReason("DB SQL 계약 보강 검증");
        item.setUseStatus("ENABLED");
        when(mapper.find(eq(AdminResource.of("users")), any(UUID.class)))
            .thenReturn(Map.of("id", UUID.randomUUID(), "name", "계약 검증 사용자", "status", "ENABLED"));

        service.create("users", item);

        var ordered = inOrder(mapper);
        ordered.verify(mapper).insert(eq(AdminResource.of("users")), any(UUID.class), eq("계약 검증 사용자"), eq("ENABLED"));
        ordered.verify(mapper).find(eq(AdminResource.of("users")), any(UUID.class));
        ordered.verify(mapper).insertHistory(eq("local_user_account"), any(UUID.class), isNull(), any(String.class), any(UUID.class), eq("DB SQL 계약 보강 검증"));
        ordered.verify(mapper).insertTransactionConstraint(eq("local_user_account"), any(UUID.class), eq("CREATE"), any(UUID.class));
    }
}
