const KEYS = {
  apiKey: "braze_api_key",
  baseUrl: "braze_sdk_endpoint",
  restApiKey: "braze_rest_api_key",
  restEndpoint: "braze_rest_endpoint",
} as const;

export type BrazeSettings = {
  apiKey: string;
  baseUrl: string;
  /** Braze REST API key (Manage Settings → API Keys) — for `/users/export/ids` profile import. */
  restApiKey: string;
  /** REST instance URL, e.g. `https://todd.braze.com` (demo) or your dashboard REST endpoint. */
  restEndpoint: string;
};

export function getBrazeSettings(): BrazeSettings {
  return {
    apiKey: localStorage.getItem(KEYS.apiKey) ?? "",
    baseUrl: localStorage.getItem(KEYS.baseUrl) ?? "",
    restApiKey: localStorage.getItem(KEYS.restApiKey) ?? "",
    restEndpoint: localStorage.getItem(KEYS.restEndpoint) ?? "",
  };
}

export function saveBrazeSettings(settings: BrazeSettings): void {
  localStorage.setItem(KEYS.apiKey, settings.apiKey);
  localStorage.setItem(KEYS.baseUrl, settings.baseUrl);
  localStorage.setItem(KEYS.restApiKey, settings.restApiKey);
  localStorage.setItem(KEYS.restEndpoint, settings.restEndpoint);
}

/**
 * Braze Web SDK `baseUrl` must be a hostname only (no protocol, no path).
 * Accepts pasted URLs and strips to host.
 */
export function normalizeBrazeBaseUrl(input: string): string {
  const raw = input.trim();
  if (!raw) return "";

  try {
    const withProto = /^https?:\/\//i.test(raw) ? raw : `https://${raw}`;
    const u = new URL(withProto);
    return u.hostname || "";
  } catch {
    const s = raw
      .replace(/^https?:\/\//i, "")
      .replace(/\/.*$/, "")
      .trim();
    return s;
  }
}

/** Normalize REST base URL with no trailing slash. */
export function normalizeBrazeRestEndpoint(input: string): string {
  const raw = input.trim();
  if (!raw) return "";
  try {
    const withProto = /^https?:\/\//i.test(raw) ? raw : `https://${raw}`;
    const u = new URL(withProto);
    if (!u.hostname) return "";
    return `${u.protocol}//${u.host}`;
  } catch {
    return "";
  }
}
