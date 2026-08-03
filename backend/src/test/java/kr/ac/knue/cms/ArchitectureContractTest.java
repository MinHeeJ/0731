package kr.ac.knue.cms;
import org.junit.jupiter.api.Test;import java.nio.file.*;import static org.assertj.core.api.Assertions.assertThat;
class ArchitectureContractTest{@Test void uses_mybatis_without_jpa_or_r2dbc() throws Exception{String pom=Files.readString(Path.of("pom.xml"));assertThat(pom).contains("mybatis-spring-boot-starter");assertThat(pom).doesNotContain("spring-boot-starter-data-jpa").doesNotContain("spring-boot-starter-data-r2dbc");}}
