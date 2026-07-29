import { useEffect, useMemo, useState } from "react";
import {
  AlertTriangle,
  CheckCircle2,
  ChevronDown,
  CircleOff,
  FolderTree,
  Loader2,
  Plus,
  RefreshCw,
  Save,
  Search,
  ShieldAlert,
  Trash2,
} from "lucide-react";
import { AdminItem, adminRequest, currentRole } from "../../services/admin/api";
import { ScreenConfig } from "./screens";

const statusOptions = {
  useStatus: ["ENABLED", "DISABLED"],
  effectiveStatus: ["ACTIVE", "EXPIRED", "REVOKED"],
  permissionAction: ["READ", "CREATE", "UPDATE", "DELETE"],
  displayStatus: ["VISIBLE", "HIDDEN"],
  none: [],
};

const permissionActions = ["READ", "CREATE", "UPDATE", "DELETE"];

function statusOf(item: AdminItem) {
  return (
    item.status ??
    item.useStatus ??
    item.effectiveStatus ??
    item.permissionAction ??
    item.displayStatus ??
    "-"
  );
}

function StatePanel({
  kind,
  title,
  message,
  onRetry,
}: {
  kind: "loading" | "empty" | "error" | "permission";
  title: string;
  message: string;
  onRetry?: () => void;
}) {
  if (kind === "loading") {
    return (
      <div
        className="state-card loading-state"
        role="status"
        aria-label="loading"
      >
        <div className="state-icon pulse">
          <Loader2 size={22} />
        </div>
        <div className="skeleton-stack" aria-hidden="true">
          <span />
          <span />
          <span />
        </div>
      </div>
    );
  }

  const icon =
    kind === "permission" ? (
      <ShieldAlert />
    ) : kind === "error" ? (
      <AlertTriangle />
    ) : (
      <CircleOff />
    );
  return (
    <div
      className={`state-card ${kind}`}
      role={kind === "error" ? "alert" : "status"}
    >
      <div className="state-icon">{icon}</div>
      <div>
        <h2>{title}</h2>
        <p>{message}</p>
      </div>
      {onRetry && kind === "error" && (
        <button className="button secondary" onClick={onRetry}>
          <RefreshCw size={15} />
          다시 조회
        </button>
      )}
    </div>
  );
}

function ArchetypeBadge({
  archetype,
}: {
  archetype: ScreenConfig["archetype"];
}) {
  return (
    <span className="archetype-badge">{archetype.replace(/_/g, " ")}</span>
  );
}

export function AdminScreen({ screen }: { screen: ScreenConfig }) {
  const [items, setItems] = useState<AdminItem[]>([]);
  const [selected, setSelected] = useState<AdminItem | null>(null);
  const [keyword, setKeyword] = useState("");
  const [name, setName] = useState("");
  const [changeReason, setChangeReason] = useState("");
  const [statusValue, setStatusValue] = useState("ENABLED");
  const [state, setState] = useState<
    "loading" | "empty" | "error" | "permission" | "success"
  >("loading");
  const [message, setMessage] = useState("");
  const [role, setRole] = useState(currentRole());
  const admin = role === "SYSTEM_ADMIN";
  const options = statusOptions[screen.statusKind];

  useEffect(() => {
    const syncRole = () => setRole(currentRole());
    window.addEventListener("role-change", syncRole);
    return () => window.removeEventListener("role-change", syncRole);
  }, []);

  useEffect(() => {
    setSelected(null);
    setKeyword("");
    load("");
  }, [screen.route, admin]);

  async function load(nextKeyword = keyword) {
    if (!admin) {
      setState("permission");
      return;
    }
    setState("loading");
    try {
      const response = await adminRequest<AdminItem[]>(
        `${screen.apiPath}?keyword=${encodeURIComponent(nextKeyword)}`,
      );
      if (!response.success) {
        setMessage(response.error?.message ?? "조회 실패");
        setState("error");
        return;
      }
      const rows = response.data ?? [];
      setItems(rows);
      setState(rows.length === 0 ? "empty" : "success");
      if (rows.length > 0) {
        select(rows[0]);
      }
    } catch {
      setMessage("API 연결 오류가 발생했습니다");
      setState("error");
    }
  }

  function select(item: AdminItem) {
    setSelected(item);
    setName(item.name);
    setStatusValue(
      item.status ??
        item.useStatus ??
        item.effectiveStatus ??
        item.permissionAction ??
        item.displayStatus ??
        "ENABLED",
    );
    setChangeReason("");
  }

  function createNew() {
    setSelected(null);
    setName("");
    setStatusValue(options[0] ?? "ENABLED");
    setChangeReason("");
    setMessage("신규 입력 모드입니다. 필수값과 변경 사유를 입력하세요.");
  }

  async function save() {
    if (!admin) {
      setState("permission");
      return;
    }
    const body: AdminItem = { name, changeReason };
    if (screen.statusKind !== "none") {
      (body as Record<string, string>)[screen.statusKind] = statusValue;
    }
    const response = await adminRequest<AdminItem>(
      selected?.id ? `${screen.apiPath}/${selected.id}` : screen.apiPath,
      { method: selected?.id ? "PUT" : "POST", body: JSON.stringify(body) },
    );
    if (!response.success) {
      setMessage(
        response.error?.errors?.[0]?.reason ??
          response.error?.message ??
          "저장 실패",
      );
      setState("error");
      return;
    }
    setMessage("저장되었습니다");
    await load();
  }

  async function remove() {
    if (!selected?.id) {
      return;
    }
    const reason = changeReason || "삭제 사유 입력";
    const response = await adminRequest<{ deleted: boolean }>(
      `${screen.apiPath}/${selected.id}?reason=${encodeURIComponent(reason)}`,
      { method: "DELETE" },
    );
    if (!response.success) {
      setMessage(response.error?.message ?? "삭제 실패");
      setState("error");
      return;
    }
    setMessage("삭제되었습니다");
    setSelected(null);
    await load();
  }

  const archetypeHelp = useMemo(() => {
    if (screen.archetype === "TREE_EDITOR") {
      return "상위/하위 노드 탐색, 정렬 제어, 선택 노드 편집 흐름";
    }
    if (screen.archetype === "PERMISSION_MATRIX") {
      return "READ / CREATE / UPDATE / DELETE 권한 셀과 일괄 저장 상태";
    }
    if (screen.archetype === "CONTENT_EDITOR") {
      return "메뉴 route, 표시 상태, 설명을 편집하는 콘텐츠 에디터";
    }
    return "검색조건, 목록, 상세/폼을 연결한 2단 관리 화면";
  }, [screen.archetype]);

  if (state === "permission") {
    return (
      <section className="content" aria-label={screen.title}>
        <StatePanel
          kind="permission"
          title="권한 없음"
          message={`${screen.title} 화면은 SYSTEM_ADMIN 권한이 필요합니다.`}
        />
      </section>
    );
  }

  return (
    <section className="content" aria-label={screen.title}>
      <div className="page-heading">
        <div>
          <p className="eyebrow">
            {screen.id} · <ArchetypeBadge archetype={screen.archetype} />
          </p>
          <h1>{screen.title}</h1>
          <p>{screen.description}</p>
        </div>
        <button className="button" onClick={createNew}>
          <Plus size={16} />
          신규
        </button>
      </div>

      <div className="filter-card" role="search">
        <label className="input-with-icon">
          <Search size={16} />
          <input
            aria-label={`${screen.title} 검색어`}
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder={`${screen.title} 검색어`}
          />
        </label>
        <button className="button secondary" onClick={() => load()}>
          <RefreshCw size={15} />
          조회
        </button>
        <span className="helper-text">{archetypeHelp}</span>
      </div>

      {state === "loading" && (
        <StatePanel
          kind="loading"
          title="로딩 중"
          message={`${screen.title} 데이터를 불러오는 중입니다.`}
        />
      )}
      {state === "empty" && (
        <StatePanel
          kind="empty"
          title="조회 결과 없음"
          message={`${screen.title} 조회 결과 없음 안내`}
        />
      )}
      {state === "error" && (
        <StatePanel
          kind="error"
          title="오류 발생"
          message={message}
          onRetry={() => load()}
        />
      )}

      <div className={`workbench archetype-${screen.archetype.toLowerCase()}`}>
        <div className="panel list-panel">
          <div className="panel-header">
            <div>
              <h2>{screen.archetype === "TREE_EDITOR" ? "Tree" : "목록"}</h2>
              <p>기본 정렬: name asc · 총 {items.length}건</p>
            </div>
            {screen.archetype === "TREE_EDITOR" && (
              <div className="segmented-controls" aria-label="정렬 제어">
                <button type="button">위</button>
                <button type="button">아래</button>
                <button type="button">상위변경</button>
              </div>
            )}
          </div>

          {screen.archetype === "TREE_EDITOR" ? (
            <div className="tree-list">
              {items.length === 0 ? (
                <div className="inline-empty">
                  <FolderTree size={18} />
                  empty tree 안내
                </div>
              ) : (
                items.map((item) => (
                  <button
                    key={item.id ?? item.name}
                    className={`tree-node ${selected?.id === item.id ? "selected" : ""}`}
                    onClick={() => select(item)}
                  >
                    <ChevronDown size={15} />
                    <span>{item.name}</span>
                    <small>{statusOf(item)}</small>
                  </button>
                ))
              )}
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>코드</th>
                    <th>명칭</th>
                    <th>상태</th>
                    <th>수정일시</th>
                  </tr>
                </thead>
                <tbody>
                  {items.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="empty-cell">
                        {screen.title} 조회 결과 없음 안내
                      </td>
                    </tr>
                  ) : (
                    items.map((item) => (
                      <tr
                        key={item.id ?? item.name}
                        onClick={() => select(item)}
                        className={selected?.id === item.id ? "selected" : ""}
                      >
                        <td>{item.id?.slice(0, 8) ?? "-"}</td>
                        <td>{item.name}</td>
                        <td>
                          <span className="status-pill">{statusOf(item)}</span>
                        </td>
                        <td>{item.updatedAt ?? "-"}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {screen.archetype === "PERMISSION_MATRIX" && (
          <div className="panel matrix-panel">
            <div className="panel-header">
              <div>
                <h2>Permission Matrix</h2>
                <p>dirty cell, validation summary, error rows 표시 영역</p>
              </div>
            </div>
            <div
              className="matrix-grid"
              role="grid"
              aria-label={`${screen.title} 권한 매트릭스`}
            >
              <strong>row label</strong>
              {permissionActions.map((action) => (
                <strong key={action}>{action}</strong>
              ))}
              {items.length === 0 ? (
                <div className="matrix-empty">권한 행이 없습니다.</div>
              ) : (
                items.slice(0, 5).map((item) => [
                  <span key={`${item.id}-label`} className="matrix-label">
                    {item.name}
                  </span>,
                  ...permissionActions.map((action) => (
                    <label key={`${item.id}-${action}`} className="matrix-cell">
                      <input
                        type="checkbox"
                        defaultChecked={statusOf(item) === action}
                        aria-label={`${item.name} ${action}`}
                      />
                    </label>
                  )),
                ])
              )}
            </div>
          </div>
        )}

        <div className="panel form-panel">
          <div className="panel-header">
            <div>
              <h2>
                {screen.archetype === "CONTENT_EDITOR"
                  ? "Content Editor"
                  : "상세/폼"}
              </h2>
              <p>{selected ? "선택 항목 편집" : "신규 항목 등록"}</p>
            </div>
          </div>
          <label>
            name
            <input
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="필수 명칭 입력"
            />
          </label>
          {screen.archetype === "CONTENT_EDITOR" && (
            <label>
              route_path
              <input
                value={selected?.id ? screen.route : ""}
                readOnly
                placeholder="/system/..."
              />
            </label>
          )}
          {options.length > 0 && (
            <label>
              status
              <select
                value={statusValue}
                onChange={(event) => setStatusValue(event.target.value)}
              >
                {options.map((option) => (
                  <option key={option}>{option}</option>
                ))}
              </select>
            </label>
          )}
          <label>
            changeReason
            <textarea
              value={changeReason}
              onChange={(event) => setChangeReason(event.target.value)}
              placeholder="저장/삭제 확인 전에 변경 사유를 입력하세요"
            />
          </label>
          <div className="actions">
            <button className="button" onClick={save}>
              <Save size={15} />
              저장
            </button>
            <button
              className="button danger"
              onClick={remove}
              disabled={!selected}
            >
              <Trash2 size={15} />
              삭제
            </button>
          </div>
          {message && (
            <p className="toast" role="status">
              <CheckCircle2 size={15} />
              {message}
            </p>
          )}
        </div>
      </div>
    </section>
  );
}
