/**
 * Braze Web push helpers (@braze/web-sdk 6.8).
 * Docs: Context7 `/braze-inc/braze-web-sdk` — initialize `serviceWorkerLocation`, `requestPushPermission`,
 * `isPushSupported` / `isPushBlocked` / `isPushPermissionGranted`, `User.setPushNotificationSubscriptionType`, `requestImmediateDataFlush`.
 */

export type BrazePushSnapshot = {
  initialized: boolean;
  supported: boolean | undefined;
  blocked: boolean | undefined;
  permissionGranted: boolean | undefined;
};

export type BrazePushUiState =
  | "sdk_offline"
  | "unsupported"
  | "blocked"
  | "prompt"
  | "enabled";

export async function readBrazePushSnapshot(): Promise<BrazePushSnapshot> {
  const braze = await import("@braze/web-sdk");
  const initialized = braze.isInitialized?.() ?? false;
  if (!initialized) {
    return {
      initialized: false,
      supported: undefined,
      blocked: undefined,
      permissionGranted: undefined,
    };
  }
  return {
    initialized: true,
    supported: braze.isPushSupported(),
    blocked: braze.isPushBlocked(),
    permissionGranted: braze.isPushPermissionGranted(),
  };
}

export function brazePushUiStateFromSnapshot(s: BrazePushSnapshot): BrazePushUiState {
  if (!s.initialized) return "sdk_offline";
  if (s.supported === false) return "unsupported";
  if (s.blocked === true) return "blocked";
  if (s.permissionGranted === true) return "enabled";
  return "prompt";
}

export type BrazePushLogFn = (type: "info" | "error", message: string) => void;

/**
 * Prompts for browser push (if supported), sets push subscription to OPTED_IN, then flushes the queue.
 */
export function requestBrazeWebPushOptIn(log?: BrazePushLogFn): void {
  void import("@braze/web-sdk").then((braze) => {
    if (!braze.isInitialized?.()) {
      log?.("error", "requestPushPermission skipped — SDK not initialized.");
      return;
    }
    braze.requestPushPermission(
      (_endpoint, _publicKey, _userAuth) => {
        const user = braze.getUser();
        if (user) {
          user.setPushNotificationSubscriptionType(
            braze.User.NotificationSubscriptionTypes.OPTED_IN,
          );
        }
        braze.requestImmediateDataFlush((success) => {
          log?.(
            success ? "info" : "error",
            success
              ? "Push registration succeeded; setPushNotificationSubscriptionType(OPTED_IN); requestImmediateDataFlush OK."
              : "Push registration succeeded; setPushNotificationSubscriptionType(OPTED_IN); flush pending (will retry).",
          );
        });
        window.dispatchEvent(new CustomEvent("braze:push-state-changed"));
      },
      (temporaryDenial) => {
        log?.(
          "error",
          temporaryDenial
            ? "requestPushPermission denied (temporary — user may try again)."
            : "requestPushPermission denied or registration error (permanent or unsupported).",
        );
        window.dispatchEvent(new CustomEvent("braze:push-state-changed"));
      },
    );
  });
}
