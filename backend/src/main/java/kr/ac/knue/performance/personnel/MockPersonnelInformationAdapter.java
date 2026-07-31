package kr.ac.knue.performance.personnel;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockPersonnelInformationAdapter implements PersonnelInformationPort {
  @Override
  public List<String> readonlySnapshotStaffIds() {
    return List.of("KORUS-ADMIN");
  }
}
