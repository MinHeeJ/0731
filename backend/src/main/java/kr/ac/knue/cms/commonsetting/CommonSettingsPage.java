package kr.ac.knue.cms.commonsetting;

import java.util.List;
import java.util.Map;

public record CommonSettingsPage(List<Map<String, Object>> items, int page, int size, int totalElements) {
}
