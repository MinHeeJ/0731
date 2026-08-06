import { FormEvent, useEffect, useState } from "react";
import { useAuth } from "../auth/AuthProvider";
import { FieldError, StateBanner } from "../components/State";
import {
  listSystemConfigs,
  SystemConfig,
  updateSystemConfig,
} from "../api/systemConfigApi";

function formatRange(config: SystemConfig) {
  const min = config.minValue || "-";
  const max = config.maxValue || "-";
  return `허용 범위: ${min} ~ ${max} ${config.unit}`;
}

function testIdKey(settingKey: string) {
  return settingKey.toLowerCase().replace(/_/g, "-");
}

export function SystemConfigPage() {
  const { user } = useAuth();
  const isR09 = user?.roles?.includes("R09") ?? false;
  const [rows, setRows] = useState<SystemConfig[]>([]);
  const [selected, setSelected] = useState<SystemConfig | null>(null);
  const [settingValue, setSettingValue] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [state, setState] = useState<
    "empty" | "error" | "success" | "permission" | ""
  >("");
  const [fields, setFields] = useState<Record<string, string>>({});

  async function load() {
    setLoading(true);
    setMessage("환경설정 목록을 조회 중입니다.");
    setState("");
    const response = await listSystemConfigs();
    setLoading(false);
    if (response.success) {
      const data = response.data || [];
      setRows(data);
      if (data.length === 0) {
        setState("empty");
        setMessage("등록된 공통 환경설정이 없습니다.");
      } else {
        setState("");
        setMessage("");
      }
    } else {
      const msg =
        response.error?.message || "공통 환경설정 조회 중 오류가 발생했습니다.";
      setState(
        msg.includes("권한") || msg.includes("인증") ? "permission" : "error",
      );
      setMessage(msg);
      setFields(response.error?.fields || {});
    }
  }

  useEffect(() => {
    load();
  }, []);

  function choose(row: SystemConfig) {
    setSelected(row);
    setSettingValue(row.settingValue);
    setFields({});
    setState("");
    setMessage("");
  }

  function cancel() {
    if (selected) {
      setSettingValue(selected.settingValue);
    }
    setFields({});
    setState("");
    setMessage("미저장 입력값을 취소했습니다.");
  }

  async function saveValue(e?: FormEvent) {
    e?.preventDefault();
    if (!selected) {
      setState("error");
      setMessage("목록에서 환경설정 항목을 먼저 선택하세요.");
      return;
    }
    const response = await updateSystemConfig(
      selected.settingKey,
      settingValue,
    );
    if (response.success && response.data) {
      setState("success");
      setMessage("환경설정 값을 저장했습니다. 최신 목록을 재조회했습니다.");
      setFields({});
      await load();
      setSelected(response.data);
      setSettingValue(response.data.settingValue);
      setState("success");
      setMessage("환경설정 값을 저장했습니다. 최신 목록을 재조회했습니다.");
    } else {
      const msg = response.error?.message || "저장 중 오류가 발생했습니다.";
      setState(
        msg.includes("권한") || msg.includes("403") ? "permission" : "error",
      );
      setMessage(msg);
      setFields(response.error?.fields || {});
    }
  }

  async function restoreDefault() {
    if (!selected) return;
    setSettingValue(selected.defaultValue);
    const response = await updateSystemConfig(
      selected.settingKey,
      selected.defaultValue,
    );
    if (response.success && response.data) {
      setState("success");
      setMessage("기본값으로 복원했습니다. 최신 목록을 재조회했습니다.");
      setFields({});
      await load();
      setSelected(response.data);
      setSettingValue(response.data.settingValue);
      setState("success");
      setMessage("기본값으로 복원했습니다. 최신 목록을 재조회했습니다.");
    } else {
      const msg =
        response.error?.message || "기본값 복원 중 오류가 발생했습니다.";
      setState(
        msg.includes("권한") || msg.includes("403") ? "permission" : "error",
      );
      setMessage(msg);
      setFields(response.error?.fields || {});
    }
  }

  return (
    <section
      className="page-stack"
      data-testid="system-config-page"
      aria-busy={loading}
    >
      <div className="page-heading">
        <div>
          <span className="eyebrow">SCR-CMN-SYSTEM-CONFIG</span>
          <h1>공통 환경설정</h1>
          <p>
            시스템 전역의 세션, 조회, 검색기간, 대량조회, 장시간작업 안내 기준을
            저장·조회합니다.
          </p>
        </div>
        <div className="checklist-card" aria-label="UI Contract checklist">
          <span>route: /system/config/common</span>
          <span>GET /api/system-config</span>
          <span>PUT /api/system-config/{"{settingKey}"}</span>
          <span>states: loading / empty / error / permission / success</span>
        </div>
      </div>

      {loading && (
        <StateBanner type="loading" message="환경설정 목록을 조회 중입니다." />
      )}
      {message && state && <StateBanner type={state} message={message} />}
      {!isR09 && (
        <StateBanner
          type="permission"
          message="R09 시스템관리자만 설정값을 변경할 수 있습니다."
        />
      )}

      <div className="card table-card" data-testid="system-config-list-panel">
        <div className="card-header-row">
          <div>
            <h2>환경설정 목록</h2>
            <p>{rows.length}건 조회됨</p>
          </div>
          <button
            className="button button-outline"
            type="button"
            onClick={load}
            data-testid="system-config-refresh-button"
          >
            새로고침
          </button>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>항목코드</th>
                <th>항목명</th>
                <th>현재 설정값</th>
                <th>단위</th>
                <th>기본값</th>
                <th>설명</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td className="empty-row" colSpan={7}>
                    등록된 공통 환경설정이 없습니다.
                  </td>
                </tr>
              ) : (
                rows.map((row) => (
                  <tr
                    key={row.settingKey}
                    data-testid={`system-config-row-${testIdKey(row.settingKey)}`}
                    className={
                      selected?.settingKey === row.settingKey ? "selected" : ""
                    }
                  >
                    <td>{row.settingKey}</td>
                    <td>{row.settingName}</td>
                    <td>{row.settingValue}</td>
                    <td>{row.unit}</td>
                    <td>{row.defaultValue}</td>
                    <td>{row.description}</td>
                    <td>
                      <button
                        className="button button-outline"
                        type="button"
                        onClick={() => choose(row)}
                        data-testid={`system-config-select-${testIdKey(row.settingKey)}-button`}
                      >
                        {row.settingKey} 선택
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <form
        className="card editor-card"
        onSubmit={saveValue}
        data-testid="system-config-edit-panel"
      >
        <div className="card-header-row">
          <div>
            <h2>선택 항목 편집</h2>
            <p>
              항목코드와 메타데이터는 읽기 전용이며 설정값만 저장 payload에
              포함됩니다.
            </p>
          </div>
          {selected && (
            <span className="badge">선택: {selected.settingKey}</span>
          )}
        </div>

        {!selected ? (
          <div className="empty-row">목록에서 환경설정 항목을 선택하세요.</div>
        ) : (
          <div className="form-grid">
            <label className="field">
              <span>항목코드 (readonly)</span>
              <input
                value={selected.settingKey}
                readOnly
                className="readonly"
                data-testid="system-config-key-input"
              />
            </label>
            <label className="field">
              <span>항목명 (readonly)</span>
              <input
                value={selected.settingName}
                readOnly
                className="readonly"
              />
            </label>
            <label className="field">
              <span>설정값</span>
              <input
                aria-label="설정값"
                value={settingValue}
                readOnly={!isR09}
                onChange={(event) => setSettingValue(event.target.value)}
                data-testid="system-config-value-input"
              />
              <FieldError name="settingValue" fields={fields} />
            </label>
            <label className="field">
              <span>단위 (readonly)</span>
              <input value={selected.unit} readOnly className="readonly" />
            </label>
            <label className="field">
              <span>기본값 (readonly)</span>
              <input
                value={selected.defaultValue}
                readOnly
                className="readonly"
              />
            </label>
            <label className="field">
              <span>설명 (readonly)</span>
              <textarea
                value={selected.description}
                readOnly
                className="readonly"
                rows={3}
              />
            </label>
            <div className="info-note">{formatRange(selected)}</div>
          </div>
        )}

        <div className="action-bar">
          <button
            className="button"
            type="submit"
            disabled={!selected || !isR09}
            data-testid="system-config-save-button"
          >
            저장
          </button>
          <button
            className="button button-outline"
            type="button"
            onClick={restoreDefault}
            disabled={!selected || !isR09}
            data-testid="system-config-restore-button"
          >
            기본값 복원
          </button>
          <button
            className="button button-outline"
            type="button"
            onClick={cancel}
            disabled={!selected}
            data-testid="system-config-cancel-button"
          >
            취소
          </button>
        </div>
      </form>
    </section>
  );
}
