/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        brand: {
          navy: "#101827",
          panel: "#172033",
          gold: "#b79356",
          line: "#e5e7eb",
        },
      },
    },
  },
  plugins: [],
};
