import { useState } from "react";
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

async function copyPlainText(text: string, logLabel: string) {
  try {
    await navigator.clipboard.writeText(text);
    brazeAppLog({
      type: "event",
      message: `[Braze clipboard] ${logLabel}`,
    });
  } catch (e) {
    brazeAppLog({
      type: "error",
      message: `[Braze clipboard] ${logLabel}`,
      detail: e instanceof Error ? e.message : String(e),
    });
  }
}

function copyRawSnippet(lines: string[], logLabel: string) {
  void copyPlainText(wrapBrazeForConsolePaste(lines), logLabel);
}

function CodeLabel({ children }: { children: string }) {
  return <code className="braze-sidebar-code">{children}</code>;
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

export function BrazeSidebarPanel() {
  const [open, setOpen] = useState(false);
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

  const toggleOpen = () => setOpen((o) => !o);

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
      "manual initialize script (paste in console)",
    );
  };

  return (
    <div className="braze-sidebar-block">
      <button
        type="button"
        className="braze-sidebar-toggle"
        aria-expanded={open}
        onClick={toggleOpen}
      >
        <span>Braze</span>
        <span className="braze-sidebar-chevron" data-open={open}>
          ▾
        </span>
      </button>

      {open ? (
        <div
          className="braze-sidebar-tools"
          role="region"
          aria-label="Braze SDK clipboard snippets"
        >
          <div className="braze-manual-init">
            <div className="braze-tool-row braze-tool-row--code braze-manual-top">
              <div className="braze-manual-top-text">
                <CodeLabel>braze.initialize(apiKey, options)</CodeLabel>
              </div>
              <CopySnippetButton
                title="Copy full init script to clipboard"
                onClick={onManualInitCopy}
              />
            </div>
            <details className="braze-manual-details">
              <summary className="braze-manual-details-summary">
                Initialize options
              </summary>
              <div className="braze-manual-details-body">
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
                <p className="braze-manual-post-title">After initialize</p>
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
            </details>
          </div>

          <div className="braze-tool-row braze-tool-row--code">
            <CodeLabel>braze.requestContentCardsRefresh()</CodeLabel>
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
            <CodeLabel>braze.requestBannersRefresh(…)</CodeLabel>
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

          <div className="braze-tool-row braze-tool-row--field">
            <CodeLabel>braze.getBanner(placementId)</CodeLabel>
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

          <div className="braze-tool-row braze-tool-row--code">
            <CodeLabel>braze.requestImmediateDataFlush()</CodeLabel>
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
            <CodeLabel>braze.refreshFeatureFlags()</CodeLabel>
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

          <div className="braze-tool-row braze-tool-row--field">
            <CodeLabel>braze.getFeatureFlag(id)</CodeLabel>
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
                    (
                      e.currentTarget.parentElement?.querySelector(
                        "button",
                      ) as HTMLButtonElement | null
                    )?.click();
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

          <div className="braze-tool-row braze-tool-row--field">
            <CodeLabel>braze.changeUser(userId)</CodeLabel>
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
                    (
                      e.currentTarget.parentElement?.querySelector(
                        "button",
                      ) as HTMLButtonElement | null
                    )?.click();
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
            <CodeLabel>braze.logCustomEvent(name)</CodeLabel>
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
                    (
                      e.currentTarget.parentElement?.querySelector(
                        "button",
                      ) as HTMLButtonElement | null
                    )?.click();
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
            <CodeLabel>braze.openSession()</CodeLabel>
            <CopySnippetButton
              title="Copy openSession snippet"
              onClick={() =>
                copyRawSnippet(["braze.openSession?.();"], "openSession()")
              }
            />
          </div>

          <div className="braze-tool-row braze-tool-row--code">
            <CodeLabel>braze.requestPushPermission(success, error)</CodeLabel>
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

          <div className="braze-tool-row braze-tool-row--code">
            <CodeLabel>braze.getUser().getUserId()</CodeLabel>
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
            <CodeLabel>braze.getCachedContentCards()</CodeLabel>
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
            <CodeLabel>braze.isInitialized()</CodeLabel>
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

          <div className="braze-tool-row braze-tool-row--code">
            <CodeLabel>braze.destroy()</CodeLabel>
            <CopySnippetButton
              danger
              title="Copy destroy snippet"
              onClick={() =>
                copyRawSnippet(["braze.destroy();"], "destroy()")
              }
            />
          </div>

          <div className="braze-tool-row braze-tool-row--code">
            <CodeLabel>braze.wipeData()</CodeLabel>
            <CopySnippetButton
              danger
              title="Copy wipeData snippet"
              onClick={() =>
                copyRawSnippet(["braze.wipeData?.();"], "wipeData()")
              }
            />
          </div>
        </div>
      ) : null}
    </div>
  );
}
