package kr.ac.knue.cms.commonsetting;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommonSettingMapper {
    List<Map<String, Object>> list(@Param("settingKey") String settingKey, @Param("offset") int offset, @Param("size") int size);
    int count(@Param("settingKey") String settingKey);
    int updateValue(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue);
    Map<String, Object> findOne(@Param("settingKey") String settingKey);
    int countOverrideLikeRows();
}
