import { DEEPLINK_REFERENCE } from "./brazeConstants";

/** Fired after Braze shows an IAM so we can patch `demostore:` links in injected HTML. */
export const DEMOSTORE_DOM_SCAN_EVENT = "phil-wed-store-scan-demostore";

/**
 * Parse `demostore://host` (Android-style) and return the SPA path for web.
 */
export function demostoreUriToWebPath(href: string): string | null {
  const trimmed = href.trim();
  const m = trimmed.match(/^demostore:\/\/([^/?#]+)/i);
  if (!m) return null;
  const host = m[1].toLowerCase().replace(/%2d/gi, "-");

  const ref = DEEPLINK_REFERENCE.find((r) => {
    const rm = r.appUri.match(/^demostore:\/\/([^/?#]+)/i);
    return rm && rm[1].toLowerCase() === host;
  });
  if (ref) return ref.webPath;

  if (host === "login") return "/profile";

  return null;
}
