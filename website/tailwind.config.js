/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        cyber: {
          mint: "#00C882",
          mintLight: "#38EFAB",
          mintDark: "#008E5B",
          cyan: "#00B4D8",
          purple: "#9D4EDD",
          amber: "#FFB703",
          laserRed: "#FF5252",
          bgDark: "#071712",
          surfaceDark: "#0C241B",
          surfaceCard: "#113326",
          border: "#205540"
        }
      },
      fontFamily: {
        mono: ['"JetBrains Mono"', 'monospace', 'ui-monospace'],
        sans: ['Inter', 'sans-serif', 'system-ui']
      }
    },
  },
  plugins: [],
}
