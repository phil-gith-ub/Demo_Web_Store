import { ALL_BANNER_PLACEMENT_IDS } from "./brazeConstants";
import { getBrazeSettings, normalizeBrazeBaseUrl } from "./brazeSettings";
import { completeIdentifiedUserAfterChangeUser, brazePreLogout } from "./brazeUserSyncWeb";

let lastIdentifiedUserId: string | null = null;
let brazeSubscriptionsRegistered = false;

export type BrazeIdentifiedInitResult =
  | { success: true }
  | { success: false; message: string };

function errMsg(e: unknown): string {
  if (e instanceof Error) return e.message;
  return String(e);
}

function registerBrazeSubscriptionsOnce(braze: typeof import("@braze/web-sdk")) {
  if (brazeSubscriptionsRegistered) return;
  braze.subscribeToBannersUpdates?.(() => {
    window.dispatchEvent(new CustomEvent("braze:banners"));
  });
  braze.subscribeToContentCardsUpdates?.(() => {
    window.dispatchEvent(new CustomEvent("braze:content-cards"));
  });
  brazeSubscriptionsRegistered = true;
}

/**
 * Start the Braze Web SDK for an identified user only.
 * Order (per Web SDK types): `initialize` → `changeUser` → subscribe to banners/cards **before** `openSession`,
 * then IAM, refresh requests, then `openSession` last.
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
        allowUserSuppliedJavascript: true,
      });
    }

    if (!braze.isInitialized?.()) {
      return {
        success: false,
        message:
          "initialize() ran but SDK reports not initialized — check API key and endpoint match your Braze workspace (Web).",
      };
    }

    if (lastIdentifiedUserId === id) {
      return { success: true };
    }

    braze.changeUser(id);
    registerBrazeSubscriptionsOnce(braze);
    braze.automaticallyShowInAppMessages();

    await completeIdentifiedUserAfterChangeUser(id);

    braze.requestBannersRefresh(ALL_BANNER_PLACEMENT_IDS);
    braze.requestContentCardsRefresh();

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
 * Flush logout attributes/events, then tear down Braze so the next login can re-init cleanly.
 */
export async function brazeOnLogout(previousUserId: string | null): Promise<void> {
  try {
    const braze = await import("@braze/web-sdk");
    if (braze.isInitialized?.() && previousUserId) {
      await brazePreLogout(previousUserId);
    }
    if (braze.isInitialized?.()) {
      braze.destroy();
    }
  } catch {
    /* SDK never loaded */
  } finally {
    lastIdentifiedUserId = null;
    brazeSubscriptionsRegistered = false;
  }
}

/**
 * Destroy SDK and reset local init state without `logged_out` (e.g. Settings → save new API key, same user).
 */
export async function brazeDestroyForReconnect(): Promise<void> {
  try {
    const braze = await import("@braze/web-sdk");
    if (braze.isInitialized?.()) {
      braze.destroy();
    }
  } catch {
    /* SDK never loaded */
  } finally {
    lastIdentifiedUserId = null;
    brazeSubscriptionsRegistered = false;
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
