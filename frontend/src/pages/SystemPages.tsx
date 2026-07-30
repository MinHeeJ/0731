import { AdminScreen } from "./AdminScreen";
import { screens } from "../routes";

function screen(id: string) {
  const found = screens.find((candidate) => candidate.id === id);
  if (!found) throw new Error(`Screen config not found: ${id}`);
  return found;
}

export function UsersPage() {
  return <AdminScreen screen={screen("USR-001")} />;
}

export function OrganizationsPage() {
  return <AdminScreen screen={screen("ORG-001")} />;
}

export function RolesPage() {
  return <AdminScreen screen={screen("ROL-001")} />;
}

export function UserRolesPage() {
  return <AdminScreen screen={screen("URO-001")} />;
}

export function MenuPermissionsPage() {
  return <AdminScreen screen={screen("MPM-001")} />;
}

export function MenuStructurePage() {
  return <AdminScreen screen={screen("MST-001")} />;
}

export function MenuInfoPage() {
  return <AdminScreen screen={screen("MIN-001")} />;
}

export function CodeGroupsPage() {
  return <AdminScreen screen={screen("CGP-001")} />;
}

export function CodeDetailsPage() {
  return <AdminScreen screen={screen("CDT-001")} />;
}
