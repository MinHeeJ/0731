package kr.ac.knue.performance.code;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CodeMapper {
  List<CodeGroupDto> groups(@Param("groupId") String groupId, @Param("groupName") String groupName, @Param("managingDepartment") String managingDepartment);
  void insertGroup(CodeGroupRequest request);
  int updateGroup(@Param("groupId") String groupId, @Param("request") CodeGroupRequest request);
  CodeGroupDto findGroup(@Param("groupId") String groupId);
  List<CodeDetailDto> details(@Param("groupId") String groupId, @Param("parentCodeValue") String parentCodeValue);
  void insertDetail(@Param("request") CodeDetailRequest request, @Param("extraAttributesJson") String extraAttributesJson);
  int updateDetail(@Param("groupId") String groupId, @Param("codeValue") String codeValue, @Param("request") CodeDetailRequest request, @Param("extraAttributesJson") String extraAttributesJson);
  CodeDetailDto findDetail(@Param("groupId") String groupId, @Param("codeValue") String codeValue);
}
