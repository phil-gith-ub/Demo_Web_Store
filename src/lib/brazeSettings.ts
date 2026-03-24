const KEYS = {
  apiKey: "braze_api_key",
  baseUrl: "braze_sdk_endpoint",
} as const;

export type BrazeSettings = {
  apiKey: string;
  baseUrl: string;
};

export function getBrazeSettings(): BrazeSettings {
  return {
    apiKey: localStorage.getItem(KEYS.apiKey) ?? "",
    baseUrl: localStorage.getItem(KEYS.baseUrl) ?? "",
  };
}

export function saveBrazeSettings(settings: BrazeSettings): void {
  localStorage.setItem(KEYS.apiKey, settings.apiKey);
  localStorage.setItem(KEYS.baseUrl, settings.baseUrl);
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
