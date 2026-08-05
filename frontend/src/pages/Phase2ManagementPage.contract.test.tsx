import {
  cleanup,
  render,
  screen,
  waitFor,
  within,
} from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import { ManagementPage } from "./ManagementPage";

function mockFetch(rows: Record<string, unknown>[]) {
  const calls: Array<{ path: string; init?: RequestInit; body?: unknown }> = [];
  global.fetch = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    calls.push({
      path: String(input),
      init,
      body: init?.body ? JSON.parse(String(init.body)) : undefined,
    });
    return {
      ok: true,
      json: async () => ({
        success: true,
        data: init?.method ? (rows[0] ?? rows) : rows,
      }),
    } as Response;
  }) as never;
  return calls;
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe("Phase2 common feature management screens", () => {
  it("renders position management and calls relative /api/positions create path", async () => {
    const calls = mockFetch([
      {
        positionId: 1,
        positionCode: "DEPT_HEAD",
        userId: "USER-10002",
        organizationCode: "CSE",
        effectiveStartDate: "2026-08-01",
        effectiveEndDate: "2026-12-31",
        useYn: "Y",
      },
    ]);
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/system/positions"]}>
        <ManagementPage type="positions" />
      </MemoryRouter>,
    );
    expect(
      await screen.findByRole("heading", { name: "보직 관리" }),
    ).toBeInTheDocument();
    const form = document.querySelector("form.editor-card") as HTMLElement;
    await user.clear(within(form).getByLabelText(/보직코드/));
    await user.type(within(form).getByLabelText(/보직코드/), "DEAN");
    await user.clear(within(form).getByLabelText(/사용자ID/));
    await user.type(within(form).getByLabelText(/사용자ID/), "USER-10002");
    await user.clear(within(form).getByLabelText(/조직코드/));
    await user.type(within(form).getByLabelText(/조직코드/), "CSE");
    await user.click(within(form).getByRole("button", { name: "등록" }));
    await waitFor(() =>
      expect(
        calls.some(
          (c) => c.path === "/api/positions" && c.init?.method === "POST",
        ),
      ).toBe(true),
    );
  });

  it("renders common config defaults and saves selected row through /api/common-configs", async () => {
    const calls = mockFetch([
      {
        configKey: "PAGE_SIZE",
        configValue: "20",
        unit: "COUNT",
        displayName: "페이지당 조회건수",
      },
    ]);
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/system/common-configs"]}>
        <ManagementPage type="common-configs" />
      </MemoryRouter>,
    );
    expect(
      await screen.findByRole("heading", { name: "공통 환경설정" }),
    ).toBeInTheDocument();
    await user.click(await screen.findByRole("button", { name: "선택" }));
    const form = document.querySelector("form.editor-card") as HTMLElement;
    await user.clear(within(form).getByLabelText(/설정값/));
    await user.type(within(form).getByLabelText(/설정값/), "50");
    await user.click(within(form).getByRole("button", { name: "저장" }));
    await waitFor(() =>
      expect(
        calls.some(
          (c) => c.path === "/api/common-configs" && c.init?.method === "PUT",
        ),
      ).toBe(true),
    );
    const save = calls.find(
      (c) => c.path === "/api/common-configs" && c.init?.method === "PUT",
    );
    expect(save?.body).toMatchObject({
      items: [{ configKey: "PAGE_SIZE", configValue: "50" }],
    });
  });

  it("renders notice management and includes attachment metadata in create payload", async () => {
    const calls = mockFetch([
      {
        noticeId: 1,
        title: "2차 공통기능 안내",
        postingStartDate: "2026-08-01",
        postingEndDate: "2026-12-31",
        importantYn: "Y",
      },
    ]);
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/system/notices"]}>
        <ManagementPage type="notices" />
      </MemoryRouter>,
    );
    expect(
      await screen.findByRole("heading", { name: "공지사항 관리" }),
    ).toBeInTheDocument();
    const form = document.querySelector("form.editor-card") as HTMLElement;
    await user.clear(within(form).getByLabelText(/제목/));
    await user.type(within(form).getByLabelText(/제목/), "공지");
    await user.clear(within(form).getByLabelText(/내용/));
    await user.type(within(form).getByLabelText(/내용/), "내용");
    await user.clear(within(form).getByLabelText(/파일명/));
    await user.type(within(form).getByLabelText(/파일명/), "guide.pdf");
    await user.click(within(form).getByRole("button", { name: "등록" }));
    await waitFor(() =>
      expect(
        calls.some(
          (c) => c.path === "/api/notices" && c.init?.method === "POST",
        ),
      ).toBe(true),
    );
    const save = calls.find(
      (c) => c.path === "/api/notices" && c.init?.method === "POST",
    );
    expect(save?.body).toMatchObject({
      attachments: [{ originalFileName: "guide.pdf" }],
    });
  });
});
