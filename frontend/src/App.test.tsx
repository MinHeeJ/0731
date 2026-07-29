import { describe, expect, test } from "vitest";
import { requiredOutputLabels, screenRoutes } from "./screenContract";

describe("screen contract", () => {
  test("contains login, admin shell, and nine management routes", () => {
    expect(screenRoutes).toContain("/login");
    expect(screenRoutes).toContain("/admin/users");
    expect(screenRoutes).toContain("/admin/code-groups/:groupId/codes");
    expect(
      screenRoutes.filter((route) => route.startsWith("/admin")).length,
    ).toBe(10);
  });

  test("shows generated application required outputs on dashboard", () => {
    expect(requiredOutputLabels).toEqual(
      expect.arrayContaining([
        "backend_dir backend",
        "frontend_dir frontend",
        "compose_file infra/docker-compose.yml",
        "health_endpoint /api/health",
      ]),
    );
  });
});
