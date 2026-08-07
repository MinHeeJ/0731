import {
  cleanup,
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { CommonSettingsPage } from "./CommonSettingsPage";

const initialItems = [
  {
    settingKey: "sessionIdleTime",
    settingValue: "30",
    settingUnit: "분",
    settingMeaning: "세션 유휴시간",
  },
  {
    settingKey: "pageSize",
    settingValue: "20",
    settingUnit: "건",
    settingMeaning: "페이지당 조회건수",
  },
  {
    settingKey: "defaultSearchPeriod",
    settingValue: "30",
    settingUnit: "일",
    settingMeaning: "기본 검색기간",
  },
  {
    settingKey: "bulkQueryThreshold",
    settingValue: "1000",
    settingUnit: "건",
    settingMeaning: "대량조회 기준건수",
  },
  {
    settingKey: "longRunningTaskNoticeThreshold",
    settingValue: "10",
    settingUnit: "분",
    settingMeaning: "장시간작업 안내 기준",
  },
];

function ok(items = initialItems) {
  return {
    success: true,
    data: { items, page: 0, size: 20, totalElements: items.length },
  };
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe("CommonSettingsPage", () => {
  it("renders five common setting rows and no user/business override controls", async () => {
    global.fetch = vi.fn(
      async () => ({ ok: true, json: async () => ok() }) as Response,
    ) as never;

    render(<CommonSettingsPage />);

    expect(
      await screen.findByTestId("common-settings-page"),
    ).toBeInTheDocument();
    for (const [key, label] of [
      ["sessionIdleTime", "세션 유휴시간"],
      ["pageSize", "페이지당 조회건수"],
      ["defaultSearchPeriod", "기본 검색기간"],
      ["bulkQueryThreshold", "대량조회 기준건수"],
      ["longRunningTaskNoticeThreshold", "장시간작업 안내 기준"],
    ] as const) {
      expect(
        within(screen.getByTestId(`common-setting-${key}-row`)).getAllByText(
          label,
        )[0],
      ).toBeInTheDocument();
    }
    expect(screen.queryByLabelText(/userId/i)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/businessId/i)).not.toBeInTheDocument();
    expect(
      screen.getByText(/사용자별·업무별 override 입력/),
    ).toBeInTheDocument();
  });

  it("saves through confirmation modal and refreshes current values from GET", async () => {
    const user = userEvent.setup();
    const calls: Array<{ path: string; init?: RequestInit; body?: unknown }> =
      [];
    const refreshed = initialItems.map((item) =>
      item.settingKey === "pageSize" ? { ...item, settingValue: "50" } : item,
    );
    global.fetch = vi.fn(
      async (input: RequestInfo | URL, init?: RequestInit) => {
        calls.push({
          path: String(input),
          init,
          body: init?.body ? JSON.parse(String(init.body)) : undefined,
        });
        if (init?.method === "PUT")
          return { ok: true, json: async () => ok(refreshed) } as Response;
        return {
          ok: true,
          json: async () => ok(calls.length > 2 ? refreshed : initialItems),
        } as Response;
      },
    ) as never;

    render(<CommonSettingsPage />);
    const pageSizeInput = await screen.findByTestId(
      "common-setting-pageSize-input",
    );
    await waitFor(() => expect(pageSizeInput).toHaveValue("20"));
    await user.clear(pageSizeInput);
    await user.type(pageSizeInput, "50");
    await user.click(screen.getByTestId("common-settings-save-button"));
    await user.click(
      within(screen.getByTestId("common-settings-save-modal")).getByText(
        "확인",
      ),
    );

    await waitFor(() =>
      expect(
        screen.getByText("공통 환경설정이 저장되었습니다"),
      ).toBeInTheDocument(),
    );
    expect(
      calls.some(
        (call) =>
          call.path === "/api/system/common-settings" &&
          call.init?.method === "PUT",
      ),
    ).toBe(true);
    const putCall = calls.find((call) => call.init?.method === "PUT");
    expect(putCall?.body).toMatchObject({ pageSize: "50" });
    expect(
      screen.getByTestId("common-setting-pageSize-current"),
    ).toHaveTextContent("50");
  });

  it("displays field-level validation errors and existing-value message", async () => {
    const user = userEvent.setup();
    global.fetch = vi.fn(
      async (input: RequestInfo | URL, init?: RequestInit) => {
        if (init?.method === "PUT") {
          return {
            ok: false,
            json: async () => ({
              success: false,
              error: {
                message: "공통 환경설정 값을 확인해 주세요.",
                fieldErrors: {
                  pageSize: "페이지당 조회건수 의미와 맞지 않는 값입니다.",
                },
              },
            }),
          } as Response;
        }
        return { ok: true, json: async () => ok() } as Response;
      },
    ) as never;

    render(<CommonSettingsPage />);
    const validationInput = await screen.findByTestId(
      "common-setting-pageSize-input",
    );
    await waitFor(() => expect(validationInput).toHaveValue("20"));
    await user.clear(validationInput);
    await user.type(validationInput, "invalid-unit");
    await user.click(screen.getByTestId("common-settings-save-button"));
    await user.click(screen.getByTestId("common-settings-confirm-button"));

    expect(
      await screen.findByText("페이지당 조회건수 의미와 맞지 않는 값입니다."),
    ).toBeInTheDocument();
    expect(screen.getByText(/기존 값은 유지됩니다/)).toBeInTheDocument();
  });
});
