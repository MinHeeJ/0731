import { describe, expect, it } from "vitest";
import { qs } from "./client";

describe("qs", () => {
  it("sends visible search filters after trimming and omits blank values", () => {
    expect(
      qs({
        roleCode: " R09 ",
        roleName: " 시스템 ",
        status: "   ",
        menuId: "MENU",
      }),
    ).toBe("?roleCode=R09&roleName=%EC%8B%9C%EC%8A%A4%ED%85%9C&menuId=MENU");
  });
});
