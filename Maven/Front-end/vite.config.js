import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite configuration for React frontend
// - Uses @vitejs/plugin-react for React support
// - Proxies /api requests to Spring Boot backend (http://localhost:8080)
export default defineConfig({
  plugins: [react()], // React plugin for Vite
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Spring Boot backend
        changeOrigin: true, // Changes the origin of the host header to the target URL
      },
    },
  },
})
