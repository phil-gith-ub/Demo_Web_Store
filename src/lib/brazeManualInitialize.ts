import type { InitializationOptions } from "@braze/web-sdk";
import { ALL_BANNER_PLACEMENT_IDS } from "./brazeConstants";
import { wrapBrazeForConsolePaste } from "./brazeClipboardSnippets";

export type ManualBrazeInitFlags = {
  destroyFirst: boolean;
  allowUserSuppliedJavascript: boolean;
  enableLogging: boolean;
  manageServiceWorkerExternally: boolean;
  /** Used when `manageServiceWorkerExternally` is false. */
  serviceWorkerLocation: string;
};

export type ManualBrazeInitPostSteps = {
  setLogger: boolean;
  subscribeBannerAndContentCards: boolean;
  subscribeInAppMessage: boolean;
  requestBannersRefresh: boolean;
  requestContentCardsRefresh: boolean;
};

export function buildManualInitializationOptions(
  host: string,
  flags: ManualBrazeInitFlags,
): InitializationOptions {
  const initOpts: InitializationOptions = {
    baseUrl: host,
    enableLogging: flags.enableLogging,
    allowUserSuppliedJavascript: flags.allowUserSuppliedJavascript,
  };
  if (flags.manageServiceWorkerExternally) {
    initOpts.manageServiceWorkerExternally = true;
  } else {
    const sw = flags.serviceWorkerLocation.trim();
    if (sw) initOpts.serviceWorkerLocation = sw;
  }
  return initOpts;
}

/**
 * One paste-ready script: optional destroy, initialize with real key from Settings, then selected post steps.
 */
export function buildManualInitClipboardScript(
  apiKey: string,
  host: string,
  flags: ManualBrazeInitFlags,
  post: ManualBrazeInitPostSteps,
): string {
  const lines: string[] = [];

  if (flags.destroyFirst) {
    lines.push("if (braze.isInitialized?.()) {");
    lines.push("  braze.destroy();");
    lines.push("}");
  }

  const initOpts = buildManualInitializationOptions(host, flags);
  lines.push(
    `braze.initialize(${JSON.stringify(apiKey)}, ${JSON.stringify(initOpts)});`,
  );

  if (post.setLogger) {
    lines.push(
      `braze.setLogger((m) => console.debug("[braze]", m));`,
    );
  }

  if (post.subscribeBannerAndContentCards) {
    lines.push(
      `braze.subscribeToBannersUpdates?.(() => { window.dispatchEvent(new CustomEvent("braze:banners")); });`,
    );
    lines.push(
      `braze.subscribeToContentCardsUpdates?.((updates) => { window.dispatchEvent(new CustomEvent("braze:content-cards", { detail: updates })); });`,
    );
  }

  if (post.subscribeInAppMessage) {
    lines.push(
      `braze.subscribeToInAppMessage?.((message) => { braze.showInAppMessage?.(message); });`,
    );
  }

  if (post.requestBannersRefresh) {
    lines.push(
      `braze.requestBannersRefresh(${JSON.stringify(ALL_BANNER_PLACEMENT_IDS)});`,
    );
  }

  if (post.requestContentCardsRefresh) {
    lines.push(
      `braze.requestContentCardsRefresh?.(() => { const cc = braze.getCachedContentCards?.(); if (cc) window.dispatchEvent(new CustomEvent("braze:content-cards", { detail: cc })); });`,
    );
  }

  return wrapBrazeForConsolePaste(lines);
}
