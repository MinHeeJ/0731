import { describe, expect, it } from "vitest";
import { readFileSync, readdirSync, statSync } from "node:fs";
import { join } from "node:path";

function files(dir: string): string[] {
  return readdirSync(dir).flatMap((fileName) => {
    const path = join(dir, fileName);
    return statSync(path).isDirectory() ? files(path) : [path];
  });
}

describe("frontend API base contract", () => {
  it("uses relative api paths only in production source", () => {
    const text = files("src")
      .filter((filePath) => /\.(ts|tsx)$/.test(filePath))
      .filter(
        (filePath) =>
          !filePath.endsWith(".test.ts") && !filePath.endsWith(".test.tsx"),
      )
      .filter((filePath) => !filePath.includes("src/test/"))
      .map((filePath) => readFileSync(filePath, "utf8"))
      .join("\n");
    expect(text).not.toContain("localhost");
    expect(text).not.toContain("http://backend");
    expect(text).toContain("/api/");
  });
});
