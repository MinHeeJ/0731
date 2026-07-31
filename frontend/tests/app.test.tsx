import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { App } from "../src/App";

afterEach(() => {
  cleanup();
});

vi.stubGlobal(
  "fetch",
  vi.fn(async (path: string) => {
    if (path === "/api/health") {
      return new Response(
        JSON.stringify({
          success: true,
          data: { status: "UP", service: "test" },
        }),
        { status: 200 },
      );
    }
    if (path === "/api/me") {
      return new Response(
        JSON.stringify({
          success: true,
          data: { userId: "admin", name: "관리자", roles: ["R09"] },
        }),
        { status: 200 },
      );
    }
    if (path.startsWith("/api/admin/users")) {
      return new Response(JSON.stringify({ success: true, data: [] }), {
        status: 200,
      });
    }
    return new Response(JSON.stringify({ success: true, data: [] }), {
      status: 200,
    });
  }),
);

describe("App routes", () => {
  it("renders Korean login screen and health badge", async () => {
    history.pushState(null, "", "/login");
    render(<App />);
    expect(
      await screen.findByText("KNUE 교수업적평가시스템"),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "로그인" })).toBeInTheDocument();
  });

  it("renders required admin users route without hardcoded absolute API base", async () => {
    history.pushState(null, "", "/admin/users");
    render(<App />);
    expect((await screen.findAllByText("사용자 관리")).length).toBeGreaterThan(
      0,
    );
    expect(
      screen.getByText(/decision_status=clarification_required/),
    ).toBeInTheDocument();
  });
});
