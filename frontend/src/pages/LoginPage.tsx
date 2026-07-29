import { FormEvent, useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { Field, StateBanner } from "../components/Ui";

export function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ userId: "admin", password: "admin" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (location.pathname === "/") return <Navigate to="/login" replace />;

  function submit(e: FormEvent) {
    e.preventDefault();
    if (!form.userId || !form.password) {
      setError("userId와 password를 입력하세요.");
      return;
    }
    setLoading(true);
    setError("");
    api("/api/auth/login", { method: "POST", body: JSON.stringify(form) })
      .then(() => navigate("/admin"))
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false));
  }

  return (
    <main className="auth-shell">
      <section className="auth-copy">
        <div className="brand-mark">KNUE</div>
        <h1>한국교원대학교 교수업적평가시스템</h1>
        <p>공통기능 관리자 콘솔</p>
        <ul>
          <li>시스템 관리 9개 메뉴 API-backed 조회·저장</li>
          <li>시드 관리자 admin/admin</li>
          <li>SSO/KORUS 원천 연동은 후속 확장 경계</li>
        </ul>
      </section>
      <section className="auth-panel">
        <div className="auth-card card">
          <h2>관리자 로그인</h2>
          <p className="muted">
            내부 계정으로 세션을 발급하고 /admin으로 이동합니다.
          </p>
          <StateBanner error={error} />
          <form className="form-grid" onSubmit={submit}>
            <Field label="userId">
              <input
                value={form.userId}
                onChange={(e) => setForm({ ...form, userId: e.target.value })}
              />
            </Field>
            <Field label="password">
              <input
                type="password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </Field>
            <div className="form-actions">
              <button className="btn" disabled={loading}>
                {loading ? "로그인 중..." : "로그인"}
              </button>
              <button
                className="btn ghost"
                type="button"
                onClick={() => setForm({ userId: "", password: "" })}
              >
                입력 초기화
              </button>
            </div>
          </form>
        </div>
      </section>
    </main>
  );
}
