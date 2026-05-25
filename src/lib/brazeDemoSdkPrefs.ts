const ALLOW_USER_JS_KEY = "braze_demo_allow_user_supplied_javascript";

/** Default matches previous hard-coded app behavior. */
export function getAllowUserSuppliedJavascriptForInit(): boolean {
  try {
    const v = localStorage.getItem(ALLOW_USER_JS_KEY);
    if (v === null) return true;
    return v === "1" || v === "true";
  } catch {
    return true;
  }
}

export function setAllowUserSuppliedJavascriptForInit(enabled: boolean): void {
  try {
    localStorage.setItem(ALLOW_USER_JS_KEY, enabled ? "1" : "0");
  } catch {
    /* ignore */
  }
}
