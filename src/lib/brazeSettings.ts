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

/** Normalize host: strip protocol and trailing slash for Braze Web SDK `baseUrl`. */
export function normalizeBrazeBaseUrl(input: string): string {
  let s = input.trim();
  if (!s) return "";
  s = s.replace(/^https?:\/\//i, "");
  s = s.replace(/\/$/, "");
  return s;
}
