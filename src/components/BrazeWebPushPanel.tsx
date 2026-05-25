import { useCallback, useEffect, useState } from "react";
import { useBrazeLogs } from "../context/BrazeLogContext";
import { useProfile } from "../context/ProfileContext";
import {
  brazePushUiStateFromSnapshot,
  readBrazePushSnapshot,
  requestBrazeWebPushOptIn,
  type BrazePushSnapshot,
  type BrazePushUiState,
} from "../lib/brazeWebPush";

function describePushState(ui: BrazePushUiState): string {
  switch (ui) {
    case "sdk_offline":
      return "Braze SDK not ready yet (log in and wait for initialization).";
    case "unsupported":
      return "Web push is not supported in this browser or context (HTTPS required in production).";
    case "blocked":
      return "Notifications are blocked for this site. Change permission in browser settings to enable push.";
    case "prompt":
      return "Push is supported. Use the button below to opt in and allow browser notifications.";
    case "enabled":
      return "Browser push permission is granted. Subscription type OPTED_IN is set after a successful opt-in.";
    default:
      return "";
  }
}

function rawSnapshotHint(snap: BrazePushSnapshot): string | null {
  if (!snap.initialized) return null;
  return `SDK: supported=${String(snap.supported)}, blocked=${String(snap.blocked)}, permission=${String(snap.permissionGranted)}`;
}

export function BrazeWebPushPanel() {
  const { currentUserId } = useProfile();
  const { pushLog } = useBrazeLogs();
  const [snap, setSnap] = useState<BrazePushSnapshot>({
    initialized: false,
    supported: undefined,
    blocked: undefined,
    permissionGranted: undefined,
  });

  const refresh = useCallback(() => {
    void readBrazePushSnapshot().then(setSnap);
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh, currentUserId]);

  useEffect(() => {
    const onReady = () => refresh();
    const onPush = () => refresh();
    window.addEventListener("braze:identified-ready", onReady);
    window.addEventListener("braze:push-state-changed", onPush);
    document.addEventListener("visibilitychange", onPush);
    return () => {
      window.removeEventListener("braze:identified-ready", onReady);
      window.removeEventListener("braze:push-state-changed", onPush);
      document.removeEventListener("visibilitychange", onPush);
    };
  }, [refresh]);

  if (!currentUserId) return null;

  const ui = brazePushUiStateFromSnapshot(snap);
  const hint = rawSnapshotHint(snap);

  const onOptIn = () => {
    pushLog({
      type: "info",
      message:
        "Web push: user clicked opt-in — requestPushPermission (then OPTED_IN + requestImmediateDataFlush on success).",
    });
    requestBrazeWebPushOptIn((type, message) => pushLog({ type, message }));
  };

  return (
    <section className="braze-push-panel stack" aria-labelledby="braze-push-heading">
      <h2 id="braze-push-heading" className="profile-vip-status__title">
        Web push (Braze)
      </h2>
      <p className="product-meta">{describePushState(ui)}</p>
      {hint ? (
        <p className="product-meta" style={{ fontFamily: "monospace", fontSize: "0.85em" }}>
          {hint}
        </p>
      ) : null}
      <div className="profile-actions">
        <button
          type="button"
          className="btn btn-primary"
          disabled={ui !== "prompt"}
          onClick={onOptIn}
        >
          Enable push notifications
        </button>
      </div>
    </section>
  );
}
