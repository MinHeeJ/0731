import { useEffect, useMemo, useState } from "react";
import {
  CommonSetting,
  CommonSettingKey,
  getCommonSettings,
  updateCommonSettings,
} from "../api/commonSettingsApi";
import { FieldError, StateBanner } from "../components/State";

const settingOrder: CommonSettingKey[] = [
  "sessionIdleTime",
  "pageSize",
  "defaultSearchPeriod",
  "bulkQueryThreshold",
  "longRunningTaskNoticeThreshold",
];

const labels: Record<CommonSettingKey, string> = {
  sessionIdleTime: "세션 유휴시간",
  pageSize: "페이지당 조회건수",
  defaultSearchPeriod: "기본 검색기간",
  bulkQueryThreshold: "대량조회 기준건수",
  longRunningTaskNoticeThreshold: "장시간작업 안내 기준",
};

const guardCopy: Record<CommonSettingKey, string> = {
  sessionIdleTime: "세션 인증방식 선택 control 없음",
  pageSize: "사용자별·업무별 page size override control 없음",
  defaultSearchPeriod: "기간 단위와 범위는 설정 metadata를 표시합니다",
  bulkQueryThreshold: "대량처리 구현방식 선택 control 없음",
  longRunningTaskNoticeThreshold: "worker/queue 설정 control 없음",
};

function isPermission(message: string) {
  return (
    message.includes("401") ||
    message.includes("403") ||
    message.includes("권한") ||
    message.includes("인증")
  );
}

export function CommonSettingsPage() {
  const [items, setItems] = useState<CommonSetting[]>([]);
  const [form, setForm] = useState<Record<CommonSettingKey, string>>({
    sessionIdleTime: "",
    pageSize: "",
    defaultSearchPeriod: "",
    bulkQueryThreshold: "",
    longRunningTaskNoticeThreshold: "",
  });
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");
  const [state, setState] = useState<
    "empty" | "error" | "success" | "permission" | ""
  >("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [confirmOpen, setConfirmOpen] = useState(false);

  const byKey = useMemo(
    () =>
      Object.fromEntries(
        items.map((item) => [item.settingKey, item]),
      ) as Partial<Record<CommonSettingKey, CommonSetting>>,
    [items],
  );

  async function load(showLoading = true) {
    if (showLoading) setLoading(true);
    setMessage("");
    setFieldErrors({});
    const response = await getCommonSettings();
    if (showLoading) setLoading(false);
    if (response.success && response.data) {
      const ordered = settingOrder
        .map((key) =>
          response.data?.items.find((item) => item.settingKey === key),
        )
        .filter(Boolean) as CommonSetting[];
      setItems(ordered);
      setForm({
        sessionIdleTime: byValue(ordered, "sessionIdleTime"),
        pageSize: byValue(ordered, "pageSize"),
        defaultSearchPeriod: byValue(ordered, "defaultSearchPeriod"),
        bulkQueryThreshold: byValue(ordered, "bulkQueryThreshold"),
        longRunningTaskNoticeThreshold: byValue(
          ordered,
          "longRunningTaskNoticeThreshold",
        ),
      });
      if ((response.data.totalElements || ordered.length) === 0) {
        setState("empty");
        setMessage("공통 환경설정 항목이 없습니다");
      } else {
        setState("");
      }
    } else {
      const msg = response.error?.message || "공통 환경설정 조회 오류";
      setState(isPermission(msg) ? "permission" : "error");
      setMessage(
        isPermission(msg) ? "공통 환경설정 접근 권한이 없습니다" : msg,
      );
    }
  }

  useEffect(() => {
    load();
  }, []);

  function cancelChanges() {
    setForm({
      sessionIdleTime: byValue(items, "sessionIdleTime"),
      pageSize: byValue(items, "pageSize"),
      defaultSearchPeriod: byValue(items, "defaultSearchPeriod"),
      bulkQueryThreshold: byValue(items, "bulkQueryThreshold"),
      longRunningTaskNoticeThreshold: byValue(
        items,
        "longRunningTaskNoticeThreshold",
      ),
    });
    setFieldErrors({});
    setState("success");
    setMessage("변경 전 값으로 복원됨");
  }

  async function saveConfirmed() {
    setConfirmOpen(false);
    setLoading(true);
    setFieldErrors({});
    const response = await updateCommonSettings(form);
    setLoading(false);
    if (response.success) {
      setState("success");
      setMessage("공통 환경설정이 저장되었습니다");
      await load(false);
      setState("success");
      setMessage("공통 환경설정이 저장되었습니다");
    } else {
      const msg = response.error?.message || "공통 환경설정 저장 오류";
      setState(isPermission(msg) ? "permission" : "error");
      setMessage(
        isPermission(msg)
          ? "공통 환경설정 접근 권한이 없습니다"
          : `${msg} 기존 값은 유지됩니다.`,
      );
      setFieldErrors(
        response.error?.fieldErrors || response.error?.fields || {},
      );
    }
  }

  return (
    <section
      className="page-stack"
      data-testid="common-settings-page"
      aria-busy={loading}
    >
      <div className="page-heading">
        <div>
          <span className="eyebrow">
            시스템 관리 &gt; 시스템 환경설정 &gt; 공통 환경설정
          </span>
          <h1>공통 환경설정</h1>
          <p>
            세션 유휴시간, 페이지당 조회건수, 기본 검색기간, 대량조회 기준건수,
            장시간작업 안내 기준을 공통 설정값으로 관리합니다.
          </p>
        </div>
        <div className="checklist-card" aria-label="UI Contract checklist">
          <span>route: /system/common-settings</span>
          <span>operation: getCommonSettings / updateCommonSettings</span>
          <span>states: loading / empty / error / permission / success</span>
        </div>
      </div>

      {loading && (
        <StateBanner
          type="loading"
          message="공통 환경설정 항목과 현재 값을 불러오는 중"
        />
      )}
      {message && state && <StateBanner type={state} message={message} />}
      {state === "error" && (
        <button
          className="button button-outline"
          type="button"
          onClick={() => load()}
          data-testid="common-settings-retry-button"
        >
          다시 조회
        </button>
      )}

      <div className="card editor-card">
        <div className="card-header-row">
          <div>
            <h2>설정 항목</h2>
            <p>
              설정 단위와 의미 metadata는 읽기 전용이며 저장 payload에는 현재
              값만 전송합니다.
            </p>
          </div>
          <span className="badge">{items.length}건 조회됨</span>
        </div>
        {items.length === 0 && !loading ? (
          <div className="empty-row">공통 환경설정 항목이 없습니다</div>
        ) : (
          <div
            className="settings-list"
            data-testid="common-settings-form-panel"
          >
            {settingOrder.map((key) => {
              const item = byKey[key];
              return (
                <div
                  className="setting-row"
                  key={key}
                  data-testid={`common-setting-${key}-row`}
                >
                  <div>
                    <strong>{labels[key]}</strong>
                    <p>{item?.settingMeaning || labels[key]}</p>
                    <small>
                      단위: {item?.settingUnit || "OQ-UI-002"} ·{" "}
                      {guardCopy[key]}
                    </small>
                  </div>
                  <div className="current-value">
                    <span>현재 값</span>
                    <strong data-testid={`common-setting-${key}-current`}>
                      {item?.settingValue || "-"}
                    </strong>
                  </div>
                  <label className="field">
                    <span>변경 값</span>
                    <input
                      data-testid={`common-setting-${key}-input`}
                      value={form[key]}
                      onChange={(event) =>
                        setForm({ ...form, [key]: event.target.value })
                      }
                      aria-label={`${labels[key]} 변경 값`}
                    />
                    <FieldError name={key} fields={fieldErrors} />
                  </label>
                </div>
              );
            })}
          </div>
        )}
        <div className="info-note">
          사용자별·업무별 override 입력, 세션 인증방식 선택, 대량처리 구현방식
          선택 control은 제공하지 않습니다.
        </div>
        <div className="action-bar">
          <button
            className="button"
            type="button"
            onClick={() => setConfirmOpen(true)}
            disabled={items.length === 0}
            data-testid="common-settings-save-button"
          >
            저장
          </button>
          <button
            className="button button-outline"
            type="button"
            onClick={cancelChanges}
            data-testid="common-settings-cancel-button"
          >
            취소
          </button>
        </div>
      </div>

      {confirmOpen && (
        <div
          className="modal-backdrop"
          role="dialog"
          aria-modal="true"
          data-testid="common-settings-save-modal"
        >
          <div className="modal card">
            <h2>저장 확인</h2>
            <p>변경한 공통 환경설정 값을 저장하시겠습니까?</p>
            <p>저장 후 현재 값은 재조회 결과로 확인합니다.</p>
            <div className="action-bar">
              <button
                className="button"
                type="button"
                onClick={saveConfirmed}
                data-testid="common-settings-confirm-button"
              >
                확인
              </button>
              <button
                className="button button-outline"
                type="button"
                onClick={() => setConfirmOpen(false)}
                data-testid="common-settings-modal-cancel-button"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

function byValue(items: CommonSetting[], key: CommonSettingKey) {
  return items.find((item) => item.settingKey === key)?.settingValue || "";
}
