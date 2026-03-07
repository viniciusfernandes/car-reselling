import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");

  const port = Number(env.VITE_DEV_PORT ?? 5173);
  const apiProxyTarget = env.VITE_API_PROXY_TARGET ?? "http://localhost:8080";
  const authProxyTarget = env.VITE_AUTH_PROXY_TARGET ?? "http://localhost:8081";

  return {
    plugins: [react()],
    server: {
      port,
      proxy: {
        "/api": {
          target: apiProxyTarget,
          changeOrigin: true,
        },
        "/auth": {
          target: authProxyTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/auth/, ""),
        },
      },
    },
  };
});
