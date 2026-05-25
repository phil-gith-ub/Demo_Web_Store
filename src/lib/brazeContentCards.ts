import type { Card } from "@braze/web-sdk";

/** Stable key for impression de-duplication and local dismiss state. */
export function stableContentCardId(card: Card): string {
  if (card.id) return card.id;
  const ex = card.extras?.card_id ?? card.extras?.id;
  if (ex) return String(ex);
  return `anon:${card.updated?.toISOString() ?? "na"}:${card.pinned ? "p" : "n"}`;
}

/** User-facing feed rows: hide control variants and locally / SDK-dismissed cards. */
export function isRenderableUserFacingContentCard(card: Card): boolean {
  if (card.isControl) return false;
  if ("dismissed" in card && (card as { dismissed?: boolean }).dismissed === true) {
    return false;
  }
  return true;
}

export function sortContentCardsForDisplay(cards: Card[]): Card[] {
  return [...cards].sort((a, b) => {
    if (a.pinned !== b.pinned) return a.pinned ? -1 : 1;
    const ta = a.updated instanceof Date ? a.updated.getTime() : 0;
    const tb = b.updated instanceof Date ? b.updated.getTime() : 0;
    return tb - ta;
  });
}

/** Uses runtime constructor names from the Web SDK (ImageOnly, CaptionedImage, ClassicCard). */
export function contentCardLayoutKind(card: Card): "imageOnly" | "captioned" | "classic" {
  const n = card.constructor?.name ?? "";
  if (n === "ImageOnly") return "imageOnly";
  if (n === "CaptionedImage") return "captioned";
  return "classic";
}

/** Share of the element's bounding box that lies inside the viewport (0–1). */
function viewportIntersectionRatio(el: Element): number {
  const rect = el.getBoundingClientRect();
  const elArea = rect.width * rect.height;
  if (elArea <= 0) return 0;
  const vw = window.innerWidth;
  const vh = window.innerHeight;
  const ix = Math.max(0, Math.min(rect.right, vw) - Math.max(rect.left, 0));
  const iy = Math.max(0, Math.min(rect.bottom, vh) - Math.max(rect.top, 0));
  return (ix * iy) / elArea;
}

function isPaintedVisible(el: Element): boolean {
  const h = el as HTMLElement & { checkVisibility?: (opts?: object) => boolean };
  if (typeof h.checkVisibility === "function") {
    return h.checkVisibility({
      checkOpacity: true,
      checkVisibilityCSS: true,
      contentVisibilityAuto: true,
    });
  }
  return true;
}

/**
 * Logs an impression once per card id when the element is actually visible to the user:
 * intersects the viewport at or above `threshold` (default 0.5), document tab is visible,
 * element passes CSS/paint visibility when `checkVisibility` exists, and when supported
 * IntersectionObserver v2 reports `isVisible !== false`.
 */
export function attachContentCardImpressionObserver(
  element: Element,
  card: Card,
  options: {
    loggedIds: Set<string>;
    onImpression: (cards: Card[]) => void;
    threshold?: number;
  },
): () => void {
  const threshold = options.threshold ?? 0.5;
  const id = stableContentCardId(card);

  let io: IntersectionObserver | null = null;
  let done = false;

  const finish = () => {
    done = true;
    document.removeEventListener("visibilitychange", onVisibilityChange);
    io?.unobserve(element);
    io?.disconnect();
    io = null;
  };

  const tryLog = (entry: IntersectionObserverEntry | null) => {
    if (done || options.loggedIds.has(id)) return;
    if (document.visibilityState !== "visible") return;

    const ratio = entry?.isIntersecting
      ? entry.intersectionRatio
      : viewportIntersectionRatio(element);
    if (ratio < threshold) return;

    if (entry && "isVisible" in entry && entry.isVisible === false) return;
    if (!isPaintedVisible(element)) return;

    options.loggedIds.add(id);
    options.onImpression([card]);
    finish();
  };

  const onVisibilityChange = () => {
    if (document.visibilityState !== "visible") return;
    try {
      io?.takeRecords();
    } catch {
      /* ignore */
    }
    tryLog(null);
  };

  const observerCallback = (entries: IntersectionObserverEntry[]) => {
    for (const entry of entries) {
      if (!entry.isIntersecting || entry.target !== element) continue;
      if (entry.intersectionRatio < threshold) continue;
      tryLog(entry);
    }
  };

  const ioOpts: IntersectionObserverInit = { threshold: [threshold] };
  try {
    Object.assign(ioOpts, { trackVisibility: true, delay: 100 });
    io = new IntersectionObserver(observerCallback, ioOpts);
  } catch {
    io = new IntersectionObserver(observerCallback, { threshold: [threshold] });
  }

  document.addEventListener("visibilitychange", onVisibilityChange);
  io.observe(element);

  return () => {
    if (!done) {
      document.removeEventListener("visibilitychange", onVisibilityChange);
    }
    io?.disconnect();
    io = null;
  };
}

/** Default Braze Content Cards UI: drop control arms from the feed. */
export function filterOutControlContentCards(cards: Card[]): Card[] {
  return cards.filter((c) => !c.isControl);
}
