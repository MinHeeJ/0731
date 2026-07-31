import { useEffect, useState } from "react";
import { api } from "../shared/api/client";

type Health = { status?: string; database?: string; timestamp?: string };

export function DashboardPage() {
  const [health, setHealth] = useState<Health | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  function load() {
    setLoading(true);
    setError("");
    api
      .get<Health>("/api/health")
      .then(setHealth)
      .catch((event) => setError(event.message))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    load();
  }, []);

  return (
    <section className="page">
      <div className="page-heading">
        <p className="eyebrow">system dashboard</p>
        <div className="title-row">
          <div>
            <h1>관리자 공통 대시보드</h1>
            <p>
              health, session, route coverage, runtime contract를 한 화면에서
              확인합니다.
            </p>
          </div>
          <button className="button secondary" type="button" onClick={load}>
            새로고침
          </button>
        </div>
      </div>

      {error && (
        <div className="state error">
          <span>{error}</span>
          <button className="button secondary" type="button" onClick={load}>
            다시 시도
          </button>
        </div>
      )}

      <div className="card-grid dashboard-grid">
        <article className="card stat-card">
          <div className="card-header-row">
            <h2>Health Card</h2>
            <span
              className={`badge ${health?.status === "UP" || health?.status === "ok" ? "success" : "muted"}`}
            >
              {loading ? "loading" : health?.status || "unknown"}
            </span>
          </div>
          {loading ? (
            <span className="skeleton-line wide" />
          ) : (
            <p>database {health?.database ?? "-"}</p>
          )}
          <small>/api/health 상대경로 호출</small>
        </article>
        <article className="card stat-card">
          <div className="card-header-row">
            <h2>Menu Coverage</h2>
            <span className="badge success">9 leaf</span>
          </div>
          <p>
            사용자, 조직, 역할, 사용자 역할, 메뉴 권한, 메뉴 구조, 메뉴 정보,
            코드그룹, 상세코드
          </p>
          <small>모든 leaf route는 AppRouter에 등록되어 있습니다.</small>
        </article>
        <article className="card stat-card">
          <div className="card-header-row">
            <h2>Runtime Contract</h2>
            <span className="badge">Compose</span>
          </div>
          <p>Spring Boot 3.3.x · React 18 · Vite 5 · PostgreSQL 16</p>
          <small>
            frontend nginx는 /api/* reverse proxy 계약을 사용합니다.
          </small>
        </article>
      </div>
    </section>
  );
}
