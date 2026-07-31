import { FormEvent, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "./api/client";

type Field = {
  key: string;
  label: string;
  readonly?: boolean;
  type?: "text" | "date" | "number" | "boolean" | "textarea" | "select";
  options?: { value: string; label: string }[];
};
type Action = {
  label: string;
  method: "post" | "put" | "patch" | "delete";
  path: (
    selected: Record<string, unknown>,
    form: Record<string, string>,
  ) => string;
  body?: (
    selected: Record<string, unknown>,
    form: Record<string, string>,
  ) => unknown;
  confirm?: string;
  variant?: "primary" | "secondary" | "destructive";
};
type DetailLink = {
  label: string;
  path: (row: Record<string, unknown>) => string;
};
type Props = {
  title: string;
  description: string;
  queryPath: string;
  fields: Field[];
  filters?: Field[];
  actions?: Action[];
  detailLink?: DetailLink;
};
type ViewState = "loading" | "empty" | "error" | "permission" | "success";

export function ManagementPage({
  title,
  description,
  queryPath,
  fields,
  filters = [],
  actions = [],
  detailLink,
}: Props) {
  const routerNavigate = useNavigate();
  const [rows, setRows] = useState<Record<string, unknown>[]>([]);
  const [selected, setSelected] = useState<Record<string, unknown> | null>(
    null,
  );
  const [filterState, setFilterState] = useState<Record<string, string>>({});
  const [form, setForm] = useState<Record<string, string>>({});
  const [viewState, setViewState] = useState<ViewState>("loading");
  const [message, setMessage] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [saving, setSaving] = useState(false);

  const query = useMemo(() => {
    const params = new URLSearchParams();
    Object.entries(filterState).forEach(([key, value]) => {
      if (value) params.set(key, value);
    });
    return params.toString()
      ? `${queryPath}${queryPath.includes("?") ? "&" : "?"}${params.toString()}`
      : queryPath;
  }, [filterState, queryPath]);

  async function load() {
    setViewState("loading");
    setMessage("");
    setFieldErrors({});
    try {
      const data = await api.get<Record<string, unknown>[]>(query);
      setRows(data);
      const nextSelected = data[0] || null;
      setSelected(nextSelected);
      setForm(toForm(nextSelected || {}, fields));
      setViewState(data.length ? "success" : "empty");
    } catch (error) {
      const apiError = error as Error & {
        status?: number;
        apiError?: { fields?: Record<string, string> };
      };
      setViewState(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setMessage(error instanceof Error ? error.message : "조회 오류");
      setFieldErrors(apiError.apiError?.fields || {});
    }
  }

  useEffect(() => {
    load();
  }, [query]);

  function select(row: Record<string, unknown>) {
    setSelected(row);
    setForm(toForm(row, fields));
    setFieldErrors({});
    setMessage("");
  }

  function startNew() {
    setSelected(null);
    setForm(Object.fromEntries(fields.map((field) => [field.key, ""])));
    setFieldErrors({});
    setMessage("신규 등록 값을 입력하세요.");
  }

  async function request(action: Action, target: Record<string, unknown>) {
    const path = action.path(target, form);
    const body = action.body ? action.body(target, form) : undefined;
    if (action.method === "delete") return api.delete<unknown>(path);
    if (action.method === "patch") return api.patch<unknown>(path, body);
    if (action.method === "put") return api.put<unknown>(path, body);
    return api.post<unknown>(path, body);
  }

  async function run(action: Action) {
    if (!selected && action.method !== "post") {
      setMessage("목록에서 저장 대상을 선택하세요.");
      return;
    }
    if (action.confirm && !window.confirm(action.confirm)) return;
    setSaving(true);
    setMessage("");
    setFieldErrors({});
    try {
      await request(action, selected || {});
      setViewState("success");
      setMessage(`${action.label} 완료 후 목록을 다시 조회했습니다.`);
      await load();
    } catch (error) {
      const apiError = error as Error & {
        status?: number;
        apiError?: { fields?: Record<string, string> };
      };
      setViewState(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setMessage(error instanceof Error ? error.message : "저장 오류");
      setFieldErrors(apiError.apiError?.fields || {});
    } finally {
      setSaving(false);
    }
  }

  const selectedKey = selected ? rowKey(selected, fields) : "__new__";
  const canCreate = actions.some((action) => action.method === "post");

  return (
    <section className="page">
      <div className="page-heading">
        <p className="eyebrow">API-backed management</p>
        <div className="title-row">
          <div>
            <h1>{title}</h1>
            <p>{description}</p>
          </div>
          {canCreate && (
            <button
              className="button secondary"
              type="button"
              onClick={startNew}
            >
              신규 입력
            </button>
          )}
        </div>
      </div>

      <form
        className="filters toolbar-card"
        onSubmit={(event: FormEvent) => {
          event.preventDefault();
          load();
        }}
      >
        {filters.map((field) => (
          <FieldControl
            key={field.key}
            field={field}
            value={filterState[field.key] || ""}
            onChange={(value) =>
              setFilterState({ ...filterState, [field.key]: value })
            }
          />
        ))}
        <div className="filter-actions">
          <button
            className="button"
            disabled={viewState === "loading" || saving}
          >
            검색
          </button>
          <button
            className="button secondary"
            type="button"
            onClick={() => setFilterState({})}
          >
            초기화
          </button>
        </div>
      </form>

      <StateBanner state={viewState} message={message} onRetry={load} />

      <div className="split">
        <div className="table-card">
          <div className="table-header">
            <strong>조회 결과</strong>
            <span>{rows.length}건</span>
          </div>
          <div className="table-scroll">
            <table>
              <thead>
                <tr>
                  {fields.map((field) => (
                    <th key={field.key}>{field.label}</th>
                  ))}
                  {detailLink && <th>연결</th>}
                </tr>
              </thead>
              <tbody>
                {viewState === "loading" ? (
                  <SkeletonRows fields={fields} />
                ) : rows.length ? (
                  rows.map((row, index) => (
                    <tr
                      key={rowKey(row, fields) || index}
                      onClick={() => select(row)}
                      className={
                        rowKey(row, fields) === selectedKey ? "selected" : ""
                      }
                    >
                      {fields.map((field) => (
                        <td key={field.key}>
                          {formatCell(row[field.key], field)}
                        </td>
                      ))}
                      {detailLink && (
                        <td>
                          <a
                            className="table-link"
                            href={detailLink.path(row)}
                            onClick={(event) => {
                              event.preventDefault();
                              routerNavigate(detailLink.path(row));
                            }}
                          >
                            {detailLink.label}
                          </a>
                        </td>
                      )}
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td
                      className="empty-cell"
                      colSpan={fields.length + (detailLink ? 1 : 0)}
                    >
                      <div className="empty-state">
                        <strong>조회 결과가 없습니다</strong>
                        <span>
                          검색 조건을 조정하거나 신규 등록을 시작하세요.
                        </span>
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        <aside className="editor" aria-label="상세 편집 패널">
          <div className="editor-header">
            <div>
              <p className="eyebrow">detail</p>
              <h2>{selected ? "상세/편집" : "신규 입력"}</h2>
            </div>
            {selected && <span className="badge">선택됨</span>}
          </div>
          <div className="form-grid">
            {fields.map((field) => (
              <FieldControl
                key={field.key}
                field={field}
                value={form[field.key] || ""}
                error={fieldErrors[field.key]}
                onChange={(value) => setForm({ ...form, [field.key]: value })}
              />
            ))}
          </div>
          <div className="actions">
            {actions.map((action) => (
              <button
                className={`button ${action.variant ?? (action.method === "delete" ? "destructive" : "")}`}
                disabled={saving || viewState === "loading"}
                key={action.label}
                onClick={() => run(action)}
                type="button"
              >
                {saving ? "처리 중..." : action.label}
              </button>
            ))}
            <button
              className="button secondary"
              type="button"
              onClick={() => (selected ? select(selected) : startNew())}
            >
              취소
            </button>
          </div>
        </aside>
      </div>
    </section>
  );
}

function FieldControl({
  field,
  value,
  error,
  onChange,
}: {
  field: Field;
  value: string;
  error?: string;
  onChange: (value: string) => void;
}) {
  const type = field.type || inferType(field);
  return (
    <label className={field.readonly ? "readonly-field" : ""}>
      <span>{field.label}</span>
      {type === "textarea" ? (
        <textarea
          readOnly={field.readonly}
          value={value}
          onChange={(event) => onChange(event.target.value)}
        />
      ) : type === "boolean" ? (
        <select
          disabled={field.readonly}
          value={value}
          onChange={(event) => onChange(event.target.value)}
        >
          <option value="">전체/미선택</option>
          <option value="true">사용</option>
          <option value="false">중지</option>
        </select>
      ) : type === "select" && field.options ? (
        <select
          disabled={field.readonly}
          value={value}
          onChange={(event) => onChange(event.target.value)}
        >
          <option value="">선택</option>
          {field.options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      ) : (
        <input
          readOnly={field.readonly}
          type={type === "date" || type === "number" ? type : "text"}
          value={value}
          onChange={(event) => onChange(event.target.value)}
        />
      )}
      {field.readonly && <small>KORUS/API 조회 전용</small>}
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}

function StateBanner({
  state,
  message,
  onRetry,
}: {
  state: ViewState;
  message: string;
  onRetry: () => void;
}) {
  const text: Record<ViewState, string> = {
    loading: "데이터를 조회하는 중입니다.",
    empty: "조회 결과가 없습니다. 필터를 초기화하거나 신규 등록을 진행하세요.",
    error: message || "오류가 발생했습니다.",
    permission:
      "권한 없음 또는 로그인이 필요합니다. 401은 로그인 화면으로, 403은 화면 내 안내로 처리됩니다.",
    success: message || "정상 조회되었습니다.",
  };
  return (
    <div className={`state ${state}`} role="status">
      <span>{text[state]}</span>
      {(state === "error" || state === "permission") && (
        <button className="button secondary" type="button" onClick={onRetry}>
          다시 시도
        </button>
      )}
    </div>
  );
}

function SkeletonRows({ fields }: { fields: Field[] }) {
  return (
    <>
      {[0, 1, 2, 3].map((row) => (
        <tr key={row}>
          {fields.map((field) => (
            <td key={field.key}>
              <span className="skeleton-line" />
            </td>
          ))}
        </tr>
      ))}
    </>
  );
}

function toForm(row: Record<string, unknown>, fields: Field[]) {
  return Object.fromEntries(
    fields.map((field) => [field.key, serializeValue(row[field.key])]),
  );
}

function serializeValue(value: unknown) {
  if (Array.isArray(value)) return value.join(",");
  if (value == null) return "";
  return String(value);
}

function inferType(field: Field): Field["type"] {
  if (
    field.key === "description" ||
    field.key === "purpose" ||
    field.key === "grantCriteria"
  )
    return "textarea";
  if (
    field.key.startsWith("is") ||
    field.key.endsWith("Enabled") ||
    field.key === "allowed"
  )
    return "boolean";
  if (
    field.key.toLowerCase().includes("date") ||
    field.key === "validFrom" ||
    field.key === "validTo"
  )
    return "date";
  if (field.key.toLowerCase().includes("order") || field.key === "menuLevel")
    return field.key === "menuLevel" ? "text" : "number";
  return field.type || "text";
}

function formatCell(value: unknown, field: Field) {
  if (Array.isArray(value)) {
    return (
      <span className="badge-row">
        {value.map((item) => (
          <span className="badge" key={String(item)}>
            {String(item)}
          </span>
        ))}
      </span>
    );
  }
  if (typeof value === "boolean" || inferType(field) === "boolean") {
    const active = value === true || value === "true";
    return (
      <span className={`badge ${active ? "success" : "muted"}`}>
        {active ? "사용" : "중지"}
      </span>
    );
  }
  if (field.key === "status" && value)
    return <span className="badge">{String(value)}</span>;
  return String(value ?? "");
}

function rowKey(row: Record<string, unknown>, fields: Field[]) {
  const preferred = [
    "id",
    "userId",
    "relationId",
    "roleCode",
    "assignmentId",
    "permissionId",
    "menuId",
    "groupId",
    "codeValue",
  ];
  const key =
    preferred.find((candidate) => row[candidate] != null) || fields[0]?.key;
  return String(row[key] ?? "");
}
