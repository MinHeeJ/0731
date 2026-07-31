package kr.ac.knue.cms.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommonApiContractTest {
    @Autowired MockMvc mvc;
    ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("health returns delivery status without authentication")
    void health_returns_delivery_status() throws Exception {
        mvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("UP"))
            .andExpect(jsonPath("$.data.decisionStatus").value("clarification_required"));
    }

    @Test
    @DisplayName("admin seed login exposes current session and all nine target menus")
    void admin_login_and_menus_follow_contract() throws Exception {
        String session = login();
        mvc.perform(get("/api/auth/me").cookie(new jakarta.servlet.http.Cookie("SESSION", session)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleCodes[0]").value("R09"));
        MvcResult menus = mvc.perform(get("/api/admin/me/menus").cookie(new jakarta.servlet.http.Cookie("SESSION", session)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();
        JsonNode items = mapper.readTree(menus.getResponse().getContentAsString()).path("data").path("items");
        long leafRoutes = 0;
        for (JsonNode item : items) {
            if (item.hasNonNull("routePath") && item.path("routePath").asText().startsWith("/system/")) leafRoutes++;
        }
        assertThat(leafRoutes).isGreaterThanOrEqualTo(9);
    }

    @Test
    @DisplayName("protected admin APIs reject unauthenticated requests")
    void protected_admin_requires_session() throws Exception {
        mvc.perform(get("/api/admin/users"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("user search returns KORUS readonly fields and local usage update records validation")
    void users_search_and_usage_validation() throws Exception {
        String session = login();
        mvc.perform(get("/api/admin/users?filter=김교원").cookie(new jakarta.servlet.http.Cookie("SESSION", session)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.items[0].employeeNo").value("1001"))
            .andExpect(jsonPath("$.data.items[0].personnelSource").value("KORUS_MOCK"))
            .andExpect(jsonPath("$.data.items[0].lastSyncedAt").exists());
        mvc.perform(patch("/api/admin/users/USR-1001/usage")
                .cookie(new jakarta.servlet.http.Cookie("SESSION", session))
                .contentType("application/json")
                .content("{\"systemUseYn\":\"INVALID\",\"reason\":\"검증\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error.fieldErrors[0].field").value("systemUseYn"));
    }

    @Test
    @DisplayName("role, menu, permission and code APIs expose persisted seed data")
    void admin_reference_groups_return_persisted_data() throws Exception {
        String session = login();
        mvc.perform(get("/api/admin/roles").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[8].roleCode").value("R09"));
        mvc.perform(get("/api/admin/organizations").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].orgCode").value("KNUE"));
        mvc.perform(get("/api/admin/user-roles").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].assignmentSource").exists());
        mvc.perform(get("/api/admin/menu-permissions?targetType=ROLE&targetId=R09").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].canAccess").value("Y"));
        mvc.perform(get("/api/admin/menus/tree").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].menuId").value("SYS"));
        mvc.perform(get("/api/admin/menus").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].useYn").value("Y"));
        mvc.perform(get("/api/admin/code-groups").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].groupId").exists());
        mvc.perform(get("/api/admin/code-groups/SYSTEM_STATUS/codes").cookie(new jakarta.servlet.http.Cookie("SESSION", session))).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[0].codeValue").value("ACTIVE"));
    }

    @Test
    @DisplayName("write APIs preserve identifiers and return API envelopes")
    void representative_admin_write_flows_return_contract_envelopes() throws Exception {
        String session = login();
        mvc.perform(put("/api/admin/roles/R09").cookie(new jakarta.servlet.http.Cookie("SESSION", session)).contentType("application/json").content("{\"roleName\":\"시스템관리자\",\"purpose\":\"관리\",\"grantCriteria\":\"지정\",\"defaultDataScope\":\"ALL\",\"reason\":\"테스트\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.roleCode").value("R09"));
        mvc.perform(put("/api/admin/users/USR-1001/roles").cookie(new jakarta.servlet.http.Cookie("SESSION", session)).contentType("application/json").content("{\"roleCodes\":[\"R01\"],\"reason\":\"업무 역할 조정\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.data.roleCodes[0]").value("R01"));
        mvc.perform(post("/api/admin/code-groups").cookie(new jakarta.servlet.http.Cookie("SESSION", session)).contentType("application/json").content("{\"groupId\":\"TEST_GROUP\",\"groupName\":\"테스트그룹\",\"managingDepartment\":\"교수지원과\",\"useYn\":\"Y\",\"reason\":\"테스트\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.data.groupId").value("TEST_GROUP"));
    }

    private String login() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"loginId\":\"admin\",\"password\":\"admin\"}"))
            .andExpect(status().isCreated())
            .andExpect(cookie().exists("SESSION"))
            .andExpect(jsonPath("$.success").value(true))
            .andReturn();
        return result.getResponse().getCookie("SESSION").getValue();
    }
}
