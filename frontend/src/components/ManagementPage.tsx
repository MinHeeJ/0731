import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Database,
  Loader2,
  Plus,
  RotateCcw,
  Save,
  Search,
  ShieldAlert,
  Trash2,
} from "lucide-react";
import { ChangeEvent, FormEvent, useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { ApiErrorShape, Row } from "../api/client";
import { FieldConfig, ScreenConfig } from "../screens";

type ViewState = "loading" | "empty" | "error" | "permission" | "success";

type Notice = { type: "success" | "error"; text: string } | null;

function stringifyValue(value: unknown) {
  if (Array.isArray(value)) return value.join(",");
  if (typeof value === "boolean") return value ? "true" : "false";
  if (value === null || value === undefined) return "";
  if (typeof value === "object") return JSON.stringify(value);
  return String(value);
}

function parseDraftValue(field: FieldConfig, value: string) {
  if (field.type === "number") return value === "" ? null : Number(value);
  if (field.type === "checkbox") return value === "true";
  return value;
}

export function ManagementPage({ screen }: { screen: ScreenConfig }) {
  const [searchParams] = useSearchParams();
  const initialParams = useMemo(() => {
    const params: Record<string, string> = { ...(screen.defaultParams ?? {}) };
    screen.filters.forEach((field) => {
      const value = searchParams.get(field.key);
      if (value) params[field.key] = value;
    });
    return params;
  }, [screen, searchParams]);

  const [filters, setFilters] = useState<Record<string, string>>(initialParams);
  const [rows, setRows] = useState<Row[]>([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [draft, setDraft] = useState<Row>({});
  const [viewState, setViewState] = useState<ViewState>("loading");
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState<Notice>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const selected = rows[selectedIndex];

  const load = async (nextFilters = filters) => {
    setViewState("loading");
    setNotice(null);
    setFieldErrors({});
    try {
      const data = await screen.read(nextFilters);
      setRows(data);
      setSelectedIndex(0);
      setDraft(data[0] ?? seedDraft(screen, nextFilters));
      setViewState(data.length ? "success" : "empty");
    } catch (err) {
      const apiError = err as ApiErrorShape;
      setViewState(
        apiError.status === 401 || apiError.status === 403
          ? "permission"
          : "error",
      );
      setNotice({
        type: "error",
        text: err instanceof Error ? err.message : "조회 실패",
      });
    }
  };

  useEffect(() => {
    setFilters(initialParams);
    void load(initialParams);
  }, [screen.route]);

  useEffect(() => {
    setDraft(selected ?? seedDraft(screen, filters));
    setFieldErrors({});
  }, [selectedIndex, selected, screen.route]);

  const kpis = useMemo(() => {
    const active = rows.filter(
      (row) =>
        row.useYn === "Y" ||
        row.accessAllowed === true ||
        row.status === "ACTIVE",
    ).length;
    const inactive = rows.filter(
      (row) =>
        row.useYn === "N" ||
        row.accessAllowed === false ||
        row.status === "REVOKED",
    ).length;
    return [
      { label: "전체", value: rows.length, helper: "현재 조회 결과" },
      { label: "사용/허용", value: active, helper: "Y, ACTIVE, 허용" },
      { label: "비활성/차단", value: inactive, helper: "N, REVOKED, 차단" },
      { label: "상태", value: stateLabel(viewState), helper: screen.archetype },
    ];
  }, [rows, screen.archetype, viewState]);

  const updateFilter = (key: string, value: string) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
  };

  const updateDraft = (field: FieldConfig, value: string) => {
    setDraft((prev) => ({
      ...prev,
      [field.key]: parseDraftValue(field, value),
    }));
  };

  const submit = async (
    event: FormEvent,
    mode: "save" | "create" | "delete",
  ) => {
    event.preventDefault();
    setBusy(true);
    setNotice(null);
    setFieldErrors({});
    try {
      if (mode === "create") {
        if (!screen.create)
          throw new Error("등록 API가 정의되어 있지 않습니다.");
        await screen.create(draft);
      } else if (mode === "delete") {
        if (!screen.remove)
          throw new Error("회수 API가 정의되어 있지 않습니다.");
        await screen.remove(draft, stringifyValue(draft.reason));
      } else if (screen.supportsMatrix) {
        await screen.save?.({
          targetType: filters.targetType || "ROLE",
          targetId: filters.targetId || "R09",
          permissions: rows.map((row) => ({
            menuId: row.menuId,
            accessAllowed: Boolean(row.accessAllowed),
          })),
          reason: draft.reason,
        });
      } else {
        if (!screen.save) throw new Error("저장 API가 정의되어 있지 않습니다.");
        await screen.save(draft, selected);
      }
      setNotice({
        type: "success",
        text:
          mode === "delete"
            ? "회수 처리가 완료되었습니다."
            : "저장 후 목록을 갱신했습니다.",
      });
      await load(filters);
    } catch (err) {
      const apiError = err as ApiErrorShape;
      const errors = apiError.error?.errors ?? [];
      setFieldErrors(
        errors.reduce<Record<string, string>>((acc, item) => {
          acc[item.field] = item.reason;
          return acc;
        }, {}),
      );
      setNotice({
        type: "error",
        text: err instanceof Error ? err.message : "저장 실패",
      });
    } finally {
      setBusy(false);
    }
  };

  const togglePermission = (index: number, checked: boolean) => {
    setRows((prev) =>
      prev.map((row, rowIndex) =>
        rowIndex === index ? { ...row, accessAllowed: checked } : row,
      ),
    );
  };

  return (
    <main className="flex flex-1 flex-col gap-4 px-4 py-6 sm:gap-6 lg:px-6">
      <section className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <span className="badge">{screen.archetype}</span>
            {screen.operationIds.map((operation) => (
              <span className="badge" key={operation}>
                {operation}
              </span>
            ))}
          </div>
          <h2 className="mt-2 text-2xl font-bold tracking-tight">
            {screen.title}
          </h2>
          <p className="mt-1 max-w-3xl text-sm text-muted-foreground">
            {screen.guidance}
          </p>
        </div>
        {screen.route === "/admin/code-groups" ? (
          <Link
            className="btn btn-outline"
            to={`/admin/code-details?groupId=${encodeURIComponent(String(draft.groupId ?? filters.groupId ?? "EVAL_AREA"))}`}
          >
            상세코드 이동
          </Link>
        ) : null}
      </section>

      <div className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
        decision_status=clarification_required: 실행·영속성 명확화 전
        build-ready handoff 차단 상태입니다.
      </div>

      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {kpis.map((item) => (
          <article className="card gap-4" key={item.label}>
            <div className="flex items-center justify-between px-6 pt-5">
              <p className="text-sm font-medium text-muted-foreground">
                {item.label}
              </p>
              <Activity className="text-muted-foreground" size={16} />
            </div>
            <div className="px-6 pb-5">
              <p className="text-2xl font-bold tracking-tight">
                {String(item.value)}
              </p>
              <p className="mt-1 text-xs text-muted-foreground">
                {item.helper}
              </p>
            </div>
          </article>
        ))}
      </section>

      <section className="card">
        <form
          className="flex flex-col gap-3 px-6 pt-5 lg:flex-row lg:items-end"
          onSubmit={(event) => {
            event.preventDefault();
            void load(filters);
          }}
        >
          {screen.filters.length === 0 ? (
            <div className="text-sm text-muted-foreground">
              필터 없이 서버 기준 트리를 조회합니다.
            </div>
          ) : null}
          {screen.filters.map((field) => (
            <FilterControl
              key={field.key}
              field={field}
              value={filters[field.key] ?? ""}
              onChange={(value) => updateFilter(field.key, value)}
            />
          ))}
          <button className="btn btn-primary lg:ms-auto" type="submit">
            <Search size={16} /> 조회
          </button>
          <button
            className="btn btn-outline"
            type="button"
            onClick={() => {
              setFilters(screen.defaultParams ?? {});
              void load(screen.defaultParams ?? {});
            }}
          >
            <RotateCcw size={16} /> 초기화
          </button>
        </form>
        <div className="px-6 pb-6 pt-4">
          {notice ? <NoticePanel notice={notice} /> : null}
          <DataArea
            screen={screen}
            rows={rows}
            state={viewState}
            selectedIndex={selectedIndex}
            emptyText={screen.emptyText}
            onSelect={setSelectedIndex}
            onTogglePermission={togglePermission}
          />
        </div>
      </section>

      <section className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_420px]">
        <article className="card">
          <div className="px-6 pt-5">
            <h3 className="text-base font-semibold">상태 흐름</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              loading / empty / error / permission / success 상태를 분리
              표시하고 실패 시 API 오류 메시지를 보존합니다.
            </p>
          </div>
          <div className="grid gap-3 px-6 pb-6 sm:grid-cols-5">
            {["loading", "empty", "error", "permission", "success"].map(
              (state) => (
                <div
                  className={`rounded-md border p-3 text-sm ${viewState === state ? "border-primary bg-primary/5 text-primary" : "bg-card"}`}
                  key={state}
                >
                  {state}
                </div>
              ),
            )}
          </div>
        </article>
        <form className="card" onSubmit={(event) => void submit(event, "save")}>
          <div className="px-6 pt-5">
            <h3 className="text-base font-semibold">선택 상세 / 편집</h3>
            <p className="mt-1 text-sm text-muted-foreground">
              행 선택 후 실제 backend mutation API로 저장합니다.
            </p>
          </div>
          <div className="space-y-4 px-6 pb-6">
            {screen.readonlyFields.length > 0 ? (
              <div className="rounded-lg border bg-muted/40 p-3">
                <p className="mb-2 text-xs font-semibold text-muted-foreground">
                  읽기 전용 원천/식별 필드
                </p>
                <div className="grid gap-2 sm:grid-cols-2">
                  {screen.readonlyFields.map((field) => (
                    <ReadOnlyField
                      field={field}
                      key={field.key}
                      value={draft[field.key]}
                    />
                  ))}
                </div>
              </div>
            ) : null}
            <div className="grid gap-3">
              {screen.editableFields.map((field) => (
                <DraftControl
                  field={field}
                  error={fieldErrors[field.key]}
                  key={field.key}
                  value={stringifyValue(draft[field.key])}
                  onChange={(value) => updateDraft(field, value)}
                />
              ))}
            </div>
            <div className="flex flex-wrap gap-2">
              <button
                className="btn btn-primary"
                disabled={busy || screen.supportsMatrix}
                type="submit"
              >
                {busy ? (
                  <Loader2 className="animate-spin" size={16} />
                ) : (
                  <Save size={16} />
                )}
                수정 저장
              </button>
              {screen.supportsCreate ? (
                <button
                  className="btn btn-outline"
                  disabled={busy}
                  onClick={(event) => void submit(event, "create")}
                  type="button"
                >
                  <Plus size={16} /> 등록
                </button>
              ) : null}
              {screen.remove ? (
                <button
                  className="btn btn-destructive"
                  disabled={busy}
                  onClick={(event) => void submit(event, "delete")}
                  type="button"
                >
                  <Trash2 size={16} /> 회수
                </button>
              ) : null}
              {screen.supportsMatrix ? (
                <button
                  className="btn btn-primary"
                  disabled={busy}
                  onClick={(event) => void submit(event, "save")}
                  type="button"
                >
                  <Save size={16} /> 권한 저장
                </button>
              ) : null}
              <button
                className="btn btn-outline"
                type="button"
                onClick={() => setDraft(selected ?? seedDraft(screen, filters))}
              >
                취소
              </button>
            </div>
          </div>
        </form>
      </section>
    </main>
  );
}

function DataArea({
  screen,
  rows,
  state,
  selectedIndex,
  emptyText,
  onSelect,
  onTogglePermission,
}: {
  screen: ScreenConfig;
  rows: Row[];
  state: ViewState;
  selectedIndex: number;
  emptyText: string;
  onSelect: (index: number) => void;
  onTogglePermission: (index: number, checked: boolean) => void;
}) {
  if (state === "loading") {
    return (
      <StatePanel
        icon={<Loader2 className="animate-spin" />}
        title="로딩 중"
        description="DB/API에서 최신 데이터를 조회하고 있습니다."
      />
    );
  }
  if (state === "empty") {
    return (
      <StatePanel
        icon={<Database />}
        title="데이터 없음"
        description={emptyText}
      />
    );
  }
  if (state === "permission") {
    return (
      <StatePanel
        icon={<ShieldAlert />}
        title="권한 또는 인증 필요"
        description="401 인증 필요와 403 권한 없음을 이 영역에서 구분 표시합니다. R09 세션으로 로그인하세요."
      />
    );
  }
  if (state === "error") {
    return (
      <StatePanel
        icon={<AlertTriangle />}
        title="조회 오류"
        description="서버 요청 처리 중 오류가 발생했습니다. 상단 오류 메시지를 확인하세요."
      />
    );
  }

  return (
    <div className="overflow-hidden rounded-md border">
      <div className="no-scrollbar overflow-auto">
        <table className="w-full min-w-[880px] caption-bottom text-sm">
          <thead>
            <tr className="border-b bg-muted/50">
              {screen.columns.map((column) => (
                <th
                  className="h-10 px-3 text-left align-middle text-xs font-medium text-muted-foreground"
                  key={column.key}
                >
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, index) => (
              <tr
                className={`group/row cursor-pointer border-b transition-colors hover:bg-muted/50 ${selectedIndex === index ? "bg-muted" : ""}`}
                data-state={selectedIndex === index ? "selected" : undefined}
                key={`${screen.route}-${index}`}
                onClick={() => onSelect(index)}
              >
                {screen.columns.map((column) => (
                  <td className="p-3 align-middle" key={column.key}>
                    {column.key === "accessAllowed" && screen.supportsMatrix ? (
                      <input
                        checked={Boolean(row.accessAllowed)}
                        onChange={(event: ChangeEvent<HTMLInputElement>) =>
                          onTogglePermission(index, event.target.checked)
                        }
                        onClick={(event) => event.stopPropagation()}
                        type="checkbox"
                      />
                    ) : column.key === "useYn" ||
                      column.key === "status" ||
                      column.key === "accessAllowed" ? (
                      <span className={statusClass(row[column.key])}>
                        {stringifyValue(row[column.key])}
                      </span>
                    ) : (
                      <span className="line-clamp-2 max-w-[240px]">
                        {stringifyValue(row[column.key]) || "-"}
                      </span>
                    )}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function FilterControl({
  field,
  value,
  onChange,
}: {
  field: FieldConfig;
  value: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="grid min-w-40 gap-2 text-sm font-medium">
      {field.label}
      {field.type === "select" ? (
        <select
          value={value}
          onChange={(event) => onChange(event.target.value)}
        >
          <option value="">전체</option>
          {field.options?.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      ) : (
        <input
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder={field.label}
        />
      )}
    </label>
  );
}

function DraftControl({
  field,
  value,
  error,
  onChange,
}: {
  field: FieldConfig;
  value: string;
  error?: string;
  onChange: (value: string) => void;
}) {
  return (
    <label className="grid gap-2 text-sm font-medium">
      {field.label}
      {field.type === "textarea" ? (
        <textarea
          rows={3}
          value={value}
          onChange={(event) => onChange(event.target.value)}
        />
      ) : field.type === "select" ? (
        <select
          value={value}
          onChange={(event) => onChange(event.target.value)}
        >
          <option value="">선택</option>
          {field.options?.map((option) => (
            <option key={option} value={option}>
              {option}
            </option>
          ))}
        </select>
      ) : (
        <input
          type={
            field.type === "number"
              ? "number"
              : field.type === "date"
                ? "date"
                : "text"
          }
          value={value}
          onChange={(event) => onChange(event.target.value)}
        />
      )}
      {field.help ? (
        <span className="text-xs text-muted-foreground">{field.help}</span>
      ) : null}
      {error ? <span className="field-error">{error}</span> : null}
    </label>
  );
}

function ReadOnlyField({
  field,
  value,
}: {
  field: FieldConfig;
  value: unknown;
}) {
  return (
    <div>
      <p className="text-xs text-muted-foreground">{field.label}</p>
      <p className="truncate text-sm font-medium">
        {stringifyValue(value) || "-"}
      </p>
    </div>
  );
}

function StatePanel({
  icon,
  title,
  description,
}: {
  icon: JSX.Element;
  title: string;
  description: string;
}) {
  return (
    <div className="flex min-h-32 items-center gap-3 rounded-md border border-dashed p-8 text-muted-foreground">
      <span className="grid size-10 place-items-center rounded-full bg-muted">
        {icon}
      </span>
      <div>
        <h3 className="font-semibold text-foreground">{title}</h3>
        <p className="text-sm">{description}</p>
      </div>
    </div>
  );
}

function NoticePanel({ notice }: { notice: NonNullable<Notice> }) {
  return (
    <div
      className={`mb-4 flex items-center gap-2 rounded-md border px-3 py-2 text-sm ${notice.type === "success" ? "border-emerald-200 bg-emerald-50 text-emerald-700" : "border-red-200 bg-red-50 text-red-700"}`}
    >
      {notice.type === "success" ? (
        <CheckCircle2 size={16} />
      ) : (
        <AlertTriangle size={16} />
      )}
      {notice.text}
    </div>
  );
}

function seedDraft(screen: ScreenConfig, params: Record<string, string>) {
  return screen.editableFields.reduce<Row>((acc, field) => {
    acc[field.key] = params[field.key] ?? (field.key === "useYn" ? "Y" : "");
    return acc;
  }, {});
}

function stateLabel(state: ViewState) {
  return {
    loading: "로딩",
    empty: "없음",
    error: "오류",
    permission: "권한",
    success: "성공",
  }[state];
}

function statusClass(value: unknown) {
  const normalized = stringifyValue(value);
  if (["Y", "ACTIVE", "true"].includes(normalized))
    return "badge badge-success";
  if (["N", "REVOKED", "false"].includes(normalized))
    return "badge badge-danger";
  return "badge";
}
