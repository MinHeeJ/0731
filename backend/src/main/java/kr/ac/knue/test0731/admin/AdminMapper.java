package kr.ac.knue.test0731.admin;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mapper
public interface AdminMapper {
    List<Map<String, Object>> list(@Param("resource") AdminResource resource, @Param("keyword") String keyword, @Param("limit") int limit, @Param("offset") int offset);
    Map<String, Object> find(@Param("resource") AdminResource resource, @Param("id") UUID id);
    int insert(@Param("resource") AdminResource resource, @Param("id") UUID id, @Param("name") String name, @Param("status") String status);
    int update(@Param("resource") AdminResource resource, @Param("id") UUID id, @Param("name") String name, @Param("status") String status);
    int softDelete(@Param("resource") AdminResource resource, @Param("id") UUID id);
    int countReferences(@Param("resource") AdminResource resource, @Param("id") UUID id);
    void insertHistory(@Param("targetTable") String targetTable, @Param("targetId") UUID targetId, @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("changedBy") UUID changedBy, @Param("changeReason") String changeReason);
    void insertTransactionConstraint(@Param("targetTable") String targetTable, @Param("targetId") UUID targetId, @Param("operationType") String operationType, @Param("lockedBy") UUID lockedBy);
}
