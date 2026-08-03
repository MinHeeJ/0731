package kr.ac.knue.cms.common;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMapperSearchFilterSqlContractTest {
    private final String mapperXml = new ClassPathResource("mappers/AdminMapper.xml")
        .getContentAsString(StandardCharsets.UTF_8);

    AdminMapperSearchFilterSqlContractTest() throws Exception {
    }

    @Test
    void list_roles_filters_role_name_with_trim_guard() {
        assertThat(mapperXml).contains("<select id=\"listRoles\"");
        assertThat(mapperXml).contains("roleName.toString().trim()");
        assertThat(mapperXml).contains("role_name LIKE CONCAT('%', #{roleName}, '%')");
    }

    @Test
    void list_user_roles_filters_status_without_null_bound_predicate() {
        assertThat(mapperXml).contains("<select id=\"listUserRoles\"");
        assertThat(mapperXml).contains("status.toString().trim()");
        assertThat(mapperXml).contains("AND status=#{status}");
        assertThat(mapperXml).doesNotContain("IS NULL OR");
    }

    @Test
    void list_menus_filters_structure_and_information_fields() {
        assertThat(mapperXml).contains("<select id=\"listMenus\"");
        assertThat(mapperXml).contains("menu_id LIKE CONCAT('%', #{menuId}, '%')");
        assertThat(mapperXml).contains("menu_name LIKE CONCAT('%', #{menuName}, '%')");
        assertThat(mapperXml).contains("screen_id=#{screenId}");
        assertThat(mapperXml).contains("url LIKE CONCAT('%', #{url}, '%')");
        assertThat(mapperXml).contains("business_category=#{businessCategory}");
    }

    @Test
    void list_code_groups_and_detail_codes_filter_all_screen_conditions() {
        assertThat(mapperXml).contains("group_name LIKE CONCAT('%', #{groupName}, '%')");
        assertThat(mapperXml).contains("managing_department LIKE CONCAT('%', #{managingDepartment}, '%')");
        assertThat(mapperXml).contains("code_value=#{codeValueFilter}");
        assertThat(mapperXml).contains("status=#{statusFilter}");
    }
}
