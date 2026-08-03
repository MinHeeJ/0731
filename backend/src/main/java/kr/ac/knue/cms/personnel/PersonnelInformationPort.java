package kr.ac.knue.cms.personnel;

import java.util.List;
import java.util.Map;

public interface PersonnelInformationPort {
    List<Map<String, Object>> staffSnapshots();
    List<Map<String, Object>> organizationSnapshots();
}
