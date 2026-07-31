package kr.ac.knue.performance.menu;

import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MenuMapper {
  List<MenuDto> list(@Param("parentMenuId") UUID parentMenuId, @Param("screenId") String screenId, @Param("menuName") String menuName);
  void insert(@Param("request") MenuRequest request);
  int update(@Param("menuId") UUID menuId, @Param("request") MenuRequest request);
  MenuDto findLatestByName(@Param("menuName") String menuName);
  MenuDto find(@Param("menuId") UUID menuId);
  List<MenuPermissionDto> listPermissions(@Param("targetType") String targetType, @Param("targetId") String targetId);
  int updatePermission(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("menuId") UUID menuId, @Param("allowed") boolean allowed);
  void insertPermission(@Param("targetType") String targetType, @Param("targetId") String targetId, @Param("menuId") UUID menuId, @Param("allowed") boolean allowed);
}
