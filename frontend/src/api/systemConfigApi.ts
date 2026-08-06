import { api } from "./client";

export type SystemConfig = {
  configId?: number;
  settingKey: string;
  settingName: string;
  settingValue: string;
  valueType: string;
  unit: string;
  defaultValue: string;
  minValue?: string;
  maxValue?: string;
  description: string;
  useYn?: string;
  updatedAt?: string;
  updatedBy?: string;
};

export const listSystemConfigs = () =>
  api<SystemConfig[]>("/api/system-config");

export const updateSystemConfig = (settingKey: string, settingValue: string) =>
  api<SystemConfig>(`/api/system-config/${encodeURIComponent(settingKey)}`, {
    method: "PUT",
    body: JSON.stringify({ setting_value: settingValue }),
  });
