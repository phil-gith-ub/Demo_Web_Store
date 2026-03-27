import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  DEMOSTORE_DOM_SCAN_EVENT,
  demostoreUriToWebPath,
} from "../lib/demostoreDeepLink";

/** Mark anchors we patched so MutationObserver does not stack listeners */
function patchDemostoreAnchors(root: ParentNode, navigate: (to: string) => void) {
  const list = root.querySelectorAll?.(
    'a[href^="demostore:"], a[href^="DEMOSTORE:"]',
  );
  list?.forEach((node) => {
    const a = node;
    if (!(a instanceof HTMLAnchorElement)) return;
    if (a.dataset.philDemostorePatched === "1") return;
    const href = a.getAttribute("href");
    if (!href) return;
    const path = demostoreUriToWebPath(href);
    if (!path) return;
    a.dataset.philDemostorePatched = "1";
    a.addEventListener(
      "click",
      (e) => {
        e.preventDefault();
        e.stopPropagation();
        navigate(path);
      },
      true,
    );
  });
}

/**
 * Web cannot open `demostore://` like the Android app. Intercepts clicks and maps to in-app routes.
 * Also patches anchors Braze injects (IAM HTML) via MutationObserver + post-IAM scan.
 */
export function DemostoreLinkInterceptor() {
  const navigate = useNavigate();

  useEffect(() => {
    const tryNavigate = (href: string): boolean => {
      const path = demostoreUriToWebPath(href);
      if (!path) return false;
      navigate(path);
      return true;
    };

    const onClickCapture = (e: MouseEvent) => {
      if (e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
        return;
      }

      let anchor: HTMLAnchorElement | null = null;
      const path = typeof e.composedPath === "function" ? e.composedPath() : [];
      for (const t of path) {
        if (t instanceof HTMLAnchorElement) {
          const href = t.getAttribute("href");
          if (href && /^demostore:/i.test(href)) {
            anchor = t;
            break;
          }
        }
      }
      if (!anchor) {
        const x = (e.target as Element | null)?.closest?.("a[href]");
        const href = x?.getAttribute("href");
        if (x instanceof HTMLAnchorElement && href && /^demostore:/i.test(href)) {
          anchor = x;
        }
      }
      if (!anchor) return;

      const href = anchor.getAttribute("href");
      if (!href) return;
      if (!tryNavigate(href)) return;

      e.preventDefault();
      e.stopPropagation();
    };

    window.addEventListener("click", onClickCapture, true);

    const runPatch = () => patchDemostoreAnchors(document.body, navigate);

    const onScan = () => {
      runPatch();
    };
    window.addEventListener(DEMOSTORE_DOM_SCAN_EVENT, onScan);

    let t: number | undefined;
    const mo = new MutationObserver(() => {
      if (t != null) window.clearTimeout(t);
      t = window.setTimeout(() => {
        t = undefined;
        runPatch();
      }, 50);
    });
    mo.observe(document.body, { childList: true, subtree: true });
    runPatch();

    return () => {
      window.removeEventListener("click", onClickCapture, true);
      window.removeEventListener(DEMOSTORE_DOM_SCAN_EVENT, onScan);
      mo.disconnect();
      if (t != null) window.clearTimeout(t);
    };
  }, [navigate]);

  return null;
}
