import React, { useMemo, useState } from "react";
import { api, ApiError, qs } from "../api/client";
import {
  Column,
  DataTable,
  DetailCard,
  FieldDef,
  inputFor,
  PageTitle,
  rowId,
  SearchBar,
  SkeletonBlock,
  StateBanner,
  useLoad,
} from "./Ui";

type FilterDef = {
  key: string;
  label: string;
  placeholder?: string;
  type?: "text" | "select";
  options?: string[];
};

type ResourcePageProps<T extends Record<string, unknown>> = {
  title: string;
  desc: string;
  path: string;
  columns: Column<T>[];
  filters?: FilterDef[];
  fields: FieldDef[];
  emptyDetail: string;
  saveLabel?: string;
  deleteLabel?: string;
  buildSave: (
    row: T,
    form: Record<string, unknown>,
  ) => { path: string; method: string; body: Record<string, unknown> };
  buildCreate?: (form: Record<string, unknown>) => {
    path: string;
    method: string;
    body: Record<string, unknown>;
  };
  buildDelete?: (
    row: T,
    form: Record<string, unknown>,
  ) => { path: string; method: string; body: Record<string, unknown> };
  afterSelect?: (row: T) => Record<string, unknown>;
  extraDetail?: (row: T, reload: () => void) => React.ReactNode;
};

export function ResourcePage<T extends Record<string, unknown>>({
  title,
  desc,
  path,
  columns,
  filters = [],
  fields,
  emptyDetail,
  saveLabel = "저장",
  deleteLabel = "삭제",
  buildSave,
  buildCreate,
  buildDelete,
  afterSelect,
  extraDetail,
}: ResourcePageProps<T>) {
  const initialFilters = useMemo(
    () => Object.fromEntries(filters.map((f) => [f.key, ""])),
    [filters],
  );
  const [filter, setFilter] = useState<Record<string, string>>(initialFilters);
  const query = qs(filter);
  const loadPath = query ? `${path}?${query}` : path;
  const { data, error, loading, reload } = useLoad(
    () => api<T[]>(loadPath),
    [loadPath],
  );
  const [selected, setSelected] = useState<T | null>(null);
  const [form, setForm] = useState<Record<string, unknown>>({});
  const [success, setSuccess] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  function selectRow(row: T) {
    setSelected(row);
    setForm({
      ...row,
      ...(afterSelect ? afterSelect(row) : {}),
      changeReason: "",
    });
    setSubmitError("");
    setFieldErrors({});
  }

  function newForm() {
    setSelected(null);
    setForm(
      Object.fromEntries(
        fields.map((f) => [
          f.key,
          f.type === "select" ? f.options?.[0] || "Y" : "",
        ]),
      ),
    );
    setSubmitError("");
    setFieldErrors({});
  }

  function handleError(e: unknown) {
    const err = e as ApiError;
    setSubmitError(err.message || "저장에 실패했습니다.");
    setFieldErrors(
      Object.fromEntries(
        (err.fieldErrors || []).map((f) => [f.field, f.message]),
      ),
    );
  }

  function submit() {
    if (!selected && !buildCreate) return;
    const request = selected ? buildSave(selected, form) : buildCreate!(form);
    if (
      !confirm(
        `${saveLabel}하시겠습니까? 변경 사유와 입력값이 서버에 저장됩니다.`,
      )
    )
      return;
    api(request.path, {
      method: request.method,
      body: JSON.stringify(request.body),
    })
      .then(() => {
        setSuccess("저장되었습니다. 목록을 다시 조회했습니다.");
        setSubmitError("");
        reload();
      })
      .catch(handleError);
  }

  function remove() {
    if (!selected || !buildDelete) return;
    const request = buildDelete(selected, form);
    if (
      !confirm(
        `${deleteLabel}하시겠습니까? 이 작업은 상태 변경으로 추적됩니다.`,
      )
    )
      return;
    api(request.path, {
      method: request.method,
      body: JSON.stringify(request.body),
    })
      .then(() => {
        setSuccess(`${deleteLabel}되었습니다. 목록을 다시 조회했습니다.`);
        reload();
      })
      .catch(handleError);
  }

  return (
    <section className="page-stack">
      <PageTitle
        title={title}
        desc={desc}
        action={
          buildCreate && (
            <button className="btn outline" onClick={newForm}>
              신규 등록
            </button>
          )
        }
      />
      <StateBanner error={error || submitError} success={success} />
      <SearchBar>
        {filters.map((f) => (
          <label key={f.key} className="toolbar-field">
            <span>{f.label}</span>
            {f.type === "select" ? (
              <select
                value={filter[f.key] || ""}
                onChange={(e) =>
                  setFilter({ ...filter, [f.key]: e.target.value })
                }
              >
                <option value="">전체</option>
                {(f.options || []).map((o) => (
                  <option key={o} value={o}>
                    {o}
                  </option>
                ))}
              </select>
            ) : (
              <input
                placeholder={f.placeholder || f.label}
                value={filter[f.key] || ""}
                onChange={(e) =>
                  setFilter({ ...filter, [f.key]: e.target.value })
                }
              />
            )}
          </label>
        ))}
        <button className="btn" onClick={reload}>
          검색
        </button>
        <button className="btn ghost" onClick={() => setFilter(initialFilters)}>
          초기화
        </button>
      </SearchBar>
      <div className="content-grid">
        <div className="card list-card">
          {loading ? (
            <SkeletonBlock />
          ) : (
            <DataTable
              items={data || []}
              columns={columns}
              onSelect={selectRow}
              selectedKey={selected ? rowId(selected) : undefined}
            />
          )}
        </div>
        <DetailCard
          title={
            selected
              ? "선택 항목 상세"
              : buildCreate && Object.keys(form).length
                ? "신규 등록"
                : "상세/편집"
          }
        >
          {selected || Object.keys(form).length ? (
            <form
              className="form-grid"
              onSubmit={(e) => {
                e.preventDefault();
                submit();
              }}
            >
              {fields.map((field) =>
                inputFor(
                  field,
                  form[field.key],
                  (value) => setForm({ ...form, [field.key]: value }),
                  fieldErrors[field.key],
                ),
              )}
              <FieldReason
                value={String(form.changeReason ?? "")}
                error={fieldErrors.changeReason}
                onChange={(value) => setForm({ ...form, changeReason: value })}
              />
              <div className="form-actions">
                <button className="btn" type="submit">
                  {saveLabel}
                </button>
                <button
                  className="btn ghost"
                  type="button"
                  onClick={() => (selected ? selectRow(selected) : setForm({}))}
                >
                  취소
                </button>
                {buildDelete && selected && (
                  <button className="btn danger" type="button" onClick={remove}>
                    {deleteLabel}
                  </button>
                )}
              </div>
              {selected && extraDetail?.(selected, reload)}
            </form>
          ) : (
            <p className="muted">{emptyDetail}</p>
          )}
        </DetailCard>
      </div>
    </section>
  );
}

function FieldReason({
  value,
  onChange,
  error,
}: {
  value: string;
  onChange: (value: string) => void;
  error?: string;
}) {
  return inputFor(
    { key: "changeReason", label: "변경 사유", type: "textarea" },
    value,
    onChange,
    error,
  );
}
