import { FormEvent, useEffect, useState } from "react";
import { api } from "../api/client";

type Props = { onSuccess: () => void; notFoundPath?: string };

export function LoginPage({ onSuccess, notFoundPath }: Props) {
  const [userId, setUserId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [health, setHealth] = useState<"loading" | "success" | "error">(
    "loading",
  );
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api
      .health()
      .then((r) => setHealth(r.success ? "success" : "error"))
      .catch(() => setHealth("error"));
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (!userId.trim()) nextErrors.userId = "사용자 ID를 입력하세요.";
    if (!password.trim()) nextErrors.password = "비밀번호를 입력하세요.";
    setFieldErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    setLoading(true);
    setError("");
    const result = await api.login(userId, password).catch((e) => ({
      success: false,
      error: { message: String(e), errors: [] },
    }));
    setLoading(false);
    if (result.success) onSuccess();
    else {
      const errors: Record<string, string> = {};
      result.error?.errors?.forEach((item) => {
        errors[item.field] = item.reason;
      });
      setFieldErrors(errors);
      setError(result.error?.message || "로그인 실패");
    }
  }

  const healthText =
    health === "loading"
      ? "확인 중"
      : health === "success"
        ? "200 정상"
        : "오류";

  return (
    <main className="login-shell">
      <section className="login-card">
        <div className="login-brand">
          KNUE<span>CMS Common Admin</span>
        </div>
        <div className="login-heading">
          <p className="eyebrow">교수업적평가시스템</p>
          <h1>관리자 로그인</h1>
          <p>시드 관리자 admin/admin으로 공통기능 관리 메뉴에 진입합니다.</p>
        </div>
        {notFoundPath && (
          <div className="error">
            요청 경로 {notFoundPath}는 준비되지 않았습니다.
          </div>
        )}
        <form onSubmit={submit} noValidate>
          <label>
            <span>사용자 ID</span>
            <input
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              autoComplete="username"
            />
            {fieldErrors.userId && <em>{fieldErrors.userId}</em>}
          </label>
          <label>
            <span>비밀번호</span>
            <input
              value={password}
              type="password"
              onChange={(e) => setPassword(e.target.value)}
              autoComplete="current-password"
            />
            {fieldErrors.password && <em>{fieldErrors.password}</em>}
          </label>
          <div className={`badge ${health === "error" ? "bad" : ""}`}>
            상태 점검: GET /api/health {healthText}
          </div>
          <button disabled={loading}>
            {loading ? "로그인 중..." : "로그인"}
          </button>
          {!error && <div className="login-empty">인증 입력을 기다립니다.</div>}
          {error && <div className="error">{error}</div>}
        </form>
      </section>
      <section className="login-hero" aria-hidden="true">
        <div className="hero-window">
          <div className="hero-toolbar">
            <span />
            <span />
            <span />
          </div>
          <div className="hero-grid">
            <div />
            <div />
            <div />
            <div />
          </div>
          <div className="hero-table">
            {Array.from({ length: 8 }).map((_, i) => (
              <span key={i} />
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}
