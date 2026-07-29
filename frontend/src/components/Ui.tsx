import React, { useEffect, useState } from "react";
import { ApiError } from "../api/client";

export type Column<T> = {
  key: keyof T;
  label: string;
  render?: (row: T) => React.ReactNode;
};
export type FieldDef = {
  key: string;
  label: string;
  type?: "text" | "select" | "date" | "number" | "textarea";
  options?: string[];
  readOnly?: boolean;
};

export function PageTitle({
  title,
  desc,
  action,
}: {
  title: string;
  desc: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="page-title">
      <div>
        <h1>{title}</h1>
        <p>{desc}</p>
      </div>
      {action}
    </div>
  );
}

export function Badge({
  children,
  tone = "blue",
}: {
  children: React.ReactNode;
  tone?: "blue" | "green" | "red" | "gray";
}) {
  return <span className={`badge ${tone}`}>{children}</span>;
}

export function Field({
  label,
  children,
  error,
}: {
  label: string;
  children: React.ReactNode;
  error?: string;
}) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}

export function StateBanner({
  error,
  success,
}: {
  error?: string;
  success?: string;
}) {
  if (!error && !success) return null;
  return (
    <div className={`state-banner ${error ? "error" : "success"}`}>
      {error || success}
    </div>
  );
}

export function SkeletonBlock() {
  return (
    <div className="skeleton-card">
      <span />
      <span />
      <span />
    </div>
  );
}

export function EmptyState({ title, desc }: { title: string; desc?: string }) {
  return (
    <div className="empty-state">
      <div className="empty-icon">∅</div>
      <b>{title}</b>
      {desc && <p>{desc}</p>}
    </div>
  );
}

export function PermissionState() {
  return (
    <div className="empty-state permission">
      <div className="empty-icon">!</div>
      <b>권한 없음</b>
      <p>이 화면은 R09 시스템관리자 권한이 필요합니다.</p>
    </div>
  );
}

export function DataTable<T extends Record<string, unknown>>({
  items,
  columns,
  onSelect,
  selectedKey,
}: {
  items: T[];
  columns: Column<T>[];
  onSelect: (row: T) => void;
  selectedKey?: string;
}) {
  if (!items.length)
    return (
      <EmptyState
        title="조건에 맞는 결과가 없습니다."
        desc="검색 조건을 조정하거나 새로고침해 주세요."
      />
    );
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={String(c.key)}>{c.label}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {items.map((row) => {
            const key = rowId(row);
            return (
              <tr
                key={key}
                className={selectedKey === key ? "selected" : ""}
                onClick={() => onSelect(row)}
              >
                {columns.map((c) => (
                  <td key={String(c.key)}>
                    {c.render ? c.render(row) : display(row[c.key])}
                  </td>
                ))}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export function rowId(row: Record<string, unknown>) {
  return String(
    row.userId ||
      row.organizationCode ||
      row.roleCode ||
      row.assignmentId ||
      row.permissionId ||
      row.menuId ||
      row.groupId ||
      row.codeValue ||
      "",
  );
}

export function display(value: unknown) {
  if (Array.isArray(value)) return value.join(", ");
  if (value && typeof value === "object") return JSON.stringify(value);
  return String(value ?? "-");
}

export function useLoad<T>(
  loader: () => Promise<T>,
  deps: React.DependencyList,
) {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const reload = () => {
    setLoading(true);
    setError("");
    setFieldErrors({});
    loader()
      .then(setData)
      .catch((e: unknown) => {
        const err = e as ApiError;
        setError(err.message || "조회에 실패했습니다.");
        setFieldErrors(
          Object.fromEntries(
            (err.fieldErrors || []).map((f) => [f.field, f.message]),
          ),
        );
      })
      .finally(() => setLoading(false));
  };
  useEffect(reload, deps);
  return { data, error, fieldErrors, loading, reload, setData };
}

export function SearchBar({ children }: { children: React.ReactNode }) {
  return <div className="card search-card">{children}</div>;
}

export function DetailCard({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <aside className="card detail-card">
      <h2>{title}</h2>
      {children}
    </aside>
  );
}

export function inputFor(
  field: FieldDef,
  value: unknown,
  onChange: (value: string) => void,
  error?: string,
) {
  const common = {
    value: String(value ?? ""),
    onChange: (
      e: React.ChangeEvent<
        HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
      >,
    ) => onChange(e.target.value),
    disabled: field.readOnly,
  };
  return (
    <Field label={field.label} error={error}>
      {field.type === "select" ? (
        <select {...common}>
          {(field.options || ["Y", "N"]).map((o) => (
            <option key={o} value={o}>
              {o}
            </option>
          ))}
        </select>
      ) : field.type === "textarea" ? (
        <textarea {...common} rows={3} />
      ) : (
        <input {...common} type={field.type || "text"} />
      )}
    </Field>
  );
}
