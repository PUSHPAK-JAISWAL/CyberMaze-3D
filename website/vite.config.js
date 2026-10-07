import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  base: './', // relative path allows deploying to root or subpaths like GitHub Pages
  build: {
    outDir: 'dist',
  }
});
