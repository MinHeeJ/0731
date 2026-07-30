import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi, beforeEach } from "vitest";
import { api } from "./api/client";
import { AdminScreen } from "./pages/AdminScreen";
import { screens } from "./routes";

beforeEach(() => {
  vi.restoreAllMocks();
});

describe("UI route contract", () => {
  it("defines all nine system management routes from ui-design.md", () => {
    expect(screens.map((screenConfig) => screenConfig.route)).toEqual([
      "/system/users",
      "/system/organizations",
      "/system/roles",
      "/system/user-roles",
      "/system/menu-permissions",
      "/system/menu-structure",
      "/system/menu-info",
      "/system/code-groups",
      "/system/code-details",
    ]);
  });

  it("renders Korean management copy and loading state for a data-backed route", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({ success: true, data: { items: [] }, error: null }),
        { status: 200, headers: { "Content-Type": "application/json" } },
      ),
    );

    render(
      <MemoryRouter>
        <AdminScreen screen={screens[0]} />
      </MemoryRouter>,
    );

    expect(screen.getByText("사용자 관리")).toBeInTheDocument();
    expect(
      await screen.findByText("검색 결과가 없습니다."),
    ).toBeInTheDocument();
  });

  it("surfaces permission-denied state when backend returns 403", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(
        JSON.stringify({
          success: false,
          data: null,
          error: { code: "FORBIDDEN", message: "권한 없음", fieldErrors: [] },
        }),
        { status: 403, headers: { "Content-Type": "application/json" } },
      ),
    );

    render(
      <MemoryRouter>
        <AdminScreen screen={screens[1]} />
      </MemoryRouter>,
    );

    expect(await screen.findByText(/권한 없음:/)).toBeInTheDocument();
  });
});

describe("relative API client contract", () => {
  it("rejects absolute API URLs so browser code cannot target localhost", async () => {
    await expect(api("https://api.example.com/api/users")).rejects.toThrow(
      "/api/",
    );
  });

  it("uses credentials include for relative /api calls", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(
        new Response(
          JSON.stringify({ success: true, data: { items: [] }, error: null }),
          { status: 200, headers: { "Content-Type": "application/json" } },
        ),
      );
    await api("/api/users");
    expect(fetchMock).toHaveBeenCalledWith(
      "/api/users",
      expect.objectContaining({ credentials: "include" }),
    );
  });
});
