package kr.ac.knue.cms.common;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {
    List<Map<String,Object>> listRoles(@Param("f") Map<String,Object> filters);
    int updateRole(@Param("roleCode") String roleCode, @Param("body") Map<String,Object> body);
    void createRole(@Param("body") Map<String,Object> body);
    List<Map<String,Object>> listUserRoles(@Param("f") Map<String,Object> filters);
    void grantUserRole(@Param("body") Map<String,Object> body, @Param("approvedBy") String approvedBy);
    int updateUserRole(@Param("assignmentId") long assignmentId, @Param("body") Map<String,Object> body);
    int revokeUserRole(@Param("assignmentId") long assignmentId);
    List<Map<String,Object>> listMenus(@Param("f") Map<String,Object> filters);
    List<Map<String,Object>> listMyMenus(@Param("userId") String userId);
    int updateMenuParent(@Param("menuId") String menuId, @Param("parentMenuId") String parentMenuId);
    int updateMenuOrder(@Param("menuId") String menuId, @Param("displayOrder") int displayOrder);
    void createMenu(@Param("body") Map<String,Object> body);
    int updateMenu(@Param("menuId") String menuId, @Param("body") Map<String,Object> body);
    List<Map<String,Object>> listMenuPermissions(@Param("f") Map<String,Object> filters);
    void upsertMenuPermission(@Param("body") Map<String,Object> body);
    List<Map<String,Object>> listCodeGroups(@Param("f") Map<String,Object> filters);
    void createCodeGroup(@Param("body") Map<String,Object> body);
    int updateCodeGroup(@Param("groupId") String groupId, @Param("body") Map<String,Object> body);
    List<Map<String,Object>> listDetailCodes(@Param("groupId") String groupId);
    void createDetailCode(@Param("groupId") String groupId, @Param("body") Map<String,Object> body);
    int updateDetailCode(@Param("groupId") String groupId, @Param("codeValue") String codeValue, @Param("body") Map<String,Object> body);
}
