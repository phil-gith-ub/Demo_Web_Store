import { useEffect, useState } from "react";
import { useProfile } from "../context/ProfileContext";
import { BrazeBannerSlot } from "../components/BrazeBannerSlot";
import {
  BrazeContentCardInbox,
  BrazeDefaultContentCardsFeed,
} from "../components/BrazeContentCardInbox";
import { BrazeContentCardSlot } from "../components/BrazeContentCardSlot";
import { ContentCustomEventDialog } from "../components/ContentCustomEventDialog";
import {
  BANNER_PLACEMENTS,
  CONTENT_CARD_SLOTS,
} from "../lib/brazeConstants";
import { logBrazeCustomEvent } from "../lib/brazeUserSyncWeb";

const contentBannerPlacements = BANNER_PLACEMENTS.filter(
  (b) => b.id === "content_banner" || b.id === "tile_banner",
);

/**
 * Demo actions at top; Banners; placement Content card slots; then full inbox + default Braze feed at bottom.
 */
export function ContentPage() {
  const { currentUserId, refreshKey } = useProfile();
  const [toast, setToast] = useState<string | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);

  /* Re-run on login/logout/user switch (`refreshKey`): first paint may have an empty cache before the new user’s feed returns. */
  useEffect(() => {
    if (!currentUserId) return;
    void import("@braze/web-sdk").then((b) => {
      if (!b.isInitialized?.()) return;
      const cc = b.getCachedContentCards();
      if (!cc?.cards?.length) b.requestContentCardsRefresh();
    });
  }, [currentUserId, refreshKey]);

  useEffect(() => {
    if (!toast) return;
    const t = window.setTimeout(() => setToast(null), 2000);
    return () => window.clearTimeout(t);
  }, [toast]);

  const fireDemoEvent = async (eventName: string) => {
    const ok = await logBrazeCustomEvent(eventName);
    setToast(
      ok ? "Custom Event Sent" : "Braze not ready — log in with a user ID first",
    );
  };

  return (
    <div className="content-page">
      <h1 className="page-title">Content</h1>

      <div className="content-action-row">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => void fireDemoEvent("enable_push")}
        >
          Enable Push
        </button>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => void fireDemoEvent("push_notification")}
        >
          Send Push
        </button>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setDialogOpen(true)}
        >
          User Action
        </button>
      </div>

      {toast ? (
        <div className="content-page-toast" role="status">
          {toast}
        </div>
      ) : null}

      <h2 className="content-section-title">Banners</h2>
      <div className="content-ghost-grid content-ghost-grid--paired">
        {contentBannerPlacements.map((b) => (
          <div
            key={b.id}
            className={
              b.id === "content_banner" ? "content-grid-span-2" : undefined
            }
          >
            <BrazeBannerSlot
              title={b.label}
              placementId={b.id}
              variant={b.id === "content_banner" ? "wide" : "square"}
            />
          </div>
        ))}
      </div>

      <h2 className="content-section-title">Content cards (placement slots)</h2>
      <div className="content-ghost-grid content-ghost-grid--paired">
        {CONTENT_CARD_SLOTS.map((c) => (
          <div
            key={c.id}
            className={
              c.id === "tile_1" ? "content-grid-span-2" : undefined
            }
          >
            <BrazeContentCardSlot
              title={c.label}
              slotId={c.id}
              variant={c.id === "tile_1" ? "wide" : "square"}
              hint={c.hint}
            />
          </div>
        ))}
      </div>

      <BrazeContentCardInbox />
      <BrazeDefaultContentCardsFeed />

      <ContentCustomEventDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onSent={(ok) => {
          setToast(
            ok
              ? "Custom Event Sent"
              : "Braze not ready — log in with a user ID first",
          );
          setDialogOpen(false);
        }}
      />
    </div>
  );
}
