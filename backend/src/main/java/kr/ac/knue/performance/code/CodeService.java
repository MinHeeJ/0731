package kr.ac.knue.performance.code;

import java.util.List;
import java.util.Map;
import kr.ac.knue.performance.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeService {
  private final CodeMapper codeMapper;

  public CodeService(CodeMapper codeMapper) {
    this.codeMapper = codeMapper;
  }

  public List<CodeGroupDto> listGroups(String groupId, String groupName, String managingDepartment) {
    return codeMapper.groups(blank(groupId), blank(groupName), blank(managingDepartment));
  }

  @Transactional
  public CodeGroupDto createGroup(CodeGroupRequest request) {
    if (codeMapper.updateGroup(request.groupId(), request) == 0) {
      codeMapper.insertGroup(request);
    }
    return codeMapper.findGroup(request.groupId());
  }

  @Transactional
  public CodeGroupDto updateGroup(String groupId, CodeGroupRequest request) {
    if (!groupId.equals(request.groupId())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "코드그룹 ID는 변경할 수 없습니다.", Map.of("groupId", "path와 body가 일치해야 합니다."));
    }
    codeMapper.updateGroup(groupId, request);
    return codeMapper.findGroup(groupId);
  }

  public List<CodeDetailDto> listDetails(String groupId, String parentCodeValue) {
    return codeMapper.details(blank(groupId), blank(parentCodeValue));
  }

  @Transactional
  public CodeDetailDto createDetail(CodeDetailRequest request) {
    validateParentCode(request);
    codeMapper.insertDetail(request, json(request));
    return codeMapper.findDetail(request.groupId(), request.codeValue());
  }

  @Transactional
  public CodeDetailDto updateDetail(String groupId, String codeValue, CodeDetailRequest request) {
    if (!groupId.equals(request.groupId()) || !codeValue.equals(request.codeValue())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "상세코드 식별자는 변경할 수 없습니다.", Map.of("codeValue", "path와 body가 일치해야 합니다."));
    }
    validateParentCode(request);
    codeMapper.updateDetail(groupId, codeValue, request, json(request));
    return codeMapper.findDetail(groupId, codeValue);
  }

  private void validateParentCode(CodeDetailRequest request) {
    if (request.codeValue().equals(request.parentCodeValue())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "자기 자신을 상위코드로 지정할 수 없습니다.", Map.of("parentCodeValue", "자기 자신은 선택할 수 없습니다."));
    }
  }

  private String blank(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private String json(CodeDetailRequest request) {
    return request.extraAttributes() == null ? "{}" : request.extraAttributes().toString();
  }
}
