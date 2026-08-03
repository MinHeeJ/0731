import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../auth/authApi";
import { useAuth } from "../auth/AuthProvider";
import { FieldError, StateBanner } from "../components/State";

export function LoginPage() {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [fields, setFields] = useState<Record<string, string>>({});
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const { setUser } = useAuth();
  const navigate = useNavigate();

  async function submit(e: FormEvent) {
    e.preventDefault();
    setLoading(true);
    setMessage("");
    setFields({});
    const res = await login(loginId, password);
    setLoading(false);
    if (res.success && res.data) {
      setUser(res.data);
      setMessage("로그인 성공");
      navigate("/system/users");
    } else {
      setFields(res.error?.fields || {});
      setMessage(res.error?.message || "로그인 실패");
    }
  }

  return (
    <main className="login-shell">
      <section className="login-panel">
        <div className="login-card card">
          <div className="sidebar-header">
            <div className="brand-mark">CMS</div>
            <div>
              <h1>교원사이트 로그인</h1>
              <p>로컬 관리자 credential로 세션을 생성합니다.</p>
            </div>
          </div>
          {loading && <StateBanner type="loading" message="로그인 처리 중" />}
          {message && (
            <StateBanner
              type={message.includes("성공") ? "success" : "error"}
              message={message}
            />
          )}
          <form onSubmit={submit} className="form-grid" aria-busy={loading}>
            <label className="field">
              <span>login_id</span>
              <input
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                autoComplete="username"
              />
              <FieldError name="loginId" fields={fields} />
            </label>
            <label className="field">
              <span>password</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
              <FieldError name="password" fields={fields} />
            </label>
            <button className="button" disabled={loading}>
              로그인
            </button>
          </form>
          <p className="info-note">
            성공 시 /system/users로 이동하고 현재 사용자 R09 badge가 표시됩니다.
          </p>
        </div>
      </section>
      <section className="login-hero" aria-hidden="true">
        <div className="hero-copy">
          <span className="eyebrow">shadcn-admin style</span>
          <h2>API-backed 시스템 관리 콘솔</h2>
          <p>
            사용자, 조직, 역할, 메뉴, 공통코드 화면을 같은 상태 흐름과 카드 기반
            레이아웃으로 관리합니다.
          </p>
        </div>
      </section>
    </main>
  );
}
