import { getBrazeSettings, normalizeBrazeBaseUrl } from "./brazeSettings";

/**
 * Tracks which user we last ran `changeUser` for this tab (avoids duplicate
 * openSession noise when React re-runs effects).
 */
let lastIdentifiedUserId: string | null = null;

/**
 * Start the Braze Web SDK for an identified user only.
 * Per Braze Web SDK flow: `initialize` → `changeUser` → IAM → `openSession`
 * (see Braze docs / Context7 samples).
 */
export async function initBrazeForIdentifiedUser(userId: string): Promise<boolean> {
  const id = userId.trim();
  if (!id) return false;

  const { apiKey, baseUrl } = getBrazeSettings();
  const key = apiKey.trim();
  const host = normalizeBrazeBaseUrl(baseUrl);
  if (!key || !host) {
    return false;
  }

  try {
    const braze = await import("@braze/web-sdk");

    if (!braze.isInitialized?.()) {
      braze.initialize(key, {
        baseUrl: host,
        enableLogging: true,
      });
    }

    if (lastIdentifiedUserId === id && braze.isInitialized?.()) {
      return true;
    }

    braze.changeUser(id);
    braze.automaticallyShowInAppMessages();
    braze.openSession();
    lastIdentifiedUserId = id;
    return true;
  } catch (e) {
    console.warn("Braze init for identified user failed:", e);
    return false;
  }
}

/**
 * Clear Braze data on logout so anonymous browsing does not keep the prior
 * user’s SDK state. Uses `wipeData` from the Web SDK (see Braze advanced APIs).
 */
export async function brazeOnLogout(): Promise<void> {
  try {
    const braze = await import("@braze/web-sdk");
    if (braze.isInitialized?.()) {
      braze.wipeData();
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
