export type Menu = {
  menuId: string;
  parentMenuId?: string;
  menuLevel: number;
  menuName: string;
  screenId?: string;
  url?: string;
  icon?: string;
  businessCategory: string;
  description?: string;
  displayOrder: number;
  useYn: "Y" | "N";
};
export type Role = {
  roleCode: string;
  roleName: string;
  purpose: string;
  grantCriteria: string;
  defaultDataScope: string;
  useYn: "Y" | "N";
};
export type User = {
  userId: string;
  personId: string;
  personName: string;
  organizationName: string;
  jobGrade: string;
  employmentStatus: string;
  positionName: string;
  retiredAt?: string;
  lastSyncedAt: string;
  systemUseYn: "Y" | "N";
  roleCodes: string[];
};
export type Organization = {
  organizationCode: string;
  organizationName: string;
  organizationType: string;
  parentOrganizationCode?: string;
  effectiveStartDate?: string;
  effectiveEndDate?: string;
  useYn: "Y" | "N";
};
export type Assignment = {
  assignmentId: number;
  userId: string;
  roleCode: string;
  grantType: string;
  approverUserId: string;
  validFrom: string;
  validTo?: string;
  status: string;
};
export type Permission = {
  permissionId: number;
  targetType: string;
  targetId: string;
  menuId: string;
  accessYn: "Y" | "N";
};
export type CodeGroup = {
  groupId: string;
  groupName: string;
  description?: string;
  managingDepartment: string;
  useYn: "Y" | "N";
};
export type CommonCode = {
  groupId: string;
  codeValue: string;
  codeName: string;
  parentCodeValue?: string;
  sortOrder: number;
  additionalAttributes?: Record<string, unknown>;
  validFrom: string;
  validTo?: string;
  useYn: "Y" | "N";
};
