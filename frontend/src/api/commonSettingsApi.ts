import { api, ApiResponse } from "./client";

export type CommonSettingKey =
  | "sessionIdleTime"
  | "pageSize"
  | "defaultSearchPeriod"
  | "bulkQueryThreshold"
  | "longRunningTaskNoticeThreshold";

export type CommonSetting = {
  settingKey: CommonSettingKey;
  settingValue: string;
  settingUnit: string;
  settingMeaning: string;
};

export type CommonSettingsPage = {
  items: CommonSetting[];
  page: number;
  size: number;
  totalElements: number;
};

export type UpdateCommonSettingsRequest = Partial<
  Record<CommonSettingKey, string>
>;

export async function getCommonSettings(): Promise<
  ApiResponse<CommonSettingsPage>
> {
  return api<CommonSettingsPage>("/api/system/common-settings");
}

export async function updateCommonSettings(
  body: UpdateCommonSettingsRequest,
): Promise<ApiResponse<CommonSettingsPage>> {
  return api<CommonSettingsPage>("/api/system/common-settings", {
    method: "PUT",
    body: JSON.stringify(body),
  });
}
