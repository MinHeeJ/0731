package kr.ac.knue.performance.organization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import kr.ac.knue.performance.common.web.ApiResponse;
import kr.ac.knue.performance.common.web.RequestIdFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationController {
  private final OrganizationService organizationService;

  public OrganizationController(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @GetMapping("/api/organizations")
  ApiResponse<List<OrganizationDto>> list(@RequestParam(required = false) String organizationCode, @RequestParam(required = false) String organizationName, @RequestParam(required = false) UUID parentOrganizationId, HttpServletRequest request) {
    return ApiResponse.ok(organizationService.list(organizationCode, organizationName, parentOrganizationId), requestId(request));
  }

  @PutMapping("/api/organization-relations/{relationId}")
  ApiResponse<OrganizationDto> update(@PathVariable UUID relationId, @Valid @RequestBody OrganizationRequests.Relation body, HttpServletRequest request) {
    return ApiResponse.ok(organizationService.updateRelation(relationId, body), requestId(request));
  }

  private String requestId(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(RequestIdFilter.ATTRIBUTE));
  }
}
