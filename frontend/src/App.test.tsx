import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import App from "./App";

vi.stubGlobal(
  "fetch",
  vi.fn(async (input: RequestInfo | URL) => {
    const path = String(input);
    if (!path.startsWith("/api/"))
      throw new Error("absolute API path is not allowed");
    if (path === "/api/auth/me")
      return new Response(
        JSON.stringify({
          success: true,
          data: { userName: "시스템관리자", roleCodes: ["R09"] },
          error: null,
        }),
        { status: 200 },
      );
    if (path === "/api/admin/me/menus")
      return new Response(
        JSON.stringify({
          success: true,
          data: {
            items: [
              {
                menuId: "M-USERS",
                menuName: "사용자 관리",
                routePath: "/system/users",
              },
            ],
          },
          error: null,
        }),
        { status: 200 },
      );
    if (path.startsWith("/api/admin/users"))
      return new Response(
        JSON.stringify({
          success: true,
          data: {
            items: [
              {
                userId: "USR-1001",
                employeeNo: "1001",
                userName: "김교원",
                orgName: "컴퓨터교육과",
                systemUseYn: "Y",
              },
            ],
          },
          error: null,
        }),
        { status: 200 },
      );
    if (path === "/api/health")
      return new Response(
        JSON.stringify({
          success: true,
          data: {
            status: "UP",
            decisionStatus: "clarification_required",
            checks: [],
          },
          error: null,
        }),
        { status: 200 },
      );
    return new Response(
      JSON.stringify({ success: true, data: {}, error: null }),
      { status: 200 },
    );
  }),
);

describe("App routes", () => {
  it("renders management screen using relative API calls", async () => {
    render(
      <MemoryRouter initialEntries={["/system/users"]}>
        <App />
      </MemoryRouter>,
    );
    expect((await screen.findAllByText("사용자 관리")).length).toBeGreaterThan(
      0,
    );
    expect((await screen.findAllByText("김교원")).length).toBeGreaterThan(0);
  });
});
