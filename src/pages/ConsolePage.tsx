import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { brazeAppLog } from "../lib/brazeAppLog";
import { wrapBrazeForConsolePaste } from "../lib/brazeClipboardSnippets";
import {
  ALL_BANNER_PLACEMENT_IDS,
  BANNER_PLACEMENTS,
  BRAZE_FEATURE_FLAG_VIP,
} from "../lib/brazeConstants";
import { getAllowUserSuppliedJavascriptForInit } from "../lib/brazeDemoSdkPrefs";
import {
  buildManualInitClipboardScript,
  type ManualBrazeInitFlags,
  type ManualBrazeInitPostSteps,
} from "../lib/brazeManualInitialize";
import { getBrazeSettings, normalizeBrazeBaseUrl } from "../lib/brazeSettings";

function CodeLabel({ children }: { children: string }) {
  return <code className="braze-console-code">{children}</code>;
}

function CopyIcon() {
  return (
    <svg
      className="braze-copy-icon"
      viewBox="0 0 24 24"
      width={16}
      height={16}
      aria-hidden
    >
      <rect
        x="4"
        y="4"
        width="12"
        height="12"
        rx="1.25"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
      />
      <rect
        x="8"
        y="8"
        width="12"
        height="12"
        rx="1.25"
        fill="none"
        stroke="currentColor"
        strokeWidth="1.5"
      />
    </svg>
  );
}

function CopySnippetButton({
  title,
  onClick,
  danger,
}: {
  title: string;
  onClick: () => void;
  danger?: boolean;
}) {
  return (
    <button
      type="button"
      className={
        "braze-tool-icon" + (danger ? " braze-tool-icon--danger" : "")
      }
      title={title}
      aria-label={title}
      onClick={onClick}
    >
      <CopyIcon />
      <span className="btn-copy-text">Copy</span>
    </button>
  );
}

function ManualCheck({
  id,
  label,
  checked,
  onChange,
}: {
  id: string;
  label: string;
  checked: boolean;
  onChange: (next: boolean) => void;
}) {
  return (
    <label className="braze-manual-check" htmlFor={id}>
      <input
        id={id}
        type="checkbox"
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
      />
      <span>{label}</span>
    </label>
  );
}

export function ConsolePage() {
  const navigate = useNavigate();
  const [toast, setToast] = useState<string | null>(null);

  // States from BrazeSidebarPanel
  const [manualDestroyFirst, setManualDestroyFirst] = useState(true);
  const [manualEnableLogging, setManualEnableLogging] = useState(true);
  const [manualManageSw, setManualManageSw] = useState(false);
  const [manualSwLoc, setManualSwLoc] = useState("/service-worker.js");
  const [postLogger, setPostLogger] = useState(true);
  const [postFeeds, setPostFeeds] = useState(true);
  const [postIam, setPostIam] = useState(true);
  const [postBanners, setPostBanners] = useState(true);
  const [postContentCards, setPostContentCards] = useState(true);
  const [bannerRefreshTarget, setBannerRefreshTarget] =
    useState<string>("__all__");
  const [bannerGetId, setBannerGetId] = useState<string>(
    BANNER_PLACEMENTS[0]?.id ?? "",
  );
  const [getFeatureFlagId, setGetFeatureFlagId] = useState<string>(
    BRAZE_FEATURE_FLAG_VIP,
  );
  const [changeUserId, setChangeUserId] = useState("");
  const [customEvent, setCustomEvent] = useState("");

  const showToast = (message: string) => {
    setToast(message);
    setTimeout(() => setToast(null), 2500);
  };

  const copyPlainText = async (text: string, logLabel: string) => {
    try {
      await navigator.clipboard.writeText(text);
      brazeAppLog({
        type: "event",
        message: `[Braze clipboard] ${logLabel}`,
      });
      showToast(`Copied snippet for ${logLabel}`);
    } catch (e) {
      brazeAppLog({
        type: "error",
        message: `[Braze clipboard] ${logLabel}`,
        detail: e instanceof Error ? e.message : String(e),
      });
      showToast("Failed to copy snippet");
    }
  };

  const copyRawSnippet = (lines: string[], logLabel: string) => {
    void copyPlainText(wrapBrazeForConsolePaste(lines), logLabel);
  };

  const onManualInitCopy = () => {
    const { apiKey, baseUrl } = getBrazeSettings();
    const key = apiKey.trim();
    const host = normalizeBrazeBaseUrl(baseUrl);
    if (!key || !host) {
      brazeAppLog({
        type: "error",
        message:
          "[Braze clipboard] manual init — set Web API key and SDK endpoint in Settings first",
      });
      showToast("Configure Web API key and SDK endpoint in Settings first");
      return;
    }
    const flags: ManualBrazeInitFlags = {
      destroyFirst: manualDestroyFirst,
      allowUserSuppliedJavascript: getAllowUserSuppliedJavascriptForInit(),
      enableLogging: manualEnableLogging,
      manageServiceWorkerExternally: manualManageSw,
      serviceWorkerLocation: manualSwLoc,
    };
    const post: ManualBrazeInitPostSteps = {
      setLogger: postLogger,
      subscribeBannerAndContentCards: postFeeds,
      subscribeInAppMessage: postIam,
      requestBannersRefresh: postBanners,
      requestContentCardsRefresh: postContentCards,
    };
    const script = buildManualInitClipboardScript(key, host, flags, post);
    void copyPlainText(
      script,
      "manual initialize script",
    );
  };

  return (
    <>
      <h1 className="page-title">Console</h1>
      <p className="console-back-nav">
        <button
          type="button"
          className="btn btn-ghost"
          onClick={() => navigate(-1)}
        >
          ← Close
        </button>
      </p>
      {toast ? <p className="settings-toast">{toast}</p> : null}

      <div className="console-layout">
        {/* Full-width Initialization Card */}
        <section className="console-card console-card--full">
          <h2>1. Manual SDK Initialization</h2>
          <p className="product-meta">
            Configure, generate, and copy a fully integrated initialization script tailored for this specific environment. Paste it in your developer console to reset and re-initialize Braze.
          </p>

          <div className="console-manual-init-grid">
            <div className="console-manual-init-left">
              <div className="braze-tool-row braze-tool-row--code braze-manual-top">
                <div className="braze-manual-top-text">
                  <CodeLabel>braze.initialize(apiKey, options)</CodeLabel>
                </div>
                <CopySnippetButton
                  title="Copy full init script to clipboard"
                  onClick={onManualInitCopy}
                />
              </div>

              <div className="console-manual-checks-group">
                <h3>Initialize options</h3>
                <div className="console-checkbox-grid">
                  <ManualCheck
                    id="braze-manual-destroy"
                    label="destroy() first if already initialized"
                    checked={manualDestroyFirst}
                    onChange={setManualDestroyFirst}
                  />
                  <ManualCheck
                    id="braze-manual-logging"
                    label="enableLogging"
                    checked={manualEnableLogging}
                    onChange={setManualEnableLogging}
                  />
                  <ManualCheck
                    id="braze-manual-manage-sw"
                    label="manageServiceWorkerExternally"
                    checked={manualManageSw}
                    onChange={setManualManageSw}
                  />
                </div>

                <div className="console-sw-input-row form-row">
                  <label
                    className="braze-manual-sw-label"
                    htmlFor="braze-manual-sw-loc"
                  >
                    serviceWorkerLocation (ignored if managing SW externally)
                  </label>
                  <input
                    id="braze-manual-sw-loc"
                    type="text"
                    className="braze-tool-input"
                    value={manualSwLoc}
                    onChange={(e) => setManualSwLoc(e.target.value)}
                    disabled={manualManageSw}
                  />
                </div>
              </div>
            </div>

            <div className="console-manual-init-right">
              <h3>Post-initialize chain actions</h3>
              <p className="product-meta">
                Automatically chain helper callbacks and subscriptions onto the initialized Braze SDK instance.
              </p>
              <div className="console-checkbox-vertical">
                <ManualCheck
                  id="braze-post-logger"
                  label="setLogger → console.debug"
                  checked={postLogger}
                  onChange={setPostLogger}
                />
                <ManualCheck
                  id="braze-post-feeds"
                  label="subscribeToBannersUpdates + subscribeToContentCardsUpdates"
                  checked={postFeeds}
                  onChange={setPostFeeds}
                />
                <ManualCheck
                  id="braze-post-iam"
                  label="subscribeToInAppMessage (+ showInAppMessage)"
                  checked={postIam}
                  onChange={setPostIam}
                />
                <ManualCheck
                  id="braze-post-ban"
                  label={`requestBannersRefresh(${JSON.stringify(ALL_BANNER_PLACEMENT_IDS)})`}
                  checked={postBanners}
                  onChange={setPostBanners}
                />
                <ManualCheck
                  id="braze-post-cc"
                  label="requestContentCardsRefresh(…)"
                  checked={postContentCards}
                  onChange={setPostContentCards}
                />
              </div>
            </div>
          </div>
        </section>

        {/* 2-column or 3-column Grid for Other Commands */}
        <div className="console-actions-grid">
          {/* Card 2: SDK Data Refresh */}
          <section className="console-card">
            <h2>2. SDK Data Refresh</h2>
            <p className="product-meta">
              Force the Braze Web SDK to fetch fresh campaigns, content cards, feature flags, or flush the queue.
            </p>

            <div className="console-card-body">
              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.requestContentCardsRefresh()</CodeLabel>
                  <span className="product-meta">Refreshes Content Cards.</span>
                </div>
                <CopySnippetButton
                  title="Copy requestContentCardsRefresh snippet"
                  onClick={() =>
                    copyRawSnippet(
                      ["braze.requestContentCardsRefresh();"],
                      "requestContentCardsRefresh()",
                    )
                  }
                />
              </div>

              <div className="braze-tool-row braze-tool-row--field">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.requestBannersRefresh(…)</CodeLabel>
                  <span className="product-meta">Request banners refresh for a placement.</span>
                </div>
                <div className="braze-tool-inline">
                  <select
                    className="braze-tool-select"
                    value={bannerRefreshTarget}
                    onChange={(e) => setBannerRefreshTarget(e.target.value)}
                  >
                    <option value="__all__">All placements</option>
                    {BANNER_PLACEMENTS.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.label} ({p.id})
                      </option>
                    ))}
                  </select>
                  <CopySnippetButton
                    title="Copy requestBannersRefresh snippet"
                    onClick={() => {
                      const ids =
                        bannerRefreshTarget === "__all__"
                          ? ALL_BANNER_PLACEMENT_IDS
                          : [bannerRefreshTarget];
                      copyRawSnippet(
                        [`braze.requestBannersRefresh(${JSON.stringify(ids)});`],
                        `requestBannersRefresh(${JSON.stringify(ids)})`,
                      );
                    }}
                  />
                </div>
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.requestImmediateDataFlush()</CodeLabel>
                  <span className="product-meta">Instantly sync pending custom events and attributes.</span>
                </div>
                <CopySnippetButton
                  title="Copy requestImmediateDataFlush snippet"
                  onClick={() =>
                    copyRawSnippet(
                      ["braze.requestImmediateDataFlush();"],
                      "requestImmediateDataFlush()",
                    )
                  }
                />
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.refreshFeatureFlags()</CodeLabel>
                  <span className="product-meta">Refreshes configured feature flags.</span>
                </div>
                <CopySnippetButton
                  title="Copy refreshFeatureFlags snippet"
                  onClick={() =>
                    copyRawSnippet(
                      ["braze.refreshFeatureFlags?.();"],
                      "refreshFeatureFlags()",
                    )
                  }
                />
              </div>
            </div>
          </section>

          {/* Card 3: User & Custom Events */}
          <section className="console-card">
            <h2>3. User & Custom Events</h2>
            <p className="product-meta">
              Change the active user context, track custom interactions, or trigger push opt-in.
            </p>

            <div className="console-card-body">
              <div className="braze-tool-row braze-tool-row--field">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.changeUser(userId)</CodeLabel>
                  <span className="product-meta">Switch identified user on this client.</span>
                </div>
                <div className="braze-tool-inline">
                  <input
                    type="text"
                    className="braze-tool-input"
                    placeholder="external user id"
                    value={changeUserId}
                    onChange={(e) => setChangeUserId(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault();
                        const id = changeUserId.trim();
                        if (id) {
                          copyRawSnippet(
                            [`braze.changeUser(${JSON.stringify(id)});`],
                            `changeUser(${JSON.stringify(id)})`,
                          );
                        }
                      }
                    }}
                  />
                  <CopySnippetButton
                    title="Copy changeUser snippet"
                    onClick={() => {
                      const id = changeUserId.trim();
                      if (!id) {
                        void copyPlainText(
                          "// braze.changeUser — enter a user id in the field above, then copy again",
                          "changeUser (placeholder — fill user id first)",
                        );
                        return;
                      }
                      copyRawSnippet(
                        [`braze.changeUser(${JSON.stringify(id)});`],
                        `changeUser(${JSON.stringify(id)})`,
                      );
                    }}
                  />
                </div>
              </div>

              <div className="braze-tool-row braze-tool-row--field">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.logCustomEvent(name)</CodeLabel>
                  <span className="product-meta">Log custom activity to trigger campaigns.</span>
                </div>
                <div className="braze-tool-inline">
                  <input
                    type="text"
                    className="braze-tool-input"
                    placeholder="event name"
                    value={customEvent}
                    onChange={(e) => setCustomEvent(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault();
                        const name = customEvent.trim();
                        if (name) {
                          copyRawSnippet(
                            [`braze.logCustomEvent(${JSON.stringify(name)});`],
                            `logCustomEvent(${JSON.stringify(name)})`,
                          );
                        }
                      }
                    }}
                  />
                  <CopySnippetButton
                    title="Copy logCustomEvent snippet"
                    onClick={() => {
                      const name = customEvent.trim();
                      if (!name) {
                        void copyPlainText(
                          "// braze.logCustomEvent — enter an event name in the field above, then copy again",
                          "logCustomEvent (placeholder — fill event name first)",
                        );
                        return;
                      }
                      copyRawSnippet(
                        [`braze.logCustomEvent(${JSON.stringify(name)});`],
                        `logCustomEvent(${JSON.stringify(name)})`,
                      );
                    }}
                  />
                </div>
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.openSession()</CodeLabel>
                  <span className="product-meta">Manually start a new SDK usage session.</span>
                </div>
                <CopySnippetButton
                  title="Copy openSession snippet"
                  onClick={() =>
                    copyRawSnippet(["braze.openSession?.();"], "openSession()")
                  }
                />
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.requestPushPermission(success, error)</CodeLabel>
                  <span className="product-meta">Triggers standard web browser push opt-in prompt.</span>
                </div>
                <CopySnippetButton
                  title="Copy requestPushPermission snippet"
                  onClick={() =>
                    copyRawSnippet(
                      [
                        `braze.requestPushPermission?.(() => console.log("[braze] push: ok"), () => console.warn("[braze] push: denied"));`,
                      ],
                      "requestPushPermission(...)",
                    )
                  }
                />
              </div>
            </div>
          </section>

          {/* Card 4: Get Attributes & State */}
          <section className="console-card">
            <h2>4. Get Attributes & State</h2>
            <p className="product-meta">
              Query locally cached content, active session details, feature flag values, and initialization status.
            </p>

            <div className="console-card-body">
              <div className="braze-tool-row braze-tool-row--field">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.getBanner(placementId)</CodeLabel>
                  <span className="product-meta">Inspect banners cache for a placement.</span>
                </div>
                <div className="braze-tool-inline">
                  <select
                    className="braze-tool-select"
                    value={bannerGetId}
                    onChange={(e) => setBannerGetId(e.target.value)}
                  >
                    {BANNER_PLACEMENTS.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.id}
                      </option>
                    ))}
                  </select>
                  <CopySnippetButton
                    title="Copy getBanner snippet"
                    onClick={() =>
                      copyRawSnippet(
                        [
                          `console.log(braze.getBanner?.(${JSON.stringify(bannerGetId)}));`,
                        ],
                        `getBanner(${JSON.stringify(bannerGetId)})`,
                      )
                    }
                  />
                </div>
              </div>

              <div className="braze-tool-row braze-tool-row--field">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.getFeatureFlag(id)</CodeLabel>
                  <span className="product-meta">Check feature flag active value.</span>
                </div>
                <div className="braze-tool-inline">
                  <input
                    type="text"
                    className="braze-tool-input"
                    placeholder={BRAZE_FEATURE_FLAG_VIP}
                    value={getFeatureFlagId}
                    onChange={(e) => setGetFeatureFlagId(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") {
                        e.preventDefault();
                        const id = getFeatureFlagId.trim() || BRAZE_FEATURE_FLAG_VIP;
                        copyRawSnippet(
                          [`console.log(braze.getFeatureFlag?.(${JSON.stringify(id)}));`],
                          `getFeatureFlag(${JSON.stringify(id)})`,
                        );
                      }
                    }}
                  />
                  <CopySnippetButton
                    title="Copy getFeatureFlag snippet"
                    onClick={() => {
                      const id = getFeatureFlagId.trim() || BRAZE_FEATURE_FLAG_VIP;
                      copyRawSnippet(
                        [`console.log(braze.getFeatureFlag?.(${JSON.stringify(id)}));`],
                        `getFeatureFlag(${JSON.stringify(id)})`,
                      );
                    }}
                  />
                </div>
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.getUser().getUserId()</CodeLabel>
                  <span className="product-meta">Fetch currently logged-in external user ID.</span>
                </div>
                <CopySnippetButton
                  title="Copy getUser().getUserId() snippet"
                  onClick={() =>
                    copyRawSnippet(
                      [
                        `console.log(braze.getUser?.()?.getUserId?.());`,
                      ],
                      "getUser().getUserId()",
                    )
                  }
                />
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.getCachedContentCards()</CodeLabel>
                  <span className="product-meta">Inspect raw cache of all content cards.</span>
                </div>
                <CopySnippetButton
                  title="Copy getCachedContentCards snippet"
                  onClick={() =>
                    copyRawSnippet(
                      [`console.log(braze.getCachedContentCards?.());`],
                      "getCachedContentCards()",
                    )
                  }
                />
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.isInitialized()</CodeLabel>
                  <span className="product-meta">Verify whether the SDK is actively initialized.</span>
                </div>
                <CopySnippetButton
                  title="Copy isInitialized snippet"
                  onClick={() =>
                    copyRawSnippet(
                      [`console.log(braze.isInitialized?.());`],
                      "isInitialized()",
                    )
                  }
                />
              </div>
            </div>
          </section>

          {/* Card 5: Danger Zone */}
          <section className="console-card console-card--danger">
            <h2 className="danger-text">⚠️ 5. Danger Zone</h2>
            <p className="product-meta">
              Destructive client actions. Clear sessions, wipe local storage data, or shut down active SDK instances.
            </p>

            <div className="console-card-body">
              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.destroy()</CodeLabel>
                  <span className="product-meta">Completely shut down the active SDK, tearing down listeners.</span>
                </div>
                <CopySnippetButton
                  danger
                  title="Copy destroy snippet"
                  onClick={() =>
                    copyRawSnippet(["braze.destroy();"], "destroy()")
                  }
                />
              </div>

              <div className="braze-tool-row braze-tool-row--code">
                <div className="console-cmd-meta">
                  <CodeLabel>braze.wipeData()</CodeLabel>
                  <span className="product-meta">Clear anonymous data, cookies, and local identifiers.</span>
                </div>
                <CopySnippetButton
                  danger
                  title="Copy wipeData snippet"
                  onClick={() =>
                    copyRawSnippet(["braze.wipeData?.();"], "wipeData()")
                  }
                />
              </div>
            </div>
          </section>
        </div>
      </div>
    </>
  );
}
