/**
 * Reference IDs and routes for this demo store (Braze placements, cards, deep links).
 */

/** Keep in sync with `package.json` → `dependencies["@braze/web-sdk"]`. */
export const BRAZE_WEB_SDK_RANGE = "^6.3.1";

export const BANNER_PLACEMENTS = [
  { label: "Store page", id: "store_page_banner" },
  { label: "Cart", id: "cart_banner" },
  { label: "Content (hero)", id: "content_banner" },
  { label: "Content (tile)", id: "tile_banner" },
] as const;

/** Pass to `requestBannersRefresh` (same placements as Android `BrazeContentManager`). */
export const ALL_BANNER_PLACEMENT_IDS: string[] = BANNER_PLACEMENTS.map(
  (b) => b.id,
);

/** Target card extras: `location`, `position_id`, or `card_id` (per Braze card KVP). */
export const CONTENT_CARD_SLOTS = [
  {
    label: "Tile 1",
    id: "tile_1",
    hint: "Match extras: location or position_id / card_id for tile_1",
  },
  {
    label: "Tile 2",
    id: "tile_2",
    hint: "Match extras: location or position_id / card_id for tile_2",
  },
] as const;

/** Custom scheme for mobile-style links; web uses `webPath` on the same host. */
export const DEEPLINK_REFERENCE = [
  { label: "Store", appUri: "demostore://store", webPath: "/store" },
  { label: "Cart", appUri: "demostore://cart", webPath: "/cart" },
  { label: "Profile / login", appUri: "demostore://profile", webPath: "/profile" },
  { label: "VIP products tab", appUri: "demostore://vip", webPath: "/store?vip=1" },
  {
    label: "Purchase history",
    appUri: "demostore://purchase-history",
    webPath: "/purchase-history",
  },
  { label: "Purchase history (alt)", appUri: "demostore://history", webPath: "/purchase-history" },
] as const;

export const BRAZE_FEATURE_FLAG_VIP = "enable_vip_products";
