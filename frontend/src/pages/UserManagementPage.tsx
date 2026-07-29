import { useState } from "react";
import { api, qs } from "../api/client";
import {
  Badge,
  DataTable,
  DetailCard,
  Field,
  PageTitle,
  SearchBar,
  SkeletonBlock,
  StateBanner,
  useLoad,
} from "../components/Ui";
import type { Role, User } from "../types";

export function UserManagementPage() {
  const [filter, setFilter] = useState({
    employeeNo: "",
    name: "",
    organization: "",
    jobGrade: "",
    employmentStatus: "",
    roleCode: "",
    systemUseYn: "",
  });
  const query = qs(filter);
  const { data, error, loading, reload } = useLoad(
    () => api<User[]>(`/api/admin/users${query ? `?${query}` : ""}`),
    [query],
  );
  const roles = useLoad(() => api<Role[]>("/api/admin/roles"), []);
  const [selected, setSelected] = useState<User | null>(null);
  const [form, setForm] = useState({
    systemUseYn: "Y",
    roleCodes: "",
    approverUserId: "admin",
    validFrom: new Date().toISOString().slice(0, 10),
    validTo: "",
    changeReason: "",
  });
  const [message, setMessage] = useState("");
  const [submitError, setSubmitError] = useState("");

  function select(row: User) {
    setSelected(row);
    setForm({
      systemUseYn: row.systemUseYn,
      roleCodes: row.roleCodes.join(","),
      approverUserId: "admin",
      validFrom: new Date().toISOString().slice(0, 10),
      validTo: "",
      changeReason: "",
    });
  }

  async function save() {
    if (!selected) return;
    if (!form.changeReason || !form.approverUserId || !form.validFrom) {
      setSubmitError("변경 사유, 승인자, 유효시작일은 필수입니다.");
      return;
    }
    if (!confirm("사용여부와 업무 역할 변경을 저장하시겠습니까?")) return;
    try {
      await api(`/api/admin/users/${selected.userId}/access`, {
        method: "PATCH",
        body: JSON.stringify({
          systemUseYn: form.systemUseYn,
          changeReason: form.changeReason,
        }),
      });
      await api(`/api/admin/users/${selected.userId}/roles`, {
        method: "PUT",
        body: JSON.stringify({
          roleCodes: form.roleCodes
            .split(",")
            .map((x) => x.trim())
            .filter(Boolean),
          approverUserId: form.approverUserId,
          validFrom: form.validFrom,
          validTo: form.validTo || undefined,
          changeReason: form.changeReason,
        }),
      });
      setMessage("저장되었습니다. 사용자 목록을 다시 조회했습니다.");
      setSubmitError("");
      reload();
    } catch (e) {
      setSubmitError((e as Error).message);
    }
  }

  return (
    <section className="page-stack">
      <PageTitle
        title="사용자 관리"
        desc="KORUS 인사 snapshot은 읽기 전용으로 표시하고, 로컬 시스템 사용여부·업무역할만 저장합니다."
      />
      <StateBanner error={error || submitError} success={message} />
      <SearchBar>
        {Object.entries({
          employeeNo: "교번",
          name: "성명",
          organization: "소속",
          jobGrade: "직급",
        }).map(([key, label]) => (
          <label className="toolbar-field" key={key}>
            <span>{label}</span>
            <input
              value={filter[key as keyof typeof filter]}
              onChange={(e) => setFilter({ ...filter, [key]: e.target.value })}
            />
          </label>
        ))}
        <label className="toolbar-field">
          <span>재직상태</span>
          <input
            value={filter.employmentStatus}
            onChange={(e) =>
              setFilter({ ...filter, employmentStatus: e.target.value })
            }
          />
        </label>
        <label className="toolbar-field">
          <span>역할</span>
          <select
            value={filter.roleCode}
            onChange={(e) => setFilter({ ...filter, roleCode: e.target.value })}
          >
            <option value="">전체</option>
            {(roles.data || []).map((r) => (
              <option key={r.roleCode} value={r.roleCode}>
                {r.roleCode} {r.roleName}
              </option>
            ))}
          </select>
        </label>
        <label className="toolbar-field">
          <span>사용</span>
          <select
            value={filter.systemUseYn}
            onChange={(e) =>
              setFilter({ ...filter, systemUseYn: e.target.value })
            }
          >
            <option value="">전체</option>
            <option>Y</option>
            <option>N</option>
          </select>
        </label>
        <button className="btn" onClick={reload}>
          검색
        </button>
      </SearchBar>
      <div className="content-grid">
        <div className="card list-card">
          {loading ? (
            <SkeletonBlock />
          ) : (
            <DataTable
              items={data || []}
              onSelect={select}
              columns={[
                { key: "userId", label: "교번" },
                { key: "personName", label: "성명" },
                { key: "organizationName", label: "소속" },
                { key: "jobGrade", label: "직급" },
                { key: "employmentStatus", label: "재직" },
                {
                  key: "roleCodes",
                  label: "역할",
                  render: (r) =>
                    r.roleCodes.map((x) => <Badge key={x}>{x}</Badge>),
                },
                {
                  key: "systemUseYn",
                  label: "사용",
                  render: (r) => (
                    <Badge tone={r.systemUseYn === "Y" ? "green" : "red"}>
                      {r.systemUseYn}
                    </Badge>
                  ),
                },
                { key: "positionName", label: "보직" },
                { key: "retiredAt", label: "퇴직일자" },
                { key: "lastSyncedAt", label: "동기화" },
              ]}
            />
          )}
        </div>
        <DetailCard title="선택 사용자 상세/편집">
          {selected ? (
            <div className="form-grid">
              <div className="readonly-panel">
                <b>{selected.personName}</b>
                <p>
                  {selected.organizationName} · {selected.jobGrade} ·{" "}
                  {selected.positionName}
                </p>
                <p>
                  퇴직일자 {selected.retiredAt || "-"} / 최종 동기화{" "}
                  {selected.lastSyncedAt}
                </p>
              </div>
              <Field label="시스템 사용여부">
                <select
                  value={form.systemUseYn}
                  onChange={(e) =>
                    setForm({ ...form, systemUseYn: e.target.value })
                  }
                >
                  <option>Y</option>
                  <option>N</option>
                </select>
              </Field>
              <Field label="업무 역할(R01~R09, 쉼표 구분)">
                <input
                  value={form.roleCodes}
                  onChange={(e) =>
                    setForm({ ...form, roleCodes: e.target.value })
                  }
                />
              </Field>
              <Field label="승인자">
                <input
                  value={form.approverUserId}
                  onChange={(e) =>
                    setForm({ ...form, approverUserId: e.target.value })
                  }
                />
              </Field>
              <Field label="유효시작">
                <input
                  type="date"
                  value={form.validFrom}
                  onChange={(e) =>
                    setForm({ ...form, validFrom: e.target.value })
                  }
                />
              </Field>
              <Field label="유효종료">
                <input
                  type="date"
                  value={form.validTo}
                  onChange={(e) =>
                    setForm({ ...form, validTo: e.target.value })
                  }
                />
              </Field>
              <Field label="변경 사유">
                <textarea
                  rows={3}
                  value={form.changeReason}
                  onChange={(e) =>
                    setForm({ ...form, changeReason: e.target.value })
                  }
                />
              </Field>
              <div className="form-actions">
                <button className="btn" onClick={save}>
                  저장
                </button>
                <button className="btn ghost" onClick={() => select(selected)}>
                  취소
                </button>
              </div>
            </div>
          ) : (
            <p className="muted">
              목록 행을 선택하면 KORUS 읽기전용 정보와 저장 가능한 내부 권한
              필드가 표시됩니다.
            </p>
          )}
        </DetailCard>
      </div>
    </section>
  );
}
