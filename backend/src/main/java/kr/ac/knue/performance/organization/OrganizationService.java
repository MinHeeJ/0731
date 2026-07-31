package kr.ac.knue.performance.organization;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.ac.knue.performance.common.error.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {
  private final OrganizationMapper organizationMapper;

  public OrganizationService(OrganizationMapper organizationMapper) {
    this.organizationMapper = organizationMapper;
  }

  public List<OrganizationDto> list(String organizationCode, String organizationName, UUID parentOrganizationId) {
    return organizationMapper.list(blank(organizationCode), blank(organizationName), parentOrganizationId);
  }

  @Transactional
  public OrganizationDto updateRelation(UUID relationId, OrganizationRequests.Relation request) {
    if (request.effectiveEndDate() != null && request.effectiveEndDate().isBefore(request.effectiveStartDate())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "적용 종료일은 시작일보다 빠를 수 없습니다.", Map.of("effectiveEndDate", "시작일 이후여야 합니다."));
    }
    if (organizationMapper.updateRelation(relationId, request.organizationId(), request.parentOrganizationId(), request.effectiveStartDate(), request.effectiveEndDate()) == 0) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "NOT_FOUND", "조직 관계를 찾을 수 없습니다.");
    }
    return organizationMapper.findByRelation(relationId);
  }

  private String blank(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
