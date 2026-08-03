package kr.ac.knue.cms.common;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChangeHistoryMapper {
    void insert(@Param("entityName") String entityName, @Param("entityKey") String entityKey, @Param("operationType") String operationType,
                @Param("beforeValue") String beforeValue, @Param("afterValue") String afterValue, @Param("changedBy") String changedBy,
                @Param("changeReason") String changeReason);
}
