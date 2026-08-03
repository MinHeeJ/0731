
package kr.ac.knue.cms.db;
import org.junit.jupiter.api.Test;import java.nio.file.*;import static org.assertj.core.api.Assertions.assertThat;
class SeedDataTest{@Test void seed_contains_admin_roles_menus_and_examples() throws Exception{String seed=Files.readString(Path.of("src/main/resources/db/migration/V002__seed_roles_menus_admin.sql"))+Files.readString(Path.of("src/main/resources/db/migration/V003__seed_korus_mock_snapshot.sql"));assertThat(seed).contains("'admin'").contains("'R01'").contains("'R09'").contains("사용자 관리").contains("상세코드 관리").contains("홍길동").contains("CSE");}}
