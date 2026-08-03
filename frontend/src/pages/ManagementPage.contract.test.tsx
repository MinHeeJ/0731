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

function mockFetchWithRows(rows: Record<string, unknown>[]) {
  const calls: Array<{ path: string; init?: RequestInit; body?: unknown }> = [];
  global.fetch = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const path = String(input);
    calls.push({
      path,
      init,
      body: init?.body ? JSON.parse(String(init.body)) : undefined,
    });
    return {
      ok: true,
      json: async () => ({ success: true, data: init?.method ? [] : rows }),
    } as Response;
  }) as never;
  return calls;
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

describe("ManagementPage UI Contract drift repairs", () => {
  it("allows role_code on create but keeps it readonly for edit lifecycle identity", async () => {
    const calls = mockFetchWithRows([
      {
        roleCode: "R09",
        roleName: "시스템관리자",
        purpose: "시스템 관리",
        assignmentCriteria: "관리자",
        defaultDataScope: "ALL",
        useYn: "Y",
      },
    ]);
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/system/roles"]}>
        <ManagementPage type="roles" />
      </MemoryRouter>,
    );

    await screen.findByRole("heading", { name: "신규 등록" });
    const form = document.querySelector("form.editor-card") as HTMLElement;
    const createRoleCode = within(form).getByLabelText(/역할코드/);
    expect(createRoleCode).not.toHaveAttribute("readonly");

    await user.type(createRoleCode, "R10");
    await user.type(within(form).getByLabelText(/역할명/), "임시 역할");
    await user.click(within(form).getByRole("button", { name: "등록" }));

    await waitFor(() =>
      expect(
        calls.some(
          (call) => call.path === "/api/roles" && call.init?.method === "POST",
        ),
      ).toBe(true),
    );
    const createCall = calls.find(
      (call) => call.path === "/api/roles" && call.init?.method === "POST",
    );
    expect(createCall?.body).toMatchObject({
      roleCode: "R10",
      roleName: "임시 역할",
    });

    await user.click(screen.getByRole("button", { name: "선택" }));
    const editRoleCode = within(form).getByLabelText(/역할코드/);
    expect(editRoleCode).toHaveAttribute("readonly");
  });

  it("excludes KORUS readonly fields and roleCodes from updateUserUsage payload", async () => {
    const calls = mockFetchWithRows([
      {
        userId: "U10001",
        staffNo: "10001",
        staffName: "홍길동",
        organizationCode: "CSE",
        organizationName: "컴퓨터교육과",
        jobTitle: "교수",
        employmentStatus: "ACTIVE",
        roleCodes: ["R01", "R09"],
        useYn: "Y",
        positionName: "학과장",
        retirementDate: "",
        lastSyncedAt: "2026-07-31T09:00",
      },
    ]);
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/system/users"]}>
        <ManagementPage type="users" />
      </MemoryRouter>,
    );

    await user.click(await screen.findByRole("button", { name: "선택" }));
    const form = document.querySelector("form.editor-card") as HTMLElement;
    await user.selectOptions(within(form).getByLabelText(/사용여부/), "N");
    await user.click(within(form).getByRole("button", { name: "저장" }));

    await waitFor(() =>
      expect(
        calls.some(
          (call) =>
            call.path === "/api/users/U10001/usage" &&
            call.init?.method === "PATCH",
        ),
      ).toBe(true),
    );
    const usageCall = calls.find(
      (call) =>
        call.path === "/api/users/U10001/usage" &&
        call.init?.method === "PATCH",
    );
    expect(usageCall?.body).toMatchObject({ useYn: "N" });
    expect(usageCall?.body).not.toHaveProperty("roleCodes");
    expect(usageCall?.body).not.toHaveProperty("staffNo");
    expect(usageCall?.body).not.toHaveProperty("staffName");
  });

  it("blocks create-style save on update-only screens until a row is selected", async () => {
    mockFetchWithRows([]);
    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={["/system/organizations"]}>
        <ManagementPage type="organizations" />
      </MemoryRouter>,
    );

    const editor = await screen.findByRole("heading", { name: "신규 등록" });
    await user.click(
      within(editor.closest("form") as HTMLElement).getByRole("button", {
        name: "등록",
      }),
    );

    expect(
      await screen.findByText("목록에서 저장할 행을 먼저 선택하세요."),
    ).toBeInTheDocument();
  });
});
