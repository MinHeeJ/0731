import { defineConfig, type PreviewOptions } from "vite";
import react from "@vitejs/plugin-react";

const previewOptions: PreviewOptions & { allowedHosts: true } = {
  host: "0.0.0.0",
  allowedHosts: true,
};

export default defineConfig({
  plugins: [react()],
  server: {
    host: "0.0.0.0",
    port: 3000,
  },
  preview: previewOptions,
});
