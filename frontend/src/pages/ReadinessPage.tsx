import { useEffect, useState } from "react";
import { api, MenuItem } from "../api/client";

type Props = { menus: MenuItem[] };
type ReadinessState = "loading" | "empty" | "error" | "permission" | "success";

const expectedRoutes = [
  "/system/users",
  "/system/organizations",
  "/system/roles",
  "/system/user-roles",
  "/system/menu-permissions",
  "/system/menu-tree",
  "/system/menus",
  "/system/code-groups",
  "/system/code-groups/SYSTEM_STATUS/codes",
];

export default function ReadinessPage({ menus }: Props) {
  const [health, setHealth] = useState<{
    status: string;
    application?: string;
    decisionStatus: string;
    persistence?: string;
    checks: Array<Record<string, unknown>>;
  } | null>(null);
  const [state, setState] = useState<ReadinessState>("loading");
  const [error, setError] = useState("");

  async function load() {
    setState("loading");
    setError("");
    try {
      const data = await api.health();
      setHealth(data);
      setState(data.checks.length ? "success" : "empty");
    } catch (err) {
      const text = err instanceof Error ? err.message : "상태 조회 실패";
      setError(text);
      setState(text.includes("권한") ? "permission" : "error");
    }
  }

  useEffect(() => {
    void load();
  }, []);

  const routeSet = new Set(menus.map((menu) => menu.routePath).filter(Boolean));
  const availableCount = expectedRoutes.filter((route) =>
    routeSet.has(route),
  ).length;
  const decisionReady =
    health?.decisionStatus &&
    health.decisionStatus !== "clarification_required";

  return (
    <section className="readiness-page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">
            시스템 관리 &gt; 공통 &gt; 1차 검증 대시보드
          </p>
          <h2>1차 검증 대시보드</h2>
          <p className="page-description">
            산출물, seed, 세션, 메뉴 권한, health API 준비상태를 확인합니다.
          </p>
        </div>
        <button onClick={load}>상태 새로고침</button>
      </div>

      <div className={`state-banner ${state}`} role="status">
        {state === "loading" && "health와 메뉴 권한을 확인하는 중입니다..."}
        {state === "empty" &&
          "health 응답은 수신했지만 상세 checks가 비어 있습니다."}
        {state === "error" && error}
        {state === "permission" && "권한이 없어 대시보드를 표시할 수 없습니다."}
        {state === "success" && "검증 데이터가 API에서 로드되었습니다."}
      </div>

      <div className="metric-grid">
        <article className="metric-card">
          <span>Health</span>
          <strong>
            {health?.status || (state === "loading" ? "확인 중" : "미준비")}
          </strong>
          <p>/api/health 응답 상태</p>
        </article>
        <article className="metric-card">
          <span>Handoff</span>
          <strong className={decisionReady ? "ok-text" : "warn-text"}>
            {health?.decisionStatus || "unknown"}
          </strong>
          <p>clarification_required는 build-ready로 표시하지 않습니다.</p>
        </article>
        <article className="metric-card">
          <span>Persistence</span>
          <strong>{health?.persistence || "mybatis/postgresql"}</strong>
          <p>관리 데이터 저장소 계약</p>
        </article>
        <article className="metric-card">
          <span>Menu coverage</span>
          <strong>
            {availableCount}/{expectedRoutes.length}
          </strong>
          <p>R09 대상 화면 route 렌더링 체크</p>
        </article>
      </div>

      <div className="dashboard-grid">
        <article className="card">
          <div className="card-header compact">
            <div>
              <h3>메뉴 checklist</h3>
              <p>사이드바는 각 항목을 React Router Link로 이동합니다.</p>
            </div>
          </div>
          <div className="check-list">
            {expectedRoutes.map((route) => {
              const found = menus.find((menu) => menu.routePath === route);
              return (
                <div
                  className={found ? "check-row ok" : "check-row warn"}
                  key={route}
                >
                  <span>{found ? "✓" : "!"}</span>
                  <div>
                    <strong>{found?.menuName || route}</strong>
                    <p>{route}</p>
                  </div>
                </div>
              );
            })}
          </div>
        </article>
        <article className="card">
          <div className="card-header compact">
            <div>
              <h3>Scope guard</h3>
              <p>1차 공통기능 범위 확인</p>
            </div>
          </div>
          <div className="scope-list">
            <p>
              교수업적평가·학술지원금 업무 데이터 기능은 생성하지 않았습니다.
            </p>
            <p>Mock KORUS 스냅샷과 로컬 DB 관리 범위만 UI에 노출합니다.</p>
            <p>API 호출은 모두 /api/... 상대경로를 사용합니다.</p>
          </div>
        </article>
      </div>

      <article className="card table-card">
        <div className="card-header compact">
          <div>
            <h3>상세 상태</h3>
            <p>delivery_status 테이블 조회 결과</p>
          </div>
        </div>
        <div className="data-table-wrap compact-table">
          <table>
            <thead>
              <tr>
                <th>상태 키</th>
                <th>값</th>
                <th>설명</th>
                <th>확인시각</th>
              </tr>
            </thead>
            <tbody>
              {state === "loading" &&
                Array.from({ length: 4 }).map((_, index) => (
                  <tr key={index}>
                    <td>
                      <span className="skeleton" />
                    </td>
                    <td>
                      <span className="skeleton" />
                    </td>
                    <td>
                      <span className="skeleton" />
                    </td>
                    <td>
                      <span className="skeleton" />
                    </td>
                  </tr>
                ))}
              {state !== "loading" &&
                health?.checks?.map((row) => (
                  <tr key={String(row.statusKey)}>
                    <td>{String(row.statusKey)}</td>
                    <td>
                      <span className="status-badge info">
                        {String(row.statusValue)}
                      </span>
                    </td>
                    <td>{String(row.description)}</td>
                    <td>{String(row.checkedAt || "-")}</td>
                  </tr>
                ))}
              {state !== "loading" && !health?.checks?.length && (
                <tr>
                  <td colSpan={4} className="empty-row">
                    상세 상태 데이터가 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </article>
    </section>
  );
}
