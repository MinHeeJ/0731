package kr.ac.knue.cms.commonsetting;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommonSettingMapper {
    List<CommonSettingItem> listGlobalSettings();
    int updateSettingValue(@Param("settingKey") String settingKey, @Param("settingValue") String settingValue, @Param("changeReason") String changeReason);
    int countNonGlobalScopes();
    int countScopeColumns();
    int countChangeHistory(@Param("entityName") String entityName);
}
