import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../../shared/api/client";
import type { CurrentUser } from "../../shared/types";

export function LoginPage() {
  const routerNavigate = useNavigate();
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    api
      .get<CurrentUser>("/api/auth/me")
      .then(() => {
        if (active) routerNavigate("/admin", { replace: true });
      })
      .catch(() => undefined)
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [routerNavigate]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setMessage("");
    try {
      await api.post<CurrentUser>("/api/auth/login", { loginId, password });
      routerNavigate("/admin", { replace: true });
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "로그인 오류");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login">
      <section className="login-panel">
        <div className="login-brand">
          <div className="brand-mark large">KN</div>
          <div>
            <strong>교수업적평가시스템</strong>
            <span>공통기능 관리자</span>
          </div>
        </div>
        <form onSubmit={submit} className="login-card">
          <div className="page-heading compact">
            <p className="eyebrow">public auth</p>
            <h1>관리자 로그인</h1>
            <p>시드 관리자 계정으로 R09 시스템 관리 메뉴에 진입합니다.</p>
          </div>
          <label>
            <span>loginId</span>
            <input
              value={loginId}
              onChange={(event) => setLoginId(event.target.value)}
            />
          </label>
          <label>
            <span>password</span>
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          <button className="button" disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}
          </button>
          {message && <p className="state error">{message}</p>}
        </form>
      </section>
      <aside className="login-hero" aria-hidden="true">
        <div className="hero-window">
          <div className="hero-bar" />
          <div className="hero-grid">
            <span />
            <span />
            <span />
            <span />
          </div>
          <div className="hero-table" />
        </div>
      </aside>
    </main>
  );
}
