package kr.ac.knue.cms.user;

import kr.ac.knue.cms.persistence.CommonMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DatabasePersonnelSnapshotAdapter implements PersonnelInformationPort {
    private final CommonMapper mapper;

    public DatabasePersonnelSnapshotAdapter(CommonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Map<String, Object>> searchReadonlyPersonnel(String query) {
        return mapper.listPersonnelSnapshots(query);
    }
}
