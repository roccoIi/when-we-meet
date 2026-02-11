/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#A8E6CF',
        'primary-dark': '#88c9b3',
        secondary: '#FFD3B6',
        tertiary: '#FDFFAB',
        'accent-pink': '#FF8B94',
        'background-light': '#FFFDF9',
        'background-dark': '#2D2D2D',
        'neutral-dark': '#4A4A4A',
        'neutral-light': '#F7F5F0',
        'pastel-border': '#E8DFF5',
        'pastel-green': '#D4F0F0',
        'pastel-blue': '#A0E7E5',
        'pastel-lilac': '#E0BBE4',
        // Login 페이지 색상
        'pastel-mint': '#B4F8C8',
        'pastel-pink': '#FFD6E0',
        'pastel-yellow': '#FBE7C6',
        'kakao-pastel': '#FEE500',
        'background-soft': '#FFF9F5',
        // MeetingList 추가 색상
        'cream-bg': '#FDFBF7',
        'cream-card': '#FFFFFF',
        'mint-primary': '#A8E6CF',
        'mint-dark': '#84DCC6',
        'mint-light': '#DCEDC8',
        'peach-accent': '#FFDAC1',
        'soft-pink': '#FFB7B2',
        'soft-purple': '#E0BBE4',
        'text-main': '#5D5D5D',
        'text-sub': '#9A9A9A',
        'surface-soft': '#F7F5F0',
        'status-green': '#C4F4E4',
        'status-yellow': '#FEF3C7',
        'status-red': '#FFD6D6',
        // 기존 색상 유지
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
      fontFamily: {
        'display': ['"Plus Jakarta Sans"', '"Quicksand"', 'sans-serif'],
      },
      borderRadius: {
        'DEFAULT': '0.5rem',
        'lg': '1rem',
        'xl': '1.5rem',
        '2xl': '1.5rem',
        '3xl': '2rem',
        'full': '9999px',
      },
      boxShadow: {
        'glow': '0 4px 20px -5px rgba(168, 230, 207, 0.6)',
        'soft': '0 4px 20px -5px rgba(180, 180, 170, 0.15)',
        'soft-hover': '0 10px 25px -5px rgba(180, 180, 170, 0.25)',
        'inner-soft': 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.03)',
        'btn': '0 2px 10px rgba(0,0,0,0.05)',
      },
    },
  },
  plugins: [],
}

