import { afterEach, describe, expect, it, vi } from "vitest";
import { listDetailCodes, listMenuTree } from "./adminApi";

describe("admin search API clients", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("forwards menu tree and detail code search filters as relative query strings", async () => {
    const calls: string[] = [];
    global.fetch = vi.fn(async (input: RequestInfo | URL) => {
      calls.push(String(input));
      return {
        ok: true,
        json: async () => ({ success: true, data: [] }),
      } as Response;
    }) as never;

    await listMenuTree({ menuName: " 관리 ", menuId: "MENU" });
    await listDetailCodes("EVAL", {
      groupId: "EVAL",
      codeValue: "TEACHING",
      status: "ACTIVE",
    });

    expect(calls).toEqual([
      "/api/menus/tree?menuName=%EA%B4%80%EB%A6%AC&menuId=MENU",
      "/api/code-groups/EVAL/detail-codes?groupId=EVAL&codeValue=TEACHING&status=ACTIVE",
    ]);
  });
});
