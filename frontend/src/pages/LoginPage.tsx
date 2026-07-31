import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

type Props = { onLoggedIn: () => Promise<void> };

export default function LoginPage({ onLoggedIn }: Props) {
  const [loginId, setLoginId] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [touched, setTouched] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const loginIdError =
    touched && !loginId.trim() ? "교번/계정을 입력하세요." : "";
  const passwordError =
    touched && !password.trim() ? "비밀번호를 입력하세요." : "";

  async function submit(event: FormEvent) {
    event.preventDefault();
    setTouched(true);
    if (!loginId.trim() || !password.trim()) return;
    setLoading(true);
    setError("");
    try {
      await api.login(loginId, password);
      await onLoggedIn();
      navigate("/system/readiness");
    } catch (err) {
      setError(err instanceof Error ? err.message : "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-screen">
      <section className="login-left">
        <div className="login-brand">
          <span className="brand-mark">KN</span>
          <strong>KNUE CMS 공통관리</strong>
        </div>
        <form className="login-card" onSubmit={submit} noValidate>
          <div className="login-heading">
            <p className="eyebrow">시스템 관리</p>
            <h1>관리자 로그인</h1>
            <p>계정과 비밀번호를 입력하면 R09 메뉴와 API 세션을 발급합니다.</p>
          </div>
          <label>
            교번/계정
            <input
              value={loginId}
              onChange={(event) => setLoginId(event.target.value)}
              onBlur={() => setTouched(true)}
              placeholder="admin"
              autoComplete="username"
            />
            {loginIdError && (
              <span className="field-error">{loginIdError}</span>
            )}
          </label>
          <label>
            비밀번호
            <input
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onBlur={() => setTouched(true)}
              type="password"
              placeholder="admin"
              autoComplete="current-password"
            />
            {passwordError && (
              <span className="field-error">{passwordError}</span>
            )}
          </label>
          {error && <div className="state-banner error">오류: {error}</div>}
          {!error && !loading && (
            <div className="state-banner empty">
              시드 관리자 계정으로 9개 메뉴 접근과 API 연결을 확인하세요.
            </div>
          )}
          {loading && (
            <div className="state-banner loading">
              <span className="spinner" /> 세션을 발급하는 중입니다...
            </div>
          )}
          <button className="primary" type="submit" disabled={loading}>
            {loading ? "로그인 중" : "로그인"}
          </button>
        </form>
      </section>
      <aside className="login-hero" aria-hidden="true">
        <div className="hero-card main-preview">
          <div className="preview-header" />
          <div className="preview-grid">
            <span />
            <span />
            <span />
          </div>
          <div className="preview-table">
            {Array.from({ length: 7 }).map((_, index) => (
              <i key={index} />
            ))}
          </div>
        </div>
        <div className="hero-copy">
          <p className="eyebrow">API-backed admin</p>
          <h2>조회 → 상세 선택 → 저장/회수 → 성공 refresh 흐름</h2>
          <p>
            공통기능 화면은 backend DTO와 실제 관리 테이블 기준 필드만
            표시합니다.
          </p>
        </div>
      </aside>
    </main>
  );
}
