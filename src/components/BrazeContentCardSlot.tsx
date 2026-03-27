import type { Card } from "@braze/web-sdk";
import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { demostoreUriToWebPath } from "../lib/demostoreDeepLink";
import { findContentCardForSlot } from "../lib/brazeUserSyncWeb";
import { BrazeGhostSlot } from "./BrazeGhostSlot";

export type BrazeContentCardVariant = "wide" | "square";

type Props = {
  title: string;
  slotId: string;
  hint?: string;
  /** `wide` = tile_1 (2:1, image left / text right). `square` = tile_2 (1:1). */
  variant: BrazeContentCardVariant;
};

function cardTitle(card: Card, fallback: string): string {
  if ("title" in card && typeof card.title === "string" && card.title.trim()) {
    return card.title;
  }
  return fallback;
}

function cardDescription(card: Card): string {
  if ("description" in card && typeof card.description === "string") {
    return card.description;
  }
  return "";
}

function cardImageUrl(card: Card): string | undefined {
  if ("imageUrl" in card && typeof card.imageUrl === "string" && card.imageUrl) {
    return card.imageUrl;
  }
  return undefined;
}

function cardUrl(card: Card): string | undefined {
  if ("url" in card && typeof card.url === "string" && card.url) {
    return card.url;
  }
  return undefined;
}

/**
 * Picks a card when extras `location` equals `slotId`; logs impressions/clicks.
 */
export function BrazeContentCardSlot({ title, slotId, hint, variant }: Props) {
  const navigate = useNavigate();
  const [card, setCard] = useState<Card | null>(null);
  const impressionLogged = useRef<string | undefined>(undefined);

  const sync = useCallback(async () => {
    const braze = await import("@braze/web-sdk");
    if (!braze.isInitialized?.()) {
      setCard(null);
      return;
    }
    const cc = braze.getCachedContentCards();
    const found = cc ? findContentCardForSlot(cc.cards, slotId) : null;
    setCard(found);
  }, [slotId]);

  useEffect(() => {
    void sync();
    const onCc = () => void sync();
    window.addEventListener("braze:content-cards", onCc);
    return () => window.removeEventListener("braze:content-cards", onCc);
  }, [sync]);

  useEffect(() => {
    if (!card) return;
    const stableId = card.id ?? card.extras?.card_id ?? slotId;
    if (impressionLogged.current === stableId) return;
    impressionLogged.current = stableId;
    void import("@braze/web-sdk").then((braze) => {
      if (braze.isInitialized?.()) braze.logContentCardImpressions([card]);
    });
  }, [card, slotId]);

  const ghostClass =
    variant === "wide"
      ? "ghost-slot--fill-card-21"
      : "ghost-slot--fill-card-square";

  if (!card) {
    return (
      <BrazeGhostSlot
        title={title}
        placementId={slotId}
        hint={hint}
        className={ghostClass}
      />
    );
  }

  const img = cardImageUrl(card);
  const ttl = cardTitle(card, title);
  const desc = cardDescription(card);
  const url = cardUrl(card);

  const onActivate = () => {
    void import("@braze/web-sdk").then((braze) => {
      if (braze.isInitialized?.()) braze.logContentCardClick(card);
    });
    if (!url) return;
    const webPath = demostoreUriToWebPath(url);
    if (webPath) {
      navigate(webPath);
      return;
    }
    window.open(url, "_blank", "noopener,noreferrer");
  };

  const wideWithImage = variant === "wide" && !!img;

  return (
    <article
      className={[
        "content-card-slot",
        variant === "wide" && "content-card-slot--wide",
        variant === "square" && "content-card-slot--square",
        wideWithImage && "content-card-slot--wide-row",
        url ? "content-card-slot--clickable" : "",
      ]
        .filter(Boolean)
        .join(" ")}
      data-slot-id={slotId}
      role={url ? "link" : undefined}
      tabIndex={url ? 0 : undefined}
      onClick={url ? onActivate : undefined}
      onKeyDown={
        url
          ? (e) => {
              if (e.key === "Enter" || e.key === " ") {
                e.preventDefault();
                onActivate();
              }
            }
          : undefined
      }
    >
      {img ? (
        <div className="content-card-slot-media">
          <img src={img} alt="" loading="lazy" />
        </div>
      ) : null}
      <div className="content-card-slot-body">
        <span className="content-card-slot-title">{ttl}</span>
        {desc ? <p className="content-card-slot-desc">{desc}</p> : null}
        {url ? (
          <span className="content-card-slot-cta">Open link</span>
        ) : (
          <code className="ghost-slot-code content-card-slot-code">{slotId}</code>
        )}
      </div>
    </article>
  );
}
