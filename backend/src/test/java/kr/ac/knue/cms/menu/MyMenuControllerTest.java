package kr.ac.knue.cms.menu;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MyMenuController.class)
class MyMenuControllerTest {
    @Autowired MockMvc mvc;
    @MockBean MyMenuService service;

    @Test
    void r09_menu_returns_nine_management_screens() throws Exception {
        when(service.listMyMenus(any())).thenReturn(List.of(
            Map.of("menuName", "사용자 관리"),
            Map.of("menuName", "조직 관리"),
            Map.of("menuName", "역할 관리"),
            Map.of("menuName", "사용자 역할 관리"),
            Map.of("menuName", "메뉴 권한 관리"),
            Map.of("menuName", "메뉴 구조 관리"),
            Map.of("menuName", "메뉴 정보 관리"),
            Map.of("menuName", "코드그룹 관리"),
            Map.of("menuName", "상세코드 관리")
        ));

        mvc.perform(get("/api/menus/my"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(9));
    }
}
