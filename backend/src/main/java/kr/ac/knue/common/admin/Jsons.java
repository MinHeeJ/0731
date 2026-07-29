package kr.ac.knue.common.admin;
import com.fasterxml.jackson.databind.ObjectMapper;
class Jsons { static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules(); static String toJson(Object value) { try { return value == null ? null : MAPPER.writeValueAsString(value); } catch (Exception e) { throw new IllegalArgumentException("JSON 변환 실패"); } } }
