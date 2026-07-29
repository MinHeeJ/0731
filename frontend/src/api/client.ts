export type FieldError = { field: string; message: string };
export type ApiResponse<T> = {
  success: boolean;
  data: T;
  error?: {
    code: string;
    message: string;
    fieldErrors?: FieldError[];
  };
};

export class ApiError extends Error {
  status: number;
  code?: string;
  fieldErrors: FieldError[];

  constructor(
    message: string,
    status: number,
    code?: string,
    fieldErrors: FieldError[] = [],
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.fieldErrors = fieldErrors;
  }
}

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    ...init,
    headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
    credentials: "include",
  });
  const body = (await res.json()) as ApiResponse<T>;
  if (!res.ok || !body.success) {
    throw new ApiError(
      body.error?.message || "요청 처리에 실패했습니다.",
      res.status,
      body.error?.code,
      body.error?.fieldErrors || [],
    );
  }
  return body.data;
}

export const qs = (params: Record<string, string | number | undefined>) =>
  new URLSearchParams(
    Object.entries(params)
      .filter(([, v]) => v !== undefined && String(v) !== "")
      .map(([k, v]) => [k, String(v)]),
  ).toString();
