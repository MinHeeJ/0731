package kr.ac.knue.cms.user;

import java.util.List;
import java.util.Map;

public interface PersonnelInformationPort {
    List<Map<String, Object>> searchReadonlyPersonnel(String query);
}
