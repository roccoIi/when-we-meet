/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#4A90E2',
        'primary-dark': '#357ABD',
        secondary: '#50C878',
        'secondary-dark': '#3D9960',
        accent: '#FFD700',
        'accent-dark': '#CCAA00',
        danger: '#E74C3C',
        'danger-dark': '#C0392B',
        sunday: '#E74C3C',
        saturday: '#4A90E2',
        today: '#E3F2FD',
        unavailable: '#E0E0E0',
        selected: '#BBDEFB',
      },
      maxWidth: {
        'app': '500px',
      },
    },
  },
  plugins: [],
}

