import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useBrazeLogs } from "../context/BrazeLogContext";
import { useProfile } from "../context/ProfileContext";
import {
  BANNER_PLACEMENTS,
  BRAZE_FEATURE_FLAG_VIP,
  CONTENT_CARD_SLOTS,
  DEEPLINK_REFERENCE,
} from "../lib/brazeConstants";
import {
  brazeDestroyForReconnect,
  initBrazeForIdentifiedUser,
} from "../lib/brazeInit";
import {
  getBrazeSettings,
  saveBrazeSettings,
} from "../lib/brazeSettings";

/*
  Braze (not shown in UI):
  - Package: @braze/web-sdk (see brazeConstants BRAZE_WEB_SDK_RANGE). Credentials in localStorage.
  - SDK starts after Profile login; if already logged in, Save runs destroy() then saves then initBrazeForIdentifiedUser.
  - Use Web app SDK key + SDK endpoint from Braze (Manage Settings → Apps → Web). Android/REST-only keys often fail.
  - Banner / card / flag / deep link sections below are reference IDs for dashboard setup.
*/

function copyValue(value: string, label: string) {
  void navigator.clipboard.writeText(value);
  return `${label} copied`;
}

export function SettingsPage() {
  const navigate = useNavigate();
  const { pushLog } = useBrazeLogs();
  const { currentUserId } = useProfile();
  const [apiKey, setApiKey] = useState(() => getBrazeSettings().apiKey);
  const [baseUrl, setBaseUrl] = useState(() => getBrazeSettings().baseUrl);
  const [toast, setToast] = useState<string | null>(null);
  const [saveBusy, setSaveBusy] = useState(false);

  const showToast = (msg: string) => {
    setToast(msg);
    setTimeout(() => setToast(null), 2500);
  };

  const saveCredentials = async () => {
    setSaveBusy(true);
    try {
      if (currentUserId) {
        await brazeDestroyForReconnect();
        saveBrazeSettings({ apiKey, baseUrl });
        const result = await initBrazeForIdentifiedUser(currentUserId);
        if (result.success) {
          pushLog({
            type: "info",
            message: `Credentials saved; Braze restarted for user "${currentUserId}".`,
          });
          showToast("Saved and Braze reconnected for your account.");
        } else {
          pushLog({
            type: "error",
            message: `Credentials saved but Braze did not start: ${result.message}`,
          });
          showToast("Saved locally. See Logs for the Braze error detail.");
        }
      } else {
        saveBrazeSettings({ apiKey, baseUrl });
        pushLog({
          type: "info",
          message:
            "Braze credentials saved. SDK starts when you log in on Profile.",
        });
        showToast("Saved. Log in on Profile to start the Braze SDK.");
      }
    } finally {
      setSaveBusy(false);
    }
  };

  const origin =
    typeof window !== "undefined" ? window.location.origin : "";

  return (
    <>
      <h1 className="page-title">Settings</h1>
      <p>
        <button
          type="button"
          className="btn btn-ghost"
          onClick={() => navigate(-1)}
        >
          ← Close
        </button>
      </p>
      {toast ? <p className="settings-toast">{toast}</p> : null}

      <div className="settings-grid">
        <section className="settings-card">
          <h2>Braze Web SDK</h2>
          <div className="stack tight">
            <div className="form-row">
              <label htmlFor="bk">Web API key</label>
              <input
                id="bk"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                autoComplete="off"
                spellCheck={false}
              />
            </div>
            <div className="form-row">
              <label htmlFor="ep">SDK endpoint (hostname only)</label>
              <input
                id="ep"
                value={baseUrl}
                onChange={(e) => setBaseUrl(e.target.value)}
                placeholder="sdk.xxx.braze.com"
                spellCheck={false}
              />
            </div>
            <button
              type="button"
              className="btn btn-primary"
              disabled={saveBusy}
              onClick={() => void saveCredentials()}
            >
              {saveBusy ? "Saving…" : "Save credentials"}
            </button>
          </div>
        </section>

        <section className="settings-card">
          <h2>Banner placement IDs</h2>
          <ul className="settings-ref-list">
            {BANNER_PLACEMENTS.map((row) => (
              <li key={row.id}>
                <div className="settings-ref-body">
                  <strong>{row.label}</strong>
                  <code>{row.id}</code>
                </div>
                <button
                  type="button"
                  className="btn btn-ghost btn-small"
                  onClick={() =>
                    showToast(copyValue(row.id, "Placement ID"))
                  }
                >
                  Copy ID
                </button>
              </li>
            ))}
          </ul>
        </section>

        <section className="settings-card">
          <h2>Content card slots</h2>
          <ul className="settings-ref-list">
            {CONTENT_CARD_SLOTS.map((row) => (
              <li key={row.id}>
                <div className="settings-ref-body">
                  <strong>{row.label}</strong>
                  <code>{row.id}</code>
                </div>
                <button
                  type="button"
                  className="btn btn-ghost btn-small"
                  onClick={() => showToast(copyValue(row.id, "Slot key"))}
                >
                  Copy key
                </button>
              </li>
            ))}
          </ul>
        </section>

        <section className="settings-card">
          <h2>Feature flag (VIP tab)</h2>
          <div className="settings-ref-body settings-ref-body--solo">
            <code>{BRAZE_FEATURE_FLAG_VIP}</code>
          </div>
          <button
            type="button"
            className="btn btn-ghost btn-small"
            onClick={() =>
              showToast(copyValue(BRAZE_FEATURE_FLAG_VIP, "Flag ID"))
            }
          >
            Copy flag ID
          </button>
        </section>

        <section className="settings-card">
          <h2>Deep links</h2>
          <ul className="settings-ref-list">
            {DEEPLINK_REFERENCE.map((row) => {
              const full =
                origin && row.webPath.startsWith("/")
                  ? `${origin}${row.webPath}`
                  : row.webPath;
              return (
                <li key={row.appUri}>
                  <div className="settings-ref-body">
                    <strong>{row.label}</strong>
                    <code className="block">{row.appUri}</code>
                    <code className="block muted">{full}</code>
                  </div>
                  <div className="settings-ref-actions">
                    <button
                      type="button"
                      className="btn btn-ghost btn-small"
                      onClick={() =>
                        showToast(copyValue(row.appUri, "App URI"))
                      }
                    >
                      Copy app URI
                    </button>
                    <button
                      type="button"
                      className="btn btn-ghost btn-small"
                      onClick={() =>
                        showToast(copyValue(full, "Web URL"))
                      }
                    >
                      Copy web URL
                    </button>
                  </div>
                </li>
              );
            })}
          </ul>
        </section>
      </div>
    </>
  );
}
