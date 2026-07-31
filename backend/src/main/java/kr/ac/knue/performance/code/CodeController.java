package kr.ac.knue.performance.code;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeController {
  private final CodeService codeService;

  public CodeController(CodeService codeService) {
    this.codeService = codeService;
  }

  @GetMapping("/api/code-groups")
  ApiResponse<List<CodeGroupDto>> groups(@RequestParam(required = false) String groupId, @RequestParam(required = false) String groupName, @RequestParam(required = false) String managingDepartment, HttpServletRequest request) {
    return ApiResponse.ok(codeService.listGroups(groupId, groupName, managingDepartment), requestId(request));
  }

  @PostMapping("/api/code-groups")
  ApiResponse<CodeGroupDto> createGroup(@Valid @RequestBody CodeGroupRequest body, HttpServletRequest request) {
    return ApiResponse.ok(codeService.createGroup(body), requestId(request));
  }

  @PutMapping("/api/code-groups/{groupId}")
  ApiResponse<CodeGroupDto> updateGroup(@PathVariable String groupId, @Valid @RequestBody CodeGroupRequest body, HttpServletRequest request) {
    return ApiResponse.ok(codeService.updateGroup(groupId, body), requestId(request));
  }

  @GetMapping("/api/code-details")
  ApiResponse<List<CodeDetailDto>> details(@RequestParam(required = false) String groupId, @RequestParam(required = false) String parentCodeValue, HttpServletRequest request) {
    return ApiResponse.ok(codeService.listDetails(groupId, parentCodeValue), requestId(request));
  }

  @PostMapping("/api/code-details")
  ApiResponse<CodeDetailDto> createDetail(@Valid @RequestBody CodeDetailRequest body, HttpServletRequest request) {
    return ApiResponse.ok(codeService.createDetail(body), requestId(request));
  }

  @PutMapping("/api/code-details/{groupId}/{codeValue}")
  ApiResponse<CodeDetailDto> updateDetail(@PathVariable String groupId, @PathVariable String codeValue, @Valid @RequestBody CodeDetailRequest body, HttpServletRequest request) {
    return ApiResponse.ok(codeService.updateDetail(groupId, codeValue, body), requestId(request));
  }

  private String requestId(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE));
  }
}
