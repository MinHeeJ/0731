import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { describe, expect, it, vi } from "vitest";
import { AuthProvider } from "../auth/AuthProvider";
import { ProtectedRoute } from "./ProtectedRoute";
describe("ProtectedRoute", () => {
  it("redirects unauthenticated users to login", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ success: false, error: { message: "인증 필요" } }),
    }) as never;
    render(
      <MemoryRouter initialEntries={["/secret"]}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<div>로그인 화면</div>} />
            <Route
              path="/secret"
              element={
                <ProtectedRoute>
                  <div>secret</div>
                </ProtectedRoute>
              }
            />
          </Routes>
        </AuthProvider>
      </MemoryRouter>,
    );
    expect(await screen.findByText("로그인 화면")).toBeInTheDocument();
  });
});
