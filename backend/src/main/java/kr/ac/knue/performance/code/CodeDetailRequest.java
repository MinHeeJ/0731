package kr.ac.knue.performance.code;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CodeDetailRequest(@NotBlank String groupId, @NotBlank String codeValue, @NotBlank String codeName, String parentCodeValue, @NotNull Integer sortOrder, JsonNode extraAttributes, LocalDate validFrom, LocalDate validTo, Boolean isActive, String reason) {}
