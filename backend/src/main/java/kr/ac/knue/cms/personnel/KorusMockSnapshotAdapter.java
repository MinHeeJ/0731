package kr.ac.knue.cms.personnel;

import kr.ac.knue.cms.user.UserMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class KorusMockSnapshotAdapter implements PersonnelInformationPort {
    private final UserMapper userMapper;
    public KorusMockSnapshotAdapter(UserMapper userMapper) { this.userMapper = userMapper; }
    public List<Map<String, Object>> staffSnapshots() { return userMapper.listUsers(Map.of()); }
    public List<Map<String, Object>> organizationSnapshots() { return userMapper.listOrganizations(Map.of()); }
}
