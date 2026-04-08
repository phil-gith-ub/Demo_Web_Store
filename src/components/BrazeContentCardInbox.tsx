import type { Card, ContentCards } from "@braze/web-sdk";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useProfile } from "../context/ProfileContext";
import { brazeAppLog } from "../lib/brazeAppLog";
import {
  attachContentCardImpressionObserver,
  contentCardLayoutKind,
  filterOutControlContentCards,
  isRenderableUserFacingContentCard,
  sortContentCardsForDisplay,
  stableContentCardId,
} from "../lib/brazeContentCards";
import { demostoreUriToWebPath } from "../lib/demostoreDeepLink";

function readCardTitle(card: Card): string {
  if ("title" in card && typeof (card as { title?: string }).title === "string") {
    const t = (card as { title: string }).title.trim();
    if (t) return t;
  }
  return "Content card";
}

function readCardDescription(card: Card): string {
  if ("description" in card && typeof (card as { description?: string }).description === "string") {
    return (card as { description: string }).description;
  }
  return "";
}

function readCardImageUrl(card: Card): string | undefined {
  if ("imageUrl" in card && typeof (card as { imageUrl?: string }).imageUrl === "string") {
    const u = (card as { imageUrl: string }).imageUrl.trim();
    return u || undefined;
  }
  return undefined;
}

function readCardUrl(card: Card): string | undefined {
  if ("url" in card && typeof (card as { url?: string }).url === "string") {
    const u = (card as { url: string }).url.trim();
    return u || undefined;
  }
  return undefined;
}

function readDismissible(card: Card): boolean {
  if ("dismissible" in card) return !!(card as { dismissible?: boolean }).dismissible;
  return false;
}

type BrazeModule = typeof import("@braze/web-sdk");

function syncCardsFromSdk(braze: BrazeModule): Card[] {
  const cc = braze.getCachedContentCards();
  if (!cc?.cards?.length) return [];
  return sortContentCardsForDisplay(cc.cards.filter(isRenderableUserFacingContentCard));
}

/**
 * Custom Content Cards inbox: renders `ContentCards.cards` (skips `isControl`), unread badge via
 * `getUnviewedCardCount()`, impressions when the card is actually visible (viewport ≥0.5, active tab,
 * paint/CSS visibility, optional IO v2 `isVisible`) + `logContentCardImpressions`,
 * clicks via `logContentCardClick`, dismissal via `logCardDismissal` (custom UI; `dismissCard()` is ineffective
 * per SDK) plus optional `card.dismissCard()` for API parity with Braze-managed feeds.
 */
export function BrazeContentCardInbox() {
  const navigate = useNavigate();
  const { currentUserId, refreshKey } = useProfile();
  const [cards, setCards] = useState<Card[]>([]);
  const [unviewed, setUnviewed] = useState(0);
  const [dismissedExtra, setDismissedExtra] = useState<Set<string>>(() => new Set());
  const impressionLogged = useRef<Set<string>>(new Set());
  const rowRefs = useRef<Map<string, HTMLElement | null>>(new Map());

  const refreshFromSdk = useCallback(async () => {
    const braze = await import("@braze/web-sdk");
    if (!braze.isInitialized?.()) {
      setCards([]);
      setUnviewed(0);
      return;
    }
    setCards(syncCardsFromSdk(braze));
    const cc = braze.getCachedContentCards();
    setUnviewed(cc?.getUnviewedCardCount() ?? 0);
  }, []);

  /* Logout / different user: drop Braze card objects, dismiss/impression state, and resync (card ids can repeat across users). */
  useEffect(() => {
    impressionLogged.current = new Set();
    setDismissedExtra(new Set());
    if (!currentUserId) {
      setCards([]);
      setUnviewed(0);
      return;
    }
    void refreshFromSdk();
  }, [currentUserId, refreshKey, refreshFromSdk]);

  useEffect(() => {
    void refreshFromSdk();

    const onContentCards = (ev: Event) => {
      const detail = (ev as CustomEvent<ContentCards>).detail;
      try {
        const list = detail != null ? (detail as { cards?: unknown }).cards : undefined;
        if (detail != null && Array.isArray(list)) {
          setCards(
            sortContentCardsForDisplay(list.filter(isRenderableUserFacingContentCard)),
          );
          const uv =
            typeof (detail as ContentCards).getUnviewedCardCount === "function"
              ? (detail as ContentCards).getUnviewedCardCount()
              : 0;
          setUnviewed(uv);
          return;
        }
      } catch {
        /* ignore malformed payloads */
      }
      void refreshFromSdk();
    };

    /* Re-read cache when Braze finishes init (listener may have missed the first content-cards event). */
    const onIdentifiedReady = () => {
      void refreshFromSdk();
    };

    window.addEventListener("braze:content-cards", onContentCards);
    window.addEventListener("braze:identified-ready", onIdentifiedReady);
    return () => {
      window.removeEventListener("braze:content-cards", onContentCards);
      window.removeEventListener("braze:identified-ready", onIdentifiedReady);
    };
  }, [refreshFromSdk]);

  useEffect(() => {
    const cleanups: Array<() => void> = [];
    for (const card of cards) {
      const clickSub = card.subscribeToClickedEvent(() => {
        brazeAppLog({
          type: "event",
          message: `Content Card subscribeToClickedEvent id=${card.id ?? "(none)"}`,
        });
      });
      const dismissSub = card.subscribeToDismissedEvent(() => {
        brazeAppLog({
          type: "event",
          message: `Content Card subscribeToDismissedEvent id=${card.id ?? "(none)"}`,
        });
      });
      if (clickSub) cleanups.push(() => card.removeSubscription(clickSub));
      if (dismissSub) cleanups.push(() => card.removeSubscription(dismissSub));
    }
    return () => cleanups.forEach((fn) => fn());
  }, [cards]);

  const visibleCards = useMemo(
    () => cards.filter((c) => !dismissedExtra.has(stableContentCardId(c))),
    [cards, dismissedExtra],
  );

  useEffect(() => {
    const disconnectors: Array<() => void> = [];
    void import("@braze/web-sdk").then((braze) => {
      if (!braze.isInitialized?.()) return;
      for (const card of visibleCards) {
        const el = rowRefs.current.get(stableContentCardId(card));
        if (!el) continue;
        disconnectors.push(
          attachContentCardImpressionObserver(el, card, {
            loggedIds: impressionLogged.current,
            threshold: 0.5,
            onImpression: (toLog) => {
              if (braze.isInitialized?.()) braze.logContentCardImpressions(toLog);
            },
          }),
        );
      }
    });
    return () => disconnectors.forEach((d) => d());
  }, [visibleCards]);

  const onManualRefresh = () => {
    void import("@braze/web-sdk").then((braze) => {
      if (braze.isInitialized?.()) braze.requestContentCardsRefresh();
    });
  };

  const openUrl = (card: Card, url: string) => {
    void import("@braze/web-sdk").then((braze) => {
      if (braze.isInitialized?.()) braze.logContentCardClick(card);
    });
    const webPath = demostoreUriToWebPath(url);
    if (webPath) {
      navigate(webPath);
      return;
    }
    window.open(url, "_blank", "noopener,noreferrer");
  };

  const dismissCard = (card: Card) => {
    const id = stableContentCardId(card);
    void import("@braze/web-sdk").then((braze) => {
      if (!braze.isInitialized?.()) return;
      braze.logCardDismissal(card);
      card.dismissCard();
    });
    setDismissedExtra((prev) => new Set(prev).add(id));
  };

  return (
    <section className="braze-cc-inbox stack" aria-labelledby="braze-cc-inbox-heading">
      <div className="braze-cc-inbox-toolbar">
        <h2 id="braze-cc-inbox-heading" className="content-section-title braze-cc-inbox-title">
          Content card inbox
          {unviewed > 0 ? (
            <span className="badge braze-cc-inbox-badge" aria-label={`${unviewed} unviewed`}>
              {unviewed}
            </span>
          ) : null}
        </h2>
        <button type="button" className="btn btn-small" onClick={onManualRefresh}>
          Refresh cards
        </button>
      </div>
      <p className="braze-cc-inbox-hint">
        Unread count uses <code>getUnviewedCardCount()</code> (control cards excluded by the SDK). Rows skip{" "}
        <code>isControl</code>.
      </p>
      {visibleCards.length === 0 ? (
        <p className="muted">No content cards in cache. Use Refresh or wait for a campaign.</p>
      ) : (
        <ul className="braze-cc-inbox-list">
          {visibleCards.map((card) => {
            const id = stableContentCardId(card);
            const kind = contentCardLayoutKind(card);
            const title = readCardTitle(card);
            const desc = readCardDescription(card);
            const img = readCardImageUrl(card);
            const url = readCardUrl(card);
            const dismissible = readDismissible(card);
            return (
              <li
                key={id}
                ref={(el) => {
                  if (el) rowRefs.current.set(id, el);
                  else rowRefs.current.delete(id);
                }}
                className={[
                  "braze-cc-inbox-row",
                  card.pinned ? "braze-cc-inbox-row--pinned" : "",
                ]
                  .filter(Boolean)
                  .join(" ")}
                data-braze-card-id={id}
              >
                <div className="braze-cc-inbox-row-inner">
                  {img ? (
                    <div className="braze-cc-inbox-media">
                      <img src={img} alt="" loading="lazy" />
                    </div>
                  ) : null}
                  <div className="braze-cc-inbox-body">
                    <span className="braze-cc-inbox-kind">{kind}</span>
                    {kind !== "imageOnly" ? (
                      <>
                        <span className="braze-cc-inbox-card-title">{title}</span>
                        {desc ? <p className="braze-cc-inbox-desc">{desc}</p> : null}
                      </>
                    ) : null}
                    <div className="braze-cc-inbox-meta">
                      {card.viewed ? (
                        <span className="braze-cc-inbox-viewed">Viewed</span>
                      ) : (
                        <span className="braze-cc-inbox-unviewed">Unviewed</span>
                      )}
                      {card.pinned ? <span className="braze-cc-inbox-pinned">Pinned</span> : null}
                      {!dismissible ? (
                        <span className="braze-cc-inbox-locked">Not dismissible</span>
                      ) : null}
                    </div>
                    <div className="braze-cc-inbox-actions">
                      {url ? (
                        <button
                          type="button"
                          className="btn btn-small btn-primary"
                          onClick={() => openUrl(card, url)}
                        >
                          Open link
                        </button>
                      ) : null}
                      {dismissible ? (
                        <button
                          type="button"
                          className="btn btn-small"
                          onClick={() => dismissCard(card)}
                        >
                          Dismiss
                        </button>
                      ) : null}
                    </div>
                  </div>
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
}

type DefaultFeedProps = {
  hostClassName?: string;
};

/**
 * Embeds Braze default Content Cards UI via `showContentCards` / `hideContentCards` / `toggleContentCards`.
 * Styling hooks: <code>.ab-feed</code> under <code>.braze-cc-feed-host</code>.
 */
export function BrazeDefaultContentCardsFeed({ hostClassName }: DefaultFeedProps) {
  const hostRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    return () => {
      const el = hostRef.current;
      void import("@braze/web-sdk").then((braze) => {
        if (braze.isInitialized?.() && el) braze.hideContentCards(el);
      });
    };
  }, []);

  const run = (fn: "show" | "hide" | "toggle") => {
    const el = hostRef.current;
    void import("@braze/web-sdk").then((braze) => {
      if (!braze.isInitialized?.()) return;
      const filter = filterOutControlContentCards;
      if (fn === "show") braze.showContentCards(el ?? undefined, filter);
      else if (fn === "hide") braze.hideContentCards(el ?? undefined);
      else braze.toggleContentCards(el ?? undefined, filter);
    });
  };

  return (
    <section className="stack" aria-labelledby="braze-default-cc-heading">
      <h2 id="braze-default-cc-heading" className="content-section-title">
        Default Braze Content Cards UI
      </h2>
      <p className="braze-cc-inbox-hint">
        Uses <code>showContentCards(parent, filter)</code>, <code>hideContentCards(parent)</code>,{" "}
        <code>toggleContentCards(parent, filter)</code>. Filter removes <code>isControl</code> cards.
      </p>
      <div className="braze-cc-default-toolbar">
        <button type="button" className="btn btn-small btn-primary" onClick={() => run("show")}>
          showContentCards
        </button>
        <button type="button" className="btn btn-small" onClick={() => run("hide")}>
          hideContentCards
        </button>
        <button type="button" className="btn btn-small" onClick={() => run("toggle")}>
          toggleContentCards
        </button>
      </div>
      <div
        ref={hostRef}
        className={["braze-cc-feed-host", hostClassName].filter(Boolean).join(" ")}
      />
    </section>
  );
}
