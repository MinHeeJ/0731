package kr.ac.knue.performance.code;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record CodeDetailDto(String groupId, String codeValue, String codeName, String parentCodeValue, Integer sortOrder, JsonNode extraAttributes, @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate validFrom, @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDate validTo, boolean isActive) {}
