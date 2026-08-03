import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { AuthProvider } from "../auth/AuthProvider";
import { AppRoutes } from "./AppRoutes";

describe("AppRoutes navigation shell", () => {
  it("renders protected menu routes with active link styling", async () => {
    global.fetch = vi.fn(async (input: RequestInfo | URL) => {
      const path = String(input);
      if (path.includes("/api/auth/me")) {
        return {
          ok: true,
          json: async () => ({
            success: true,
            data: {
              userId: "admin",
              loginId: "admin",
              displayName: "시스템관리자",
              roles: ["R09"],
            },
          }),
        } as Response;
      }
      return {
        ok: true,
        json: async () => ({ success: true, data: [] }),
      } as Response;
    }) as never;

    render(
      <MemoryRouter initialEntries={["/system/roles"]}>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </MemoryRouter>,
    );

    const rolesLink = await screen.findByRole("link", { name: "역할 관리" });
    expect(rolesLink).toHaveAttribute("href", "/system/roles");
    await waitFor(() => expect(rolesLink.className).toContain("active"));
    expect(
      await screen.findByRole("heading", { name: "역할 관리" }),
    ).toBeInTheDocument();
  });
});
