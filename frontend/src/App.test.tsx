import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, test, vi, beforeEach } from "vitest";
import { App } from "./App";

describe("공통기능 frontend routes", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "fetch",
      vi.fn(async (path: string) => {
        if (path === "/api/health")
          return json({ success: true, data: { status: "UP" } });
        if (path === "/api/auth/login")
          return json({ success: true, data: { userId: "admin" } });
        if (path === "/api/me/menus")
          return json({
            success: true,
            data: [{ url: "/admin/users", menuName: "사용자 관리" }],
          });
        return json({
          success: true,
          data: [
            {
              userId: "admin",
              name: "관리자",
              useYn: "Y",
              positionName: "시스템관리자",
            },
          ],
        });
      }),
    );
    window.history.pushState({}, "", "/login");
  });

  test("로그인 후 사용자 관리 화면으로 이동하고 sidebar/header를 렌더링한다", async () => {
    render(<App />);
    await screen.findByText(/200 정상/);
    await userEvent.click(screen.getByRole("button", { name: "로그인" }));
    expect(
      await screen.findByRole("heading", { name: "사용자 관리" }),
    ).toBeInTheDocument();
    expect(screen.getByText(/clarification_required/)).toBeInTheDocument();
  });

  test("모든 9개 관리 route가 화면 제목을 렌더링한다", async () => {
    for (const route of [
      "/admin/users",
      "/admin/orgs",
      "/admin/roles",
      "/admin/user-roles",
      "/admin/menu-permissions",
      "/admin/menu-structure",
      "/admin/menu-info",
      "/admin/code-groups",
      "/admin/code-details",
    ]) {
      window.history.pushState({}, "", route);
      render(<App />);
      await waitFor(() =>
        expect(
          screen.queryByText("loading spinner..."),
        ).not.toBeInTheDocument(),
      );
      document.body.innerHTML = "";
    }
  });

  test("API client는 /api 상대경로를 사용한다", async () => {
    render(<App />);
    await userEvent.click(
      await screen.findByRole("button", { name: "로그인" }),
    );
    expect(
      (fetch as unknown as ReturnType<typeof vi.fn>).mock.calls.every((call) =>
        String(call[0]).startsWith("/api/"),
      ),
    ).toBe(true);
  });
});
function json(body: unknown) {
  return Promise.resolve({ ok: true, status: 200, json: async () => body });
}
