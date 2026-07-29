import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, expect, test, vi } from "vitest";
import { App } from "./App";

beforeEach(() => {
  window.localStorage.clear();
  window.history.pushState({}, "", "/system/users");
  vi.stubGlobal(
    "fetch",
    vi.fn(async () => ({
      json: async () => ({
        success: true,
        data: [
          {
            id: "11111111-1111-1111-1111-111111111111",
            name: "시드 관리자",
            status: "ENABLED",
            updatedAt: "2026-07-29T00:00:00Z",
          },
        ],
      }),
    })),
  );
});

test("renders all nine authorized system management routes through sidebar navigation", async () => {
  render(<App />);
  for (const title of [
    "사용자 관리",
    "조직 관리",
    "역할 관리",
    "사용자 역할 관리",
    "메뉴 권한 관리",
    "메뉴 구조 관리",
    "메뉴 정보 관리",
    "코드그룹 관리",
    "상세코드 관리",
  ]) {
    await userEvent.click(screen.getByRole("link", { name: title }));
    expect(
      await screen.findByRole("heading", { name: title }),
    ).toBeInTheDocument();
  }
});

test("uses relative api paths for collection loading", async () => {
  render(<App />);
  await waitFor(() =>
    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/users?keyword=",
      expect.any(Object),
    ),
  );
});

test("hides unauthorized menu entries and shows permission state", async () => {
  render(<App />);
  await userEvent.selectOptions(
    screen.getByLabelText("역할 선택"),
    "SYSTEM_VIEWER",
  );
  expect(
    screen.queryByRole("link", { name: "사용자 관리" }),
  ).not.toBeInTheDocument();
  expect(
    await screen.findByRole("heading", { name: "권한 없음" }),
  ).toBeInTheDocument();
});

test("save posts field validation-ready payload to selected api", async () => {
  render(<App />);
  await screen.findByText("시드 관리자");
  await userEvent.clear(screen.getByLabelText("changeReason"));
  await userEvent.type(screen.getByLabelText("changeReason"), "변경 사유");
  await userEvent.click(screen.getByRole("button", { name: "저장" }));
  await waitFor(() =>
    expect(fetch).toHaveBeenCalledWith(
      "/api/admin/users/11111111-1111-1111-1111-111111111111",
      expect.objectContaining({ method: "PUT" }),
    ),
  );
});
