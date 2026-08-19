package kr.ac.knue.cms.health;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ContractMetadataMapper {
    @Select("""
        SELECT area as \"area\", contract as \"contract\", canonical_id as \"canonicalId\"
        FROM technology_stack
        WHERE use_yn = 'Y'
        ORDER BY area
        """)
    List<Map<String, Object>> listTechnologyStack();

    @Select("""
        SELECT bom_key as \"key\", version_value as \"version\", canonical_id as \"canonicalId\"
        FROM version_b_o_m
        WHERE use_yn = 'Y'
        ORDER BY bom_key
        """)
    List<Map<String, Object>> listVersionBom();

    @Select("""
        SELECT output_key as \"key\", output_value as \"value\", canonical_id as \"canonicalId\"
        FROM required_outputs
        WHERE use_yn = 'Y'
        ORDER BY output_key
        """)
    List<Map<String, Object>> listRequiredOutputs();

    @Select("""
        SELECT
          scope_id as \"scopeId\",
          feature_code as \"featureCode\",
          feature_name as \"featureName\",
          screen_id as \"screenId\",
          route_path as \"routePath\",
          api_path as \"apiPath\",
          primary_entity as \"primaryEntity\",
          status as \"status\"
        FROM c_m_s_1401
        WHERE use_yn = 'Y'
        ORDER BY feature_code, screen_id
        """)
    List<Map<String, Object>> listCms1401Scope();
}
