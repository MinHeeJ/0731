import {
  Activity,
  Building2,
  CheckCircle2,
  Loader2,
  LogIn,
} from "lucide-react";
import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

export function LoginPage() {
  const navigate = useNavigate();
  const [userId, setUserId] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [health, setHealth] = useState("확인 중");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api
      .getHealth()
      .then((data) => setHealth(data.status))
      .catch(() => setHealth("ERROR"));
  }, []);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError("");
    if (!userId.trim() || !password.trim()) {
      setError("사용자 ID와 비밀번호를 입력하세요.");
      return;
    }
    setLoading(true);
    try {
      await api.login(userId, password);
      navigate("/admin/users", { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : "로그인 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="grid min-h-svh bg-background text-foreground lg:grid-cols-2">
      <section className="flex items-center justify-center px-6 py-10 lg:p-8">
        <div className="mx-auto flex w-full max-w-sm flex-col justify-center space-y-6">
          <div className="flex items-center justify-center gap-2">
            <span className="grid size-9 place-items-center rounded-lg bg-primary text-primary-foreground shadow-sm">
              <Building2 size={19} />
            </span>
            <span className="text-xl font-semibold">
              KNUE 교수업적평가시스템
            </span>
          </div>
          <form
            className="rounded-xl border bg-card p-6 text-card-foreground shadow-sm"
            onSubmit={submit}
          >
            <div className="mb-5 space-y-1.5 text-start">
              <h1 className="text-lg font-semibold tracking-tight">로그인</h1>
              <p className="text-sm text-muted-foreground">
                R09 시스템관리자 세션을 생성해 공통 관리 메뉴로 이동합니다.
              </p>
            </div>
            <div className="grid gap-4">
              <label className="grid gap-2 text-sm font-medium">
                사용자 ID
                <input
                  value={userId}
                  onChange={(event) => setUserId(event.target.value)}
                  placeholder="admin"
                  autoComplete="username"
                />
              </label>
              <label className="grid gap-2 text-sm font-medium">
                비밀번호
                <input
                  type="password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="admin"
                  autoComplete="current-password"
                />
              </label>
              <div className="flex items-center justify-between rounded-md border bg-muted/50 px-3 py-2 text-sm text-muted-foreground">
                <span className="inline-flex items-center gap-2">
                  <Activity size={16} /> 상태 점검
                </span>
                <span
                  className={health === "UP" ? "badge badge-success" : "badge"}
                >
                  {health === "UP" ? <CheckCircle2 size={14} /> : null}
                  {health}
                </span>
              </div>
              {error ? <p className="field-error">{error}</p> : null}
              <button
                className="btn btn-primary h-10 w-full"
                disabled={loading}
                type="submit"
              >
                {loading ? (
                  <Loader2 className="animate-spin" size={16} />
                ) : (
                  <LogIn size={16} />
                )}
                {loading ? "로그인 중..." : "로그인"}
              </button>
            </div>
            <p className="mt-5 text-center text-xs text-muted-foreground">
              시드 관리자 계정은 admin / admin 입니다.
            </p>
          </form>
        </div>
      </section>
      <section className="relative hidden overflow-hidden bg-muted lg:block">
        <div className="absolute inset-8 rounded-3xl border bg-background/80 p-8 shadow-2xl backdrop-blur">
          <div className="mb-8 flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-muted-foreground">
                Common Foundation
              </p>
              <h2 className="text-2xl font-bold tracking-tight">
                시스템 관리 콘솔
              </h2>
            </div>
            <span className="badge badge-success">API-backed</span>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            {["사용자·조직", "역할·권한", "메뉴", "공통코드"].map(
              (title, index) => (
                <div
                  key={title}
                  className="rounded-xl border bg-card p-5 shadow-sm"
                >
                  <p className="text-sm font-medium text-muted-foreground">
                    0{index + 1}
                  </p>
                  <p className="mt-2 text-lg font-semibold">{title} 관리</p>
                  <div className="mt-5 h-2 rounded-full bg-secondary">
                    <div
                      className="h-2 rounded-full bg-primary"
                      style={{ width: `${55 + index * 10}%` }}
                    />
                  </div>
                </div>
              ),
            )}
          </div>
          <div className="mt-6 rounded-xl border bg-card p-5 shadow-sm">
            <div className="mb-3 h-3 w-36 rounded-full bg-muted" />
            <div className="space-y-2">
              <div className="h-9 rounded-md bg-muted/80" />
              <div className="h-9 rounded-md bg-muted/60" />
              <div className="h-9 rounded-md bg-muted/40" />
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}
