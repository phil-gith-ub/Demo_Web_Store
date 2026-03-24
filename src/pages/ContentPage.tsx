import { BrazeGhostSlot } from "../components/BrazeGhostSlot";
import {
  BANNER_PLACEMENTS,
  CONTENT_CARD_SLOTS,
} from "../lib/brazeConstants";

export function ContentPage() {
  const contentBanners = BANNER_PLACEMENTS.filter(
    (b) => b.id === "content_banner" || b.id === "tile_banner",
  );

  return (
    <>
      <h1 className="page-title">Content</h1>
      <p className="product-meta content-lead">
        Placeholders for Braze banners and content cards. When campaigns use the
        same placement IDs and card extras, creative will render here.
      </p>

      <h2 className="content-section-title">Banners</h2>
      <div className="content-ghost-grid">
        {contentBanners.map((b) => (
          <BrazeGhostSlot key={b.id} title={b.label} placementId={b.id} />
        ))}
      </div>

      <h2 className="content-section-title">Content cards</h2>
      <div className="content-ghost-grid">
        {CONTENT_CARD_SLOTS.map((c) => (
          <BrazeGhostSlot
            key={c.id}
            title={c.label}
            placementId={c.id}
            hint={c.hint}
          />
        ))}
      </div>
    </>
  );
}
