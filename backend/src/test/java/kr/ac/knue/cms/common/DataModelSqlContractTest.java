package kr.ac.knue.cms.common;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class DataModelSqlContractTest {
    @Test
    void schema_declares_korus_personnel_snapshot_contract_table() throws Exception {
        String schema = new ClassPathResource("schema.sql").getContentAsString(StandardCharsets.UTF_8).toLowerCase();
        String migration = new ClassPathResource("db/migration/V1__common_foundation.sql").getContentAsString(StandardCharsets.UTF_8).toLowerCase();

        assertThat(schema).contains("create table if not exists korus_personnel_snapshot");
        assertThat(migration).contains("create table if not exists korus_personnel_snapshot");
        assertThat(schema).contains("comment on table korus_personnel_snapshot");
        assertThat(schema).contains("create index if not exists idx_korus_personnel_snapshot_search");
    }
}
