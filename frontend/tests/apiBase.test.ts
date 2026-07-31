import { describe, expect, it } from "vitest";
import { apiRequest } from "../src/shared/api/client";
describe("relative API client", () => {
  it("rejects non relative api paths", async () => {
    await expect(apiRequest("http://localhost:8080/api/users")).rejects.toThrow(
      "/api/",
    );
  });
});
