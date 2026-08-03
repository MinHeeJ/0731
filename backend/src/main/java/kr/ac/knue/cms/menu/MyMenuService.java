package kr.ac.knue.cms.menu;

import kr.ac.knue.cms.common.AdminMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MyMenuService {
    private final AdminMapper mapper;

    public MyMenuService(AdminMapper mapper) {
        this.mapper = mapper;
    }

    public List<Map<String, Object>> listMyMenus(String userId) {
        return mapper.listMyMenus(userId);
    }
}
