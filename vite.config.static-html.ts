/**
 * Production bundle for opening without `vite` / `npm run dev`.
 *
 *   npm run build:static-html
 *
 * Output: ./static-html-site/ — open index.html (file://) or serve the folder with any static host.
 * Uses hash routing (#/store) so paths work without a server. Braze REST calls use your saved REST URL;
 * there is no /braze-rest proxy, so the REST host must allow browser CORS or you only use the Web SDK path.
 */
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig(({ mode }) => {
  loadEnv(mode, process.cwd(), "");
  return {
    plugins: [react()],
    base: "./",
    build: {
      outDir: "static-html-site",
      emptyOutDir: true,
    },
    define: {
      "import.meta.env.VITE_HASH_ROUTER": JSON.stringify("true"),
    },
    resolve: {
      dedupe: ["react", "react-dom"],
    },
    optimizeDeps: {
      include: ["react", "react-dom", "react-router-dom"],
      exclude: ["@braze/web-sdk"],
    },
  };
});
