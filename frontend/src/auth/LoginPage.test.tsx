import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { BrowserRouter } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { AuthProvider } from "./AuthProvider";
import { LoginPage } from "../pages/LoginPage";
describe("LoginPage", () => {
  it("submits admin credentials and shows login form fields", async () => {
    global.fetch = vi.fn().mockResolvedValue({
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
    }) as never;
    render(
      <BrowserRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </BrowserRouter>,
    );
    expect(screen.getByText("교원사이트 로그인")).toBeInTheDocument();
    await userEvent.click(screen.getByText("로그인"));
    expect(global.fetch).toHaveBeenCalledWith(
      "/api/auth/login",
      expect.any(Object),
    );
  });
});
