import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

// Set VITE_BASE_PATH=/repo-name/ when deploying to GitHub Project Pages
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const brazeRestTarget =
    env.BRAZE_REST_PROXY_TARGET || "https://todd.braze.com";

  return {
    plugins: [react()],
    base: process.env.VITE_BASE_PATH ?? "/",
    resolve: {
      dedupe: ["react", "react-dom"],
    },
    optimizeDeps: {
      include: ["react", "react-dom", "react-router-dom"],
      /** Avoid pre-bundling Braze with Vite (reduces odd chunk + re-init issues). */
      exclude: ["@braze/web-sdk"],
    },
    server: {
      proxy: {
        "/braze-rest": {
          target: brazeRestTarget,
          changeOrigin: true,
          secure: true,
          rewrite: (path) => path.replace(/^\/braze-rest/, ""),
        },
      },
    },
  };
});
