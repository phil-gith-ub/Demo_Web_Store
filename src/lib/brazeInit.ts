import { getBrazeSettings, normalizeBrazeBaseUrl } from "./brazeSettings";

let lastIdentifiedUserId: string | null = null;

export type BrazeIdentifiedInitResult =
  | { success: true }
  | { success: false; message: string };

function errMsg(e: unknown): string {
  if (e instanceof Error) return e.message;
  return String(e);
}

/**
 * Start the Braze Web SDK for an identified user only.
 * Order: `initialize` (if needed) → `changeUser` → IAM → `openSession`.
 */
export async function initBrazeForIdentifiedUser(
  userId: string,
): Promise<BrazeIdentifiedInitResult> {
  const id = userId.trim();
  if (!id) {
    return { success: false, message: "User ID is empty." };
  }

  const { apiKey, baseUrl } = getBrazeSettings();
  const key = apiKey.trim();
  const host = normalizeBrazeBaseUrl(baseUrl);
  if (!key) {
    return {
      success: false,
      message:
        "No Web API key in Settings. Use the Web channel SDK key from Braze (not the Android or REST-only key unless your workspace uses one key for both).",
    };
  }
  if (!host) {
    return {
      success: false,
      message:
        "No SDK endpoint hostname in Settings. Paste only the host (e.g. sdk.fra-02.braze.eu or sondheim.braze.com), no https:// or paths.",
    };
  }

  try {
    const braze = await import("@braze/web-sdk");

    if (!braze.isInitialized?.()) {
      braze.initialize(key, {
        baseUrl: host,
        enableLogging: true,
      });
    }

    if (!braze.isInitialized?.()) {
      return {
        success: false,
        message:
          "initialize() ran but SDK reports not initialized — check API key and endpoint match your Braze workspace (Web).",
      };
    }

    if (lastIdentifiedUserId === id && braze.isInitialized?.()) {
      return { success: true };
    }

    braze.changeUser(id);
    braze.automaticallyShowInAppMessages();
    braze.openSession();
    lastIdentifiedUserId = id;
    return { success: true };
  } catch (e) {
    const detail = errMsg(e);
    console.warn("Braze init for identified user failed:", e);
    return {
      success: false,
      message: `SDK threw: ${detail}`,
    };
  }
}

/**
 * Tear down Braze on logout so the next login can call `initialize` cleanly.
 * Prefer `destroy()` over `wipeData()` here: after `wipeData()`, a subsequent
 * init with Vite’s lazy chunks can throw "Class extends value undefined".
 */
export async function brazeOnLogout(): Promise<void> {
  try {
    const braze = await import("@braze/web-sdk");
    if (braze.isInitialized?.()) {
      braze.destroy();
    }
  } catch {
    /* SDK never loaded */
  } finally {
    lastIdentifiedUserId = null;
  }
}

export async function isBrazeReady(): Promise<boolean> {
  try {
    const braze = await import("@braze/web-sdk");
    return braze.isInitialized?.() ?? false;
  } catch {
    return false;
  }
}
