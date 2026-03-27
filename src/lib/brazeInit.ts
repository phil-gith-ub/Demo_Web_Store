import { ALL_BANNER_PLACEMENT_IDS } from "./brazeConstants";
import { brazeAppLog } from "./brazeAppLog";
import { DEMOSTORE_DOM_SCAN_EVENT } from "./demostoreDeepLink";
import { getBrazeSettings, normalizeBrazeBaseUrl } from "./brazeSettings";
import { completeIdentifiedUserAfterChangeUser, brazePreLogout } from "./brazeUserSyncWeb";

let lastIdentifiedUserId: string | null = null;
/** Last `refreshKey` we ran init with (Profile login bumps key even when user id unchanged). */
let lastBrazeInitRefreshKey: number | null = null;
let brazeSubscriptionsRegistered = false;
let inAppMessageHandlerRegistered = false;

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

function registerInAppMessagesOnce(braze: typeof import("@braze/web-sdk")) {
  if (inAppMessageHandlerRegistered) return;
  const subId = braze.subscribeToInAppMessage?.((message) => {
    braze.showInAppMessage?.(message);
    queueMicrotask(() => {
      window.dispatchEvent(new Event(DEMOSTORE_DOM_SCAN_EVENT));
    });
  });
  if (subId === undefined) {
    braze.automaticallyShowInAppMessages?.();
  }
  inAppMessageHandlerRegistered = true;
}

/**
 * Start the Braze Web SDK for an identified user only.
 * Order (per Braze Web docs): `initialize` → `changeUser` → `subscribeToContentCardsUpdates` /
 * `subscribeToBannersUpdates` **before** `openSession` (required for session-start refreshes). IAM subscription
 * before `openSession`. Then `openSession` last among session lifecycle calls; **`requestContentCardsRefresh`
 * runs after `openSession`** so the feed request happens after the session exists server-side (session-start
 * Canvas/Campaign Content Card steps can appear on this session, not only after a manual refresh).
 *
 * @param refreshKey Profile `refreshKey` — increments on every Log in; same user + new key destroys and re-inits
 *   so `changeUser` runs again (SDK no-ops if user id unchanged without a fresh init).
 */
export async function initBrazeForIdentifiedUser(
  userId: string,
  refreshKey?: number,
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
    const rk = refreshKey ?? 0;
    const sameUserRelogin =
      lastIdentifiedUserId === id &&
      lastBrazeInitRefreshKey !== null &&
      rk !== lastBrazeInitRefreshKey;

    if (lastIdentifiedUserId === id && !sameUserRelogin) {
      /* Still ask the SDK for fresh banners/cards — short-circuit skips the block below that normally refreshes. */
      try {
        const b = await import("@braze/web-sdk");
        if (b.isInitialized?.()) {
          b.requestBannersRefresh(ALL_BANNER_PLACEMENT_IDS);
          b.requestContentCardsRefresh();
        }
      } catch {
        /* ignore */
      }
      return { success: true };
    }

    if (sameUserRelogin) {
      try {
        const b = await import("@braze/web-sdk");
        if (b.isInitialized?.()) b.destroy();
      } catch {
        /* ignore */
      }
      lastIdentifiedUserId = null;
      lastBrazeInitRefreshKey = null;
      brazeSubscriptionsRegistered = false;
      inAppMessageHandlerRegistered = false;
    }

    const braze = await import("@braze/web-sdk");

    if (!braze.isInitialized?.()) {
      braze.initialize(key, {
        baseUrl: host,
        enableLogging: true,
        allowUserSuppliedJavascript: true,
        /**
         * Vite serves `public/service-worker.js` at `/service-worker.js` (site root).
         * HTTPS required for push in production; localhost OK for dev.
         */
        serviceWorkerLocation: "/service-worker.js",
      });
    }

    if (!braze.isInitialized?.()) {
      return {
        success: false,
        message:
          "initialize() ran but SDK reports not initialized — check API key and endpoint match your Braze workspace (Web).",
      };
    }

    /**
     * Route Braze SDK debug strings into the in-app Logs page (`brazeAppLog` → `registerBrazeAppLogSink`).
     * `enableLogging` alone only prints to the browser console; Android-style “everything in one log” needs this.
     * @see https://github.com/braze-inc/braze-web-sdk — `setLogger`
     */
    braze.setLogger((message: string) => {
      if (import.meta.env.DEV) {
        console.debug(message);
      }
      const lower = message.toLowerCase();
      let type: "info" | "request" | "response" | "event" | "error" = "info";
      /* SDK skips cards it cannot parse — usually dashboard/Web mismatch, not an app crash. */
      if (/ignoring card with unknown type/i.test(message)) {
        type = "info";
      } else if (
        /\berror\b|\bfail(ed)?\b|\bexception\b/i.test(message) &&
        !/did not match/i.test(lower)
      ) {
        type = "error";
      } else if (/requesting|fetching|connecting to real-time/i.test(lower)) {
        type = "request";
      } else if (/received|initialized for the braze backend/i.test(lower)) {
        type = "response";
      } else if (
        /trigger|logcustom|logged custom|firing templated|session start/i.test(
          lower,
        )
      ) {
        type = "event";
      }
      brazeAppLog({ type, message });
    });

    braze.changeUser(id);
    registerBrazeSubscriptionsOnce(braze);
    registerInAppMessagesOnce(braze);

    await completeIdentifiedUserAfterChangeUser(id);

    braze.requestBannersRefresh(ALL_BANNER_PLACEMENT_IDS);

    braze.openSession();
    /* SDK also refreshes content cards on session start when subscribed above; this explicit refresh runs after
       the session is opened so server-side “session start” eligibility (e.g. Canvas Content Card steps) is included. */
    braze.requestContentCardsRefresh();
    lastIdentifiedUserId = id;
    lastBrazeInitRefreshKey = rk;
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
    lastBrazeInitRefreshKey = null;
    brazeSubscriptionsRegistered = false;
    inAppMessageHandlerRegistered = false;
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
    lastBrazeInitRefreshKey = null;
    brazeSubscriptionsRegistered = false;
    inAppMessageHandlerRegistered = false;
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
