package kr.ac.knue.cms.systemconfig;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SystemConfigMapper {
    List<Map<String, Object>> listActive();
    Map<String, Object> findByKey(@Param("settingKey") String settingKey);
    int updateValue(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue, @Param("updatedBy") String updatedBy);
}
