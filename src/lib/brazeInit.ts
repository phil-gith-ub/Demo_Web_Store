import { ALL_BANNER_PLACEMENT_IDS } from "./brazeConstants";
import { brazeAppLog } from "./brazeAppLog";
import { getAllowUserSuppliedJavascriptForInit } from "./brazeDemoSdkPrefs";
import { DEMOSTORE_DOM_SCAN_EVENT } from "./demostoreDeepLink";
import { getBrazeSettings, normalizeBrazeBaseUrl } from "./brazeSettings";
import {
  brazePreLogout,
  completeIdentifiedUserAfterChangeUser,
  type CompleteIdentifiedUserOptions,
} from "./brazeUserSyncWeb";

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
  braze.subscribeToContentCardsUpdates?.((updates) => {
    window.dispatchEvent(
      new CustomEvent("braze:content-cards", { detail: updates }),
    );
  });
  brazeSubscriptionsRegistered = true;
}

/** Push current `getCachedContentCards()` to the same bus as `subscribeToContentCardsUpdates` (after refresh completes). */
export function dispatchCachedContentCardsDetail(
  braze: typeof import("@braze/web-sdk"),
) {
  const cc = braze.getCachedContentCards?.();
  if (cc) {
    window.dispatchEvent(new CustomEvent("braze:content-cards", { detail: cc }));
  }
}

/** After `destroy()`, allow banners/cards/IAM hooks to register again. */
export function resetBrazeSdkHookRegistrationState(): void {
  brazeSubscriptionsRegistered = false;
  inAppMessageHandlerRegistered = false;
}

/**
 * Route Braze SDK debug strings into the in-app Logs page (`brazeAppLog` → `registerBrazeAppLogSink`).
 * @see https://github.com/braze-inc/braze-web-sdk — `setLogger`
 */
export function attachBrazeAppSdkLogger(braze: typeof import("@braze/web-sdk")) {
  braze.setLogger((message: string) => {
    if (import.meta.env.DEV) {
      console.debug(message);
    }
    const lower = message.toLowerCase();
    let type: "info" | "request" | "response" | "event" | "error" = "info";
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
}

export function registerBrazeBannersAndContentCardsSubscribers(
  braze: typeof import("@braze/web-sdk"),
) {
  registerBrazeSubscriptionsOnce(braze);
}

export function registerBrazeInAppMessageHandler(
  braze: typeof import("@braze/web-sdk"),
) {
  registerInAppMessagesOnce(braze);
}

function inAppMessageLogSummary(message: {
  isControl?: boolean;
  triggerId?: string;
  constructor?: { name?: string };
}): string {
  if (message.isControl === true) {
    return `ControlMessage triggerId=${message.triggerId ?? "(none)"}`;
  }
  const kind = message.constructor?.name ?? "InAppMessage";
  return `${kind} triggerId=${message.triggerId ?? "(none)"}`;
}

function registerInAppMessagesOnce(braze: typeof import("@braze/web-sdk")) {
  if (inAppMessageHandlerRegistered) return;
  const subId = braze.subscribeToInAppMessage?.((message) => {
    brazeAppLog({
      type: "event",
      message: `subscribeToInAppMessage callback: ${inAppMessageLogSummary(message)}`,
    });
    const displayed = braze.showInAppMessage?.(message);
    brazeAppLog({
      type: "info",
      message: `showInAppMessage → ${String(displayed)} (${inAppMessageLogSummary(message)})`,
    });
    if (import.meta.env.DEV) {
      console.debug("[braze IAM]", inAppMessageLogSummary(message), "displayed=", displayed);
    }
    queueMicrotask(() => {
      window.dispatchEvent(new Event(DEMOSTORE_DOM_SCAN_EVENT));
    });
  });
  if (subId !== undefined) {
    brazeAppLog({
      type: "info",
      message: `subscribeToInAppMessage registered (before changeUser / session) — id=${subId}`,
    });
  } else {
    brazeAppLog({
      type: "error",
      message:
        "subscribeToInAppMessage returned undefined — using automaticallyShowInAppMessages fallback (check isInitialized)",
    });
    braze.automaticallyShowInAppMessages?.();
  }
  inAppMessageHandlerRegistered = true;
}

/**
 * Start the Braze Web SDK for an identified user only (no init until login — no anonymous Braze usage).
 * Order: `initialize` → register **`subscribeToInAppMessage` / `subscribeToContentCardsUpdates` /
 * `subscribeToBannersUpdates`** → **`changeUser`**. We do **not** call `openSession()` here; the SDK starts a
 * session when `changeUser` applies a new user id (see Web SDK request controller). Then
 * `requestBannersRefresh` / `requestContentCardsRefresh` for feeds after identification.
 *
 * @param refreshKey Profile `refreshKey` — increments on every Log in / Log out; resets to 0 on full page load.
 * @param initOptions Optional; set `logLoginEvent: true` only when init is due to Profile **Log in** this session
 *   (e.g. `refreshKey > 0`). Session restore after refresh and Settings reconnect should use `logLoginEvent: false`.
 */
export async function initBrazeForIdentifiedUser(
  userId: string,
  refreshKey?: number,
  initOptions?: CompleteIdentifiedUserOptions,
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
          b.requestContentCardsRefresh(() => dispatchCachedContentCardsDetail(b));
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
      resetBrazeSdkHookRegistrationState();
    }

    const braze = await import("@braze/web-sdk");

    if (!braze.isInitialized?.()) {
      braze.initialize(key, {
        baseUrl: host,
        enableLogging: true,
        allowUserSuppliedJavascript: getAllowUserSuppliedJavascriptForInit(),
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

    attachBrazeAppSdkLogger(braze);

    registerBrazeSubscriptionsOnce(braze);
    registerInAppMessagesOnce(braze);

    braze.changeUser(id);

    await completeIdentifiedUserAfterChangeUser(id, initOptions);

    braze.requestBannersRefresh(ALL_BANNER_PLACEMENT_IDS);

    /* Session is opened by the SDK as part of `changeUser` when the user id changes; no explicit `openSession()`. */
    braze.requestContentCardsRefresh(() => dispatchCachedContentCardsDetail(braze));
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
    resetBrazeSdkHookRegistrationState();
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
    resetBrazeSdkHookRegistrationState();
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
