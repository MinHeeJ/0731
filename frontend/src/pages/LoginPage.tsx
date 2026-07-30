import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { endpoints } from "../api/client";

type LoginPageProps = {
  onLogin: (routes: string[]) => void;
};

export function LoginPage({ onLogin }: LoginPageProps) {
  const [loginId, setLoginId] = useState("admin");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const result = await endpoints.login({ loginId, password });
      onLogin(result.menuRoutes);
      navigate("/system/users");
    } catch (err) {
      setError(err instanceof Error ? err.message : "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-brand-navy px-4">
      <form onSubmit={submit} className="card w-full max-w-md p-8">
        <p className="text-sm font-semibold text-brand-gold">
          KNUE Faculty Achievement
        </p>
        <h1 className="mt-2 text-2xl font-bold">관리자 Login</h1>
        <p className="mt-2 text-sm text-slate-500">
          시드 관리자 계정으로 9개 시스템 관리 화면을 검증합니다.
        </p>
        {error && (
          <div
            role="alert"
            className="mt-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700"
          >
            {error}
          </div>
        )}
        <label className="mt-6 block text-sm font-semibold">로그인 ID</label>
        <input
          className="dense-input mt-2 w-full"
          value={loginId}
          onChange={(event) => setLoginId(event.target.value)}
        />
        <label className="mt-4 block text-sm font-semibold">비밀번호</label>
        <input
          className="dense-input mt-2 w-full"
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          placeholder="admin"
        />
        <button className="primary-button mt-6 w-full" disabled={loading}>
          {loading ? "인증 중..." : "Login"}
        </button>
      </form>
    </div>
  );
}
