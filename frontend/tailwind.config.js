/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        knue: {
          navy: "#12305a",
          blue: "#2563eb",
          mint: "#10b981",
          line: "#dbe4ef",
        },
      },
    },
  },
  plugins: [],
};
