import type { Card, ContentCards } from "@braze/web-sdk";
import { useCallback, useEffect, useRef, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useProfile } from "../context/ProfileContext";
import {
  attachContentCardImpressionObserver,
  isRenderableUserFacingContentCard,
  sortContentCardsForDisplay,
  stableContentCardId,
} from "../lib/brazeContentCards";
import { demostoreUriToWebPath } from "../lib/demostoreDeepLink";

type Props = {
  isOpen: boolean;
  onClose: () => void;
};

function readCardTitle(card: Card): string {
  if ("title" in card && typeof (card as { title?: string }).title === "string") {
    const t = (card as { title: string }).title.trim();
    if (t) return t;
  }
  return "Notification";
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

export function BrazeNotificationPanel({ isOpen, onClose }: Props) {
  const navigate = useNavigate();
  const { currentUserId, refreshKey } = useProfile();
  const [cards, setCards] = useState<Card[]>([]);
  const [dismissedLocal, setDismissedLocal] = useState<Set<string>>(() => new Set());
  const impressionLogged = useRef<Set<string>>(new Set());
  const rowRefs = useRef<Map<string, HTMLElement | null>>(new Map());

  const refreshFromSdk = useCallback(async () => {
    const braze = await import("@braze/web-sdk");
    if (!braze.isInitialized?.()) {
      setCards([]);
      return;
    }
    const cc = braze.getCachedContentCards();
    if (cc?.cards) {
      setCards(sortContentCardsForDisplay(cc.cards.filter(isRenderableUserFacingContentCard)));
    }
  }, []);

  useEffect(() => {
    impressionLogged.current = new Set();
    setDismissedLocal(new Set());
    if (!currentUserId) {
      setCards([]);
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
          setCards(sortContentCardsForDisplay(list.filter(isRenderableUserFacingContentCard)));
          return;
        }
      } catch {
        /* ignore */
      }
      void refreshFromSdk();
    };

    window.addEventListener("braze:content-cards", onContentCards);
    window.addEventListener("braze:identified-ready", refreshFromSdk);
    return () => {
      window.removeEventListener("braze:content-cards", onContentCards);
      window.removeEventListener("braze:identified-ready", refreshFromSdk);
    };
  }, [refreshFromSdk]);

  const visibleCards = useMemo(
    () => cards.filter((c) => !dismissedLocal.has(stableContentCardId(c))),
    [cards, dismissedLocal]
  );

  // Track impressions when cards are visible inside the open panel
  useEffect(() => {
    if (!isOpen || visibleCards.length === 0) return;

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
          })
        );
      }
    });
    return () => disconnectors.forEach((d) => d());
  }, [visibleCards, isOpen]);

  const onManualRefresh = () => {
    void import("@braze/web-sdk").then((braze) => {
      if (braze.isInitialized?.()) {
        braze.requestContentCardsRefresh();
      }
    });
  };

  const handleCardClick = (card: Card, url: string) => {
    void import("@braze/web-sdk").then((braze) => {
      if (braze.isInitialized?.()) braze.logContentCardClick(card);
    });
    const webPath = demostoreUriToWebPath(url);
    if (webPath) {
      navigate(webPath);
    } else {
      window.open(url, "_blank", "noopener,noreferrer");
    }
    onClose();
  };

  const handleCardDismiss = (card: Card) => {
    const id = stableContentCardId(card);
    void import("@braze/web-sdk").then((braze) => {
      if (!braze.isInitialized?.()) return;
      braze.logCardDismissal(card);
      card.dismissCard();
    });
    setDismissedLocal((prev) => new Set(prev).add(id));
  };

  if (!isOpen) return null;

  return (
    <>
      <div className="notification-panel-overlay" onClick={onClose} />
      <aside className="notification-panel">
        <header className="notification-panel-header">
          <div className="notification-panel-title-area">
            <h2>Notifications</h2>
            <button
              type="button"
              className="refresh-icon-btn"
              onClick={onManualRefresh}
              title="Refresh notifications"
              aria-label="Refresh notifications"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" className="refresh-svg">
                <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l.73-.7" />
              </svg>
            </button>
          </div>
          <button
            type="button"
            className="close-panel-btn"
            onClick={onClose}
            aria-label="Close panel"
          >
            ✕
          </button>
        </header>

        <div className="notification-panel-body">
          {visibleCards.length === 0 ? (
            <div className="notification-panel-empty">
              <p className="muted">No new notifications.</p>
            </div>
          ) : (
            <ul className="notification-panel-list">
              {visibleCards.map((card) => {
                const id = stableContentCardId(card);
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
                    className="notification-panel-item"
                  >
                    {img ? (
                      <div className="notification-panel-item-media">
                        <img src={img} alt="" loading="lazy" />
                      </div>
                    ) : null}
                    <div className="notification-panel-item-content">
                      <span className="notification-panel-item-title">{title}</span>
                      {desc ? <p className="notification-panel-item-desc">{desc}</p> : null}
                      
                      <div className="notification-panel-item-footer">
                        {url ? (
                          <button
                            type="button"
                            className="btn btn-small btn-primary"
                            onClick={() => handleCardClick(card, url)}
                          >
                            Open Link
                          </button>
                        ) : null}
                      </div>
                    </div>
                    {dismissible ? (
                      <button
                        type="button"
                        className="notification-card-dismiss-btn"
                        onClick={() => handleCardDismiss(card)}
                        aria-label="Dismiss notification"
                        title="Dismiss"
                      >
                        ✕
                      </button>
                    ) : null}
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </aside>
    </>
  );
}
