import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AdminLayout } from "../src/app/AdminLayout";
import { adminRoutes } from "../src/app/routes";
describe("admin navigation", () => {
  it("renders nine management leaf routes and Korean shell", () => {
    render(
      <AdminLayout
        currentUser={{ userId: "u", loginId: "admin", roleCodes: ["R09"] }}
        onLogout={() => undefined}
      >
        <div>content</div>
      </AdminLayout>,
    );
    for (const route of adminRoutes)
      expect(screen.getByText(route.label)).toBeInTheDocument();
    expect(adminRoutes).toHaveLength(9);
    expect(screen.getByText("시스템 관리")).toBeInTheDocument();
  });
});
