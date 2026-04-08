/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_BASE_PATH?: string;
  /** Set at build time by vite.config.static-html.ts for file:// / static folder use. */
  readonly VITE_HASH_ROUTER?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

/** DevTools: same `@braze/web-sdk` namespace the app uses (script-tag demos often set `window.braze`). */
declare global {
  interface Window {
    braze?: typeof import("@braze/web-sdk");
  }
}

export {};
