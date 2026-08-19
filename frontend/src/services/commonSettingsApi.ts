import { api, ApiResponse } from "../api/client";

export type CommonSettingItem = {
  settingKey: string;
  settingName: string;
  settingValue: string;
  valueType: "INTEGER";
  unitCode: "MINUTE" | "COUNT" | "DAY" | "SECOND";
  scopeType: "GLOBAL";
  displayOrder?: number;
};

export type CommonSettingsResponse = {
  items: CommonSettingItem[];
};

export type CommonSettingsUpdateRequest = {
  sessionIdleMinutes: number;
  pageSize: number;
  defaultSearchPeriodDays: number;
  bulkQueryThresholdCount: number;
  longRunningTaskNoticeSeconds: number;
  changeReason?: string;
};

export function getCommonSettings(): Promise<
  ApiResponse<CommonSettingsResponse>
> {
  return api<CommonSettingsResponse>("/api/system/common-settings");
}

export function updateCommonSettings(
  body: CommonSettingsUpdateRequest,
): Promise<ApiResponse<CommonSettingsResponse>> {
  return api<CommonSettingsResponse>("/api/system/common-settings", {
    method: "PUT",
    body: JSON.stringify(body),
  });
}
