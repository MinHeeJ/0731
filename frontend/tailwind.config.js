/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        knue: { navy: "#17345c", blue: "#2563eb", mist: "#eef4ff" },
      },
    },
  },
  plugins: [],
};
