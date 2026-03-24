import { BrazeBannerSlot } from "../components/BrazeBannerSlot";
import { BrazeContentCardSlot } from "../components/BrazeContentCardSlot";
import {
  BANNER_PLACEMENTS,
  CONTENT_CARD_SLOTS,
} from "../lib/brazeConstants";

/* Banners: placement IDs. Content cards: match extras position_id / location / card_id (e.g. tile_1, tile_2). */

export function ContentPage() {
  const contentBanners = BANNER_PLACEMENTS.filter(
    (b) => b.id === "content_banner" || b.id === "tile_banner",
  );

  return (
    <div className="content-page">
      <h1 className="page-title">Content</h1>

      <h2 className="content-section-title">Banners</h2>
      <div className="content-ghost-grid content-ghost-grid--paired">
        {contentBanners.map((b) => (
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

      <h2 className="content-section-title">Content cards</h2>
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
            />
          </div>
        ))}
      </div>
    </div>
  );
}
