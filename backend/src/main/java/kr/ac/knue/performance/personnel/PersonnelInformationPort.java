package kr.ac.knue.performance.personnel;

import java.util.List;

public interface PersonnelInformationPort {
  List<String> readonlySnapshotStaffIds();
}
