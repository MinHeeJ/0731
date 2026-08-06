import {
  cleanup,
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import { afterEach } from "vitest";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";

afterEach(() => cleanup());
import { AuthContext } from "../auth/AuthProvider";
import { SystemConfigPage } from "./SystemConfigPage";

const rows = [
  {
    settingKey: "SESSION_IDLE_TIMEOUT",
    settingName: "세션 유휴시간",
    settingValue: "30",
    unit: "분",
    defaultValue: "30",
    minValue: "5",
    maxValue: "480",
    description: "세션 유휴시간",
  },
  {
    settingKey: "PAGE_SIZE",
    settingName: "페이지당 조회건수",
    settingValue: "20",
    unit: "건",
    defaultValue: "20",
    minValue: "10",
    maxValue: "100",
    description: "페이지당 조회건수",
  },
];

function renderWithRole(role: string) {
  const calls: Array<{ path: string; init?: RequestInit; body?: unknown }> = [];
  global.fetch = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const path = String(input);
    calls.push({
      path,
      init,
      body: init?.body ? JSON.parse(String(init.body)) : undefined,
    });
    if (init?.method === "PUT") {
      return {
        ok: true,
        json: async () => ({
          success: true,
          data: { ...rows[1], settingValue: "50" },
        }),
      } as Response;
    }
    return {
      ok: true,
      json: async () => ({ success: true, data: rows }),
    } as Response;
  }) as never;

  render(
    <AuthContext.Provider
      value={{
        user: {
          userId: "u1",
          loginId: "u1",
          displayName: "사용자",
          roles: [role],
        },
        setUser: vi.fn(),
        loading: false,
        hasRole: (target: string) => target === role,
      }}
    >
      <SystemConfigPage />
    </AuthContext.Provider>,
  );
  return calls;
}

describe("SystemConfigPage", () => {
  it("shows loading then system config list with required columns", async () => {
    renderWithRole("R09");

    expect(
      screen.getByText("환경설정 목록을 조회 중입니다."),
    ).toBeInTheDocument();
    expect(await screen.findByText("PAGE_SIZE")).toBeInTheDocument();
    expect(
      screen.getByRole("columnheader", { name: "항목코드" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("columnheader", { name: "현재 설정값" }),
    ).toBeInTheDocument();
  });

  it("selects a row, shows range guide, saves and restores default through relative api", async () => {
    const calls = renderWithRole("R09");
    const user = userEvent.setup();

    await user.click(
      await screen.findByRole("button", { name: "PAGE_SIZE 선택" }),
    );
    const form = screen.getByTestId("system-config-edit-panel");
    expect(
      within(form).getByText("허용 범위: 10 ~ 100 건"),
    ).toBeInTheDocument();

    await user.clear(within(form).getByLabelText("설정값"));
    await user.type(within(form).getByLabelText("설정값"), "50");
    await user.click(within(form).getByRole("button", { name: "저장" }));

    await waitFor(() =>
      expect(
        calls.some(
          (call) =>
            call.path === "/api/system-config/PAGE_SIZE" &&
            call.init?.method === "PUT",
        ),
      ).toBe(true),
    );
    expect(
      calls.find((call) => call.init?.method === "PUT")?.body,
    ).toMatchObject({ setting_value: "50" });

    await user.click(within(form).getByRole("button", { name: "기본값 복원" }));
    await waitFor(() =>
      expect(
        calls.filter(
          (call) =>
            call.path === "/api/system-config/PAGE_SIZE" &&
            call.init?.method === "PUT",
        ).length,
      ).toBeGreaterThanOrEqual(2),
    );
  });

  it("renders non R09 users as readonly with disabled save and restore buttons", async () => {
    renderWithRole("R01");
    const user = userEvent.setup();

    await user.click(
      await screen.findByRole("button", { name: "PAGE_SIZE 선택" }),
    );
    const form = screen.getByTestId("system-config-edit-panel");
    expect(
      screen.getByText("R09 시스템관리자만 설정값을 변경할 수 있습니다."),
    ).toBeInTheDocument();
    expect(within(form).getByRole("button", { name: "저장" })).toBeDisabled();
    expect(
      within(form).getByRole("button", { name: "기본값 복원" }),
    ).toBeDisabled();
  });
});
