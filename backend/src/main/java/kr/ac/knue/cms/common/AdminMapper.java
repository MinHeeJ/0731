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

    List<Map<String,Object>> listPositions(@Param("f") Map<String,Object> filters);
    void insertPosition(@Param("body") Map<String,Object> body);
    int updatePosition(@Param("positionId") long positionId, @Param("body") Map<String,Object> body);
    int countUser(@Param("userId") String userId);
    int countOrganization(@Param("organizationCode") String organizationCode);
    int countRole(@Param("roleCode") String roleCode);
    List<Map<String,Object>> listCommonConfigs();
    void upsertCommonConfig(@Param("body") Map<String,Object> body, @Param("changeReason") String changeReason);
    List<Map<String,Object>> listNotices(@Param("f") Map<String,Object> filters, @Param("userId") String userId);
    Map<String,Object> getNotice(@Param("noticeId") long noticeId);
    void insertNotice(@Param("body") Map<String,Object> body, @Param("createdBy") String createdBy);
    int updateNotice(@Param("noticeId") long noticeId, @Param("body") Map<String,Object> body);
    List<Map<String,Object>> listNoticeAttachments(@Param("noticeId") long noticeId);
    void deleteNoticeAttachments(@Param("noticeId") long noticeId);
    void insertNoticeAttachment(@Param("noticeId") long noticeId, @Param("body") Map<String,Object> body);
}
