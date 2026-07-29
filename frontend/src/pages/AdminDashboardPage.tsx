import { Link, useOutletContext } from "react-router-dom";
import type { Menu } from "../types";
import { Badge, PageTitle } from "../components/Ui";

export function AdminDashboardPage() {
  const { menus, health } = useOutletContext<{
    menus: Menu[];
    health: string;
  }>();
  const cards = [
    { title: "사용자·조직", count: 2, desc: "사용자 관리, 조직 관리" },
    { title: "역할·권한", count: 3, desc: "역할, 사용자 역할, 메뉴 권한" },
    { title: "메뉴", count: 2, desc: "구조 재정렬과 실행정보" },
    { title: "공통코드", count: 2, desc: "코드그룹과 상세코드" },
  ];
  const leafMenus = menus.filter((m) => m.menuLevel === 3);
  return (
    <section className="page-stack">
      <PageTitle
        title="공통기능 현황"
        desc="health와 관리 메뉴 컨텍스트를 API로 조회해 1차 산출물 상태를 확인합니다."
      />
      <div className="stat-grid">
        {cards.map((card) => (
          <div className="card stat-card" key={card.title}>
            <p>{card.title}</p>
            <strong>{card.count}</strong>
            <span>{card.desc}</span>
          </div>
        ))}
      </div>
      <div className="dashboard-grid">
        <section className="card dashboard-card">
          <h2>산출물/스택 검증</h2>
          <ul className="check-list">
            <li>backend_dir backend</li>
            <li>frontend_dir frontend</li>
            <li>compose_file infra/docker-compose.yml</li>
            <li>health_endpoint /api/health ({health})</li>
          </ul>
          <p className="muted">
            React 18 + TypeScript + Vite 5, 상대경로 /api/... 호출, Spring
            Boot/MyBatis/PostgreSQL 계약을 유지합니다.
          </p>
        </section>
        <section className="card dashboard-card">
          <h2>메뉴 컨텍스트</h2>
          {leafMenus.length ? (
            <div className="menu-chip-grid">
              {leafMenus.map((m) =>
                m.url ? (
                  <Link className="menu-chip" key={m.menuId} to={m.url}>
                    {m.menuName}
                    <Badge tone={m.useYn === "Y" ? "green" : "gray"}>
                      {m.screenId}
                    </Badge>
                  </Link>
                ) : (
                  <span className="menu-chip" key={m.menuId}>
                    {m.menuName}
                  </span>
                ),
              )}
            </div>
          ) : (
            <p className="muted">메뉴 권한이 없습니다.</p>
          )}
        </section>
        <section className="card dashboard-card wide">
          <h2>범위 안내</h2>
          <div className="notice-grid">
            <p>
              변경 추적 구조는 변경성 화면의 사유 입력과 서버 저장 응답으로
              확인합니다.
            </p>
            <p>
              실제 SSO, KORUS 원천 동기화, 파일/배치 업무 기능은 후속 확장
              경계입니다.
            </p>
            <p>
              비활성 정책은 물리 삭제 대신 useYn=N 상태 변경 UX로 제공합니다.
            </p>
          </div>
        </section>
      </div>
    </section>
  );
}
