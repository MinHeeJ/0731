package kr.ac.knue.performance.organization;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrganizationMapper {
  List<OrganizationDto> list(@Param("organizationCode") String organizationCode, @Param("organizationName") String organizationName, @Param("parentOrganizationId") UUID parentOrganizationId);
  int updateRelation(@Param("relationId") UUID relationId, @Param("organizationId") UUID organizationId, @Param("parentOrganizationId") UUID parentOrganizationId, @Param("start") LocalDate start, @Param("end") LocalDate end);
  OrganizationDto findByRelation(@Param("relationId") UUID relationId);
}
