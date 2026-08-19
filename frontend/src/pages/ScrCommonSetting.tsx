import { FormEvent, useEffect, useMemo, useState } from "react";
import { FieldError, StateBanner } from "../components/State";
import {
  CommonSettingItem,
  getCommonSettings,
  updateCommonSettings,
} from "../services/commonSettingsApi";

const settingKeys = [
  "sessionIdleMinutes",
  "pageSize",
  "defaultSearchPeriodDays",
  "bulkQueryThresholdCount",
  "longRunningTaskNoticeSeconds",
] as const;

type SettingKey = (typeof settingKeys)[number];

type FormState = Record<SettingKey, string> & { changeReason: string };

const fallbackNames: Record<SettingKey, string> = {
  sessionIdleMinutes: "세션 유휴시간",
  pageSize: "페이지당 조회건수",
  defaultSearchPeriodDays: "기본 검색기간",
  bulkQueryThresholdCount: "대량조회 기준건수",
  longRunningTaskNoticeSeconds: "장시간작업 안내 기준",
};

const unitLabels: Record<string, string> = {
  MINUTE: "분",
  COUNT: "건",
  DAY: "일",
  SECOND: "초",
};

function emptyForm(): FormState {
  return {
    sessionIdleMinutes: "",
    pageSize: "",
    defaultSearchPeriodDays: "",
    bulkQueryThresholdCount: "",
    longRunningTaskNoticeSeconds: "",
    changeReason: "",
  };
}

function isPermission(message: string) {
  return (
    message.includes("권한") ||
    message.includes("401") ||
    message.includes("403") ||
    message.includes("인증")
  );
}

function toForm(items: CommonSettingItem[]): FormState {
  const next = emptyForm();
  items.forEach((item) => {
    if (settingKeys.includes(item.settingKey as SettingKey)) {
      next[item.settingKey as SettingKey] = item.settingValue ?? "";
    }
  });
  return next;
}

export function ScrCommonSetting() {
  const [items, setItems] = useState<CommonSettingItem[]>([]);
  const [form, setForm] = useState<FormState>(emptyForm());
  const [loading, setLoading] = useState(false);
  const [state, setState] = useState<
    "empty" | "error" | "success" | "permission" | ""
  >("");
  const [message, setMessage] = useState("");
  const [fields, setFields] = useState<Record<string, string>>({});

  const itemByKey = useMemo(
    () => new Map(items.map((item) => [item.settingKey, item])),
    [items],
  );

  async function load(nextChangeReason = form.changeReason) {
    setLoading(true);
    setMessage("");
    setState("");
    const response = await getCommonSettings();
    setLoading(false);
    if (response.success) {
      const nextItems = response.data?.items || [];
      setItems(nextItems);
      setForm({ ...toForm(nextItems), changeReason: nextChangeReason });
      setFields({});
      if (nextItems.length === 0) {
        setState("empty");
        setMessage("등록된 공통 환경설정이 없습니다.");
      }
    } else {
      const msg = response.error?.message || "공통 환경설정 조회 오류";
      setState(isPermission(msg) ? "permission" : "error");
      setMessage(msg);
      setFields(response.error?.fields || {});
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function save(event: FormEvent) {
    event.preventDefault();
    setFields({});
    const body = {
      sessionIdleMinutes: Number(form.sessionIdleMinutes),
      pageSize: Number(form.pageSize),
      defaultSearchPeriodDays: Number(form.defaultSearchPeriodDays),
      bulkQueryThresholdCount: Number(form.bulkQueryThresholdCount),
      longRunningTaskNoticeSeconds: Number(form.longRunningTaskNoticeSeconds),
      changeReason: form.changeReason,
    };
    const response = await updateCommonSettings(body);
    if (response.success) {
      setItems(response.data?.items || []);
      setForm({ ...toForm(response.data?.items || []), changeReason: "" });
      setState("success");
      setMessage("저장되었습니다. 최신 공통 환경설정 값을 재조회했습니다.");
      await load("");
      setState("success");
      setMessage("저장되었습니다. 최신 공통 환경설정 값을 재조회했습니다.");
    } else {
      const msg = response.error?.message || "공통 환경설정 저장 오류";
      setState(isPermission(msg) ? "permission" : "error");
      setMessage(msg);
      setFields(response.error?.fields || {});
    }
  }

  const checklist = [
    "route: /system/common-settings",
    "menu_path: 시스템 관리 > 시스템 환경설정 > 공통 환경설정",
    "CTA: 새로고침 / 설정 저장",
    "states: loading / empty / error / permission / success",
    "scope: GLOBAL only, userId/businessCategory 입력 없음",
  ];

  return (
    <section
      className="page-stack"
      aria-busy={loading}
      data-testid="common-settings-page"
    >
      <div className="page-heading">
        <div>
          <span className="eyebrow">SEARCH_LIST_DETAIL</span>
          <h1>공통 환경설정</h1>
          <p>
            세션, 검색 기간, 조회 건수, 장시간작업 안내 기준을 전역 설정값으로
            관리합니다.
          </p>
        </div>
        <div className="checklist-card" aria-label="UI Contract checklist">
          {checklist.map((item) => (
            <span key={item}>{item}</span>
          ))}
        </div>
      </div>

      {loading && (
        <StateBanner type="loading" message="공통 환경설정을 조회 중입니다." />
      )}
      {message && state && <StateBanner type={state} message={message} />}
      <div className="info-note">
        특정 사용자나 업무별 설정은 제공하지 않습니다. 다섯 항목은 GLOBAL
        scope와 항목별 단위로만 저장됩니다.
      </div>

      <div className="card table-card">
        <div className="card-header-row">
          <div>
            <h2>현재 설정값</h2>
            <p>{items.length}건 조회됨</p>
          </div>
          <button
            className="button button-outline"
            type="button"
            onClick={() => load()}
            data-testid="common-settings-refresh-button"
          >
            새로고침
          </button>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>항목</th>
                <th>현재 값</th>
                <th>단위</th>
                <th>값 유형</th>
                <th>Scope</th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr>
                  <td className="empty-row" colSpan={5}>
                    등록된 공통 환경설정이 없습니다.
                  </td>
                </tr>
              ) : (
                items.map((item) => (
                  <tr
                    key={item.settingKey}
                    data-testid={`common-setting-row-${item.settingKey}`}
                  >
                    <td>{item.settingName}</td>
                    <td>{item.settingValue}</td>
                    <td>{unitLabels[item.unitCode] || item.unitCode}</td>
                    <td>{item.valueType}</td>
                    <td>{item.scopeType}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <form className="card editor-card" onSubmit={save}>
        <div className="card-header-row">
          <div>
            <h2>설정 저장</h2>
            <p>
              정수 값과 변경사유만 전송합니다. 사용자별·업무별 scope 필드는
              없습니다.
            </p>
          </div>
          <span className="badge">operationId: updateCommonSettings</span>
        </div>
        <div className="form-grid">
          {settingKeys.map((key) => {
            const item = itemByKey.get(key);
            return (
              <label className="field" key={key}>
                <span>
                  {item?.settingName || fallbackNames[key]} (
                  {unitLabels[item?.unitCode || ""] || item?.unitCode || "단위"}
                  )
                </span>
                <input
                  type="number"
                  inputMode="numeric"
                  value={form[key]}
                  onChange={(event) =>
                    setForm({ ...form, [key]: event.target.value })
                  }
                  data-testid={`common-setting-input-${key}`}
                />
                <FieldError name={key} fields={fields} />
              </label>
            );
          })}
          <label className="field">
            <span>변경사유</span>
            <textarea
              rows={3}
              maxLength={500}
              value={form.changeReason}
              onChange={(event) =>
                setForm({ ...form, changeReason: event.target.value })
              }
              data-testid="common-settings-change-reason"
            />
            <FieldError name="changeReason" fields={fields} />
          </label>
        </div>
        <div className="action-bar">
          <button type="submit" data-testid="common-settings-save-button">
            설정 저장
          </button>
          <button
            className="button button-outline"
            type="button"
            onClick={() => setForm(toForm(items))}
            data-testid="common-settings-reset-button"
          >
            취소
          </button>
        </div>
      </form>
    </section>
  );
}
