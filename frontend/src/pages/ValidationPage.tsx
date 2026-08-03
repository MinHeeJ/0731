import { useState } from "react";
import { api } from "../api/client";
import { StateBanner } from "../components/State";

type Check = {
  command: string;
  expected: string;
  status: "pending" | "running" | "pass" | "fail";
  evidence: string;
};

type ContractRow = Record<string, string>;
type HealthData = {
  status: string;
  service: string;
  technologyStack?: ContractRow[];
  versionBom?: ContractRow[];
  requiredOutputs?: ContractRow[];
};

const initialChecks: Check[] = [
  {
    command: "curl /api/health",
    expected: "ApiResponse.success=true",
    status: "pending",
    evidence: "-",
  },
  {
    command: "mvn test",
    expected: "backend contract/unit tests pass",
    status: "pending",
    evidence: "docs/validation-commands.md",
  },
  {
    command: "npm test -- --run",
    expected: "frontend route/state tests pass",
    status: "pending",
    evidence: "docs/validation-commands.md",
  },
  {
    command: "docker compose up",
    expected: "backend/frontend/database services healthy",
    status: "pending",
    evidence: "docs/validation-commands.md",
  },
  {
    command: "admin login smoke",
    expected: "admin/admin creates R09 session",
    status: "pending",
    evidence: "docs/validation-commands.md",
  },
];

export function ValidationPage() {
  const [message, setMessage] = useState("실행된 검증이 없습니다");
  const [state, setState] = useState<"empty" | "success" | "error" | "loading">(
    "empty",
  );
  const [checks, setChecks] = useState<Check[]>(initialChecks);

  async function run() {
    setState("loading");
    setMessage("/api/health 검증 실행 중");
    setChecks((items) =>
      items.map((item, index) =>
        index === 0
          ? { ...item, status: "running", evidence: "GET /api/health" }
          : item,
      ),
    );
    const response = await api<HealthData>("/api/health");
    if (response.success) {
      const requiredOutputSummary =
        response.data?.requiredOutputs
          ?.map((row) => `${row.key}=${row.value}`)
          .join(", ") || "required_outputs not returned";
      setState("success");
      setMessage(`health 200 / DB 계약 조회 성공: ${requiredOutputSummary}`);
      setChecks((items) =>
        items.map((item, index) =>
          index === 0
            ? {
                ...item,
                status: "pass",
                evidence: `GET /api/health returned requiredOutputs from DB: ${requiredOutputSummary}`,
              }
            : item,
        ),
      );
    } else {
      setState("error");
      setMessage(response.error?.message || "검증 실패");
      setChecks((items) =>
        items.map((item, index) =>
          index === 0
            ? {
                ...item,
                status: "fail",
                evidence: response.error?.message || "HTTP/API error",
              }
            : item,
        ),
      );
    }
  }

  return (
    <section className="page-stack">
      <div className="page-heading">
        <div>
          <span className="eyebrow">BATCH_OPERATION</span>
          <h1>1차 산출물 검증</h1>
          <p>
            구현 품질 gate와 backend/frontend 통합 검증 handoff를 관찰합니다.
          </p>
        </div>
        <button className="button" onClick={run}>
          curl /api/health
        </button>
      </div>
      <StateBanner type={state} message={message} />
      <div className="card table-card">
        <div className="card-header-row">
          <div>
            <h2>검증 checklist</h2>
            <p>
              실제 로컬 명령은 docs/validation-commands.md를 기준으로
              실행합니다.
            </p>
          </div>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>command</th>
                <th>expected result</th>
                <th>actual status</th>
                <th>evidence path</th>
              </tr>
            </thead>
            <tbody>
              {checks.map((check) => (
                <tr key={check.command}>
                  <td>{check.command}</td>
                  <td>{check.expected}</td>
                  <td>
                    <span className="badge">{check.status}</span>
                  </td>
                  <td>{check.evidence}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </section>
  );
}
