import { cleanup, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ScrCommonSetting } from "./ScrCommonSetting";

const items = [
  {
    settingKey: "sessionIdleMinutes",
    settingName: "세션 유휴시간",
    settingValue: "30",
    valueType: "INTEGER",
    unitCode: "MINUTE",
    scopeType: "GLOBAL",
    displayOrder: 1,
  },
  {
    settingKey: "pageSize",
    settingName: "페이지당 조회건수",
    settingValue: "50",
    valueType: "INTEGER",
    unitCode: "COUNT",
    scopeType: "GLOBAL",
    displayOrder: 2,
  },
  {
    settingKey: "defaultSearchPeriodDays",
    settingName: "기본 검색기간",
    settingValue: "7",
    valueType: "INTEGER",
    unitCode: "DAY",
    scopeType: "GLOBAL",
    displayOrder: 3,
  },
  {
    settingKey: "bulkQueryThresholdCount",
    settingName: "대량조회 기준건수",
    settingValue: "1000",
    valueType: "INTEGER",
    unitCode: "COUNT",
    scopeType: "GLOBAL",
    displayOrder: 4,
  },
  {
    settingKey: "longRunningTaskNoticeSeconds",
    settingName: "장시간작업 안내 기준",
    settingValue: "60",
    valueType: "INTEGER",
    unitCode: "SECOND",
    scopeType: "GLOBAL",
    displayOrder: 5,
  },
];

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe("ScrCommonSetting", () => {
  it("renders five global common settings with their units from GET response", async () => {
    global.fetch = vi.fn(async () => ({
      ok: true,
      json: async () => ({ success: true, data: { items } }),
    })) as never;

    render(<ScrCommonSetting />);

    expect(
      await screen.findByRole("heading", { name: "공통 환경설정" }),
    ).toBeInTheDocument();
    expect(await screen.findByText("세션 유휴시간")).toBeInTheDocument();
    expect(screen.getByText("페이지당 조회건수")).toBeInTheDocument();
    expect(screen.getByText("기본 검색기간")).toBeInTheDocument();
    expect(screen.getByText("대량조회 기준건수")).toBeInTheDocument();
    expect(screen.getByText("장시간작업 안내 기준")).toBeInTheDocument();
    expect(screen.getByText("분")).toBeInTheDocument();
    expect(screen.getAllByText("건").length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText("일")).toBeInTheDocument();
    expect(screen.getByText("초")).toBeInTheDocument();
  });

  it("saves integer values through relative PUT path and refreshes after success", async () => {
    const calls: Array<{ path: string; init?: RequestInit; body?: unknown }> =
      [];
    global.fetch = vi.fn(
      async (input: RequestInfo | URL, init?: RequestInit) => {
        calls.push({
          path: String(input),
          init,
          body: init?.body ? JSON.parse(String(init.body)) : undefined,
        });
        return {
          ok: true,
          json: async () => ({ success: true, data: { items } }),
        } as Response;
      },
    ) as never;
    const user = userEvent.setup();

    render(<ScrCommonSetting />);

    const pageSize = await screen.findByTestId("common-setting-input-pageSize");
    await waitFor(() =>
      expect((pageSize as HTMLInputElement).value).toBe("50"),
    );
    await user.clear(pageSize);
    await user.type(pageSize, "50");
    await user.type(
      screen.getByTestId("common-settings-change-reason"),
      "운영 기준 변경",
    );
    await user.click(screen.getByTestId("common-settings-save-button"));

    await waitFor(() =>
      expect(
        calls.some(
          (call) =>
            call.path === "/api/system/common-settings" &&
            call.init?.method === "PUT",
        ),
      ).toBe(true),
    );
    const putCall = calls.find((call) => call.init?.method === "PUT");
    expect(putCall?.body).toMatchObject({
      pageSize: 50,
      changeReason: "운영 기준 변경",
    });
    expect(putCall?.body).not.toHaveProperty("userId");
    expect(putCall?.body).not.toHaveProperty("businessCategory");
    await screen.findByText(
      "저장되었습니다. 최신 공통 환경설정 값을 재조회했습니다.",
    );
    expect(
      calls.filter(
        (call) =>
          call.path === "/api/system/common-settings" && !call.init?.method,
      ).length,
    ).toBeGreaterThanOrEqual(2);
  });

  it("shows field errors and permission-denied state from API responses", async () => {
    global.fetch = vi.fn(
      async (input: RequestInfo | URL, init?: RequestInit) => {
        if (init?.method === "PUT") {
          return {
            ok: false,
            json: async () => ({
              success: false,
              error: {
                message: "입력값을 확인해 주세요.",
                fields: { sessionIdleMinutes: "정수 값만 입력할 수 있습니다." },
              },
            }),
          } as Response;
        }
        return {
          ok: true,
          json: async () => ({ success: true, data: { items } }),
        } as Response;
      },
    ) as never;
    const user = userEvent.setup();

    render(<ScrCommonSetting />);

    await user.click(await screen.findByTestId("common-settings-save-button"));
    expect(
      await screen.findByText("정수 값만 입력할 수 있습니다."),
    ).toBeInTheDocument();

    cleanup();
    global.fetch = vi.fn(async () => ({
      ok: false,
      json: async () => ({
        success: false,
        error: { message: "권한이 없습니다.", fields: {} },
      }),
    })) as never;

    render(<ScrCommonSetting />);
    expect(await screen.findByText("권한이 없습니다.")).toBeInTheDocument();
  });
});
