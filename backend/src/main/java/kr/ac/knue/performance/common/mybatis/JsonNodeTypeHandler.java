package kr.ac.knue.performance.common.mybatis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(JsonNode.class)
public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter.toString());
  }

  @Override
  public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return parse(rs.getObject(columnName));
  }

  @Override
  public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return parse(rs.getObject(columnIndex));
  }

  @Override
  public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return parse(cs.getObject(columnIndex));
  }

  private JsonNode parse(Object value) throws SQLException {
    if (value == null) {
      return null;
    }
    String json = value instanceof byte[] bytes ? new String(bytes, StandardCharsets.UTF_8) : value.toString();
    try {
      return OBJECT_MAPPER.readTree(json);
    } catch (Exception first) {
      try {
        return OBJECT_MAPPER.readTree(OBJECT_MAPPER.readValue(json, String.class));
      } catch (Exception second) {
        throw new SQLException("JSON 값을 JsonNode로 변환할 수 없습니다: " + json, second);
      }
    }
  }
}
