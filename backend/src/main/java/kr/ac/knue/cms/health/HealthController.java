package kr.ac.knue.cms.health;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import kr.ac.knue.cms.common.ApiResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private final ObjectProvider<ContractMetadataMapper> contractMetadataMapper;

    public HealthController(ObjectProvider<ContractMetadataMapper> contractMetadataMapper) {
        this.contractMetadataMapper = contractMetadataMapper;
    }

    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "cms-common-foundation");
        body.put("timestamp", OffsetDateTime.now().toString());
        ContractMetadataMapper mapper = contractMetadataMapper.getIfAvailable();
        if (mapper != null) {
            body.put("technologyStack", mapper.listTechnologyStack());
            body.put("versionBom", mapper.listVersionBom());
            body.put("requiredOutputs", mapper.listRequiredOutputs());
        }
        return ApiResponse.ok(body);
    }
}
