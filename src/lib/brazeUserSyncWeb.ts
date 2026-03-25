import type { Card } from "@braze/web-sdk";
import type { UserProfile } from "../context/ProfileContext";
import type { Product } from "../types/product";
import { BRAZE_FEATURE_FLAG_VIP, ALL_BANNER_PLACEMENT_IDS } from "./brazeConstants";
import { brazeAppLog } from "./brazeAppLog";
import {
  loadBrazeLastSent,
  mergeBrazeLastSent,
  type BrazeLastSentValues,
} from "./brazeSyncState";
import { isVip } from "./purchaseStorage";

/** Slug for Braze `logPurchase` product id (matches Android `BrazeUserSync.logPurchase`). */
export function slugProductIdFromName(name: string): string {
  return name
    .toLowerCase()
    .replaceAll(" ", "_")
    .replaceAll("-", "_")
    .replaceAll("'", "")
    .replaceAll(".", "")
    .replaceAll(",", "");
}

function addedToCartProps(product: Product): Record<string, string | number | boolean> {
  const p: Record<string, string | number | boolean> = {
    id: product.id,
    product_name: product.name,
    product_category: product.category,
    product_description: product.description,
    product_price: product.price,
  };
  if (product.isVip) p.is_vip = true;
  return p;
}

function purchaseProps(product: Product): Record<string, string | number | boolean> {
  const p: Record<string, string | number | boolean> = {
    id: product.id,
    product_name: product.name,
    product_category: product.category,
    product_description: product.description,
  };
  if (product.isVip) p.is_vip = true;
  return p;
}

export async function refreshBrazeBannersAndCards(): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;
  braze.requestBannersRefresh(ALL_BANNER_PLACEMENT_IDS);
  braze.requestContentCardsRefresh();
}

/**
 * After `changeUser`: active_member, logged_in, `vip_member` only if user qualifies (never false), feature flags.
 */
export async function completeIdentifiedUserAfterChangeUser(
  userId: string,
): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;

  const user = braze.getUser();
  user?.setCustomUserAttribute("active_member", true);
  mergeBrazeLastSent(userId, { activeMember: true });

  braze.logCustomEvent("logged_in");
  brazeAppLog({ type: "event", message: 'logCustomEvent("logged_in")' });

  await syncVipStatusToBraze(userId);

  braze.refreshFeatureFlags?.();
  braze.logFeatureFlagImpression?.(BRAZE_FEATURE_FLAG_VIP);

  braze.requestImmediateDataFlush();
  /* Banners/cards refresh runs in `initBrazeForIdentifiedUser` before `openSession`. */
}

export async function brazePreLogout(userId: string): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;

  const last = loadBrazeLastSent(userId);
  const user = braze.getUser();
  if (user && last.activeMember !== false) {
    user.setCustomUserAttribute("active_member", false);
    mergeBrazeLastSent(userId, { activeMember: false });
  }

  braze.logCustomEvent("logged_out");
  brazeAppLog({ type: "event", message: 'logCustomEvent("logged_out")' });
  braze.requestImmediateDataFlush();
  await new Promise((r) => setTimeout(r, 200));
}

export async function syncUserToBraze(profile: UserProfile): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) {
    brazeAppLog({
      type: "error",
      message: "syncUserToBraze skipped — Braze not initialized",
    });
    return;
  }

  const last = loadBrazeLastSent(profile.userId);
  let hasChanges = false;

  const currentFirstName = profile.firstName.trim() || null;
  const currentLastName = profile.lastName.trim() || null;
  const currentEmail = profile.email.trim() || null;
  const currentMobile = profile.mobile.trim() || null;
  const currentCategory = profile.favoriteProductCategory.trim() || null;

  const user = braze.getUser();
  if (!user) return;

  if (currentFirstName !== last.firstName) {
    user.setFirstName(currentFirstName ?? "");
    hasChanges = true;
  }
  if (currentLastName !== last.lastName) {
    user.setLastName(currentLastName ?? "");
    hasChanges = true;
  }
  if (currentEmail !== last.email) {
    user.setEmail(currentEmail ?? "");
    hasChanges = true;
  }
  if (currentMobile !== last.mobile) {
    user.setPhoneNumber(currentMobile ?? "");
    hasChanges = true;
  }
  if (currentCategory !== last.favoriteProductCategory) {
    user.setCustomUserAttribute(
      "favorite_product_category",
      currentCategory,
    );
    hasChanges = true;
  }
  if (profile.paidMembership !== last.paidMembership) {
    user.setCustomUserAttribute("paid_membership", profile.paidMembership);
    hasChanges = true;
  }

  if (!hasChanges) {
    brazeAppLog({ type: "info", message: "syncUserToBraze — no deltas" });
    return;
  }

  const next: BrazeLastSentValues = {
    firstName: currentFirstName,
    lastName: currentLastName,
    email: currentEmail,
    mobile: currentMobile,
    favoriteProductCategory: currentCategory,
    activeMember: last.activeMember,
    vipMember: last.vipMember,
    paidMembership:
      profile.paidMembership !== last.paidMembership
        ? profile.paidMembership
        : last.paidMembership,
  };
  mergeBrazeLastSent(profile.userId, next);
  braze.requestImmediateDataFlush();
  brazeAppLog({ type: "event", message: "syncUserToBraze — profile deltas flushed" });
}

/**
 * Sets `vip_member=true` in Braze once the user qualifies. Never sets `false` (VIP is permanent in Braze;
 * non-VIP / new profiles omit the attribute). Clears legacy local `vipMember: false` without sending.
 */
export async function syncVipStatusToBraze(userId: string): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;

  const vip = isVip(userId);
  const last = loadBrazeLastSent(userId);

  if (!vip) {
    if (last.vipMember === false) {
      mergeBrazeLastSent(userId, { vipMember: null });
    }
    return;
  }

  if (last.vipMember === true) return;

  const user = braze.getUser();
  user?.setCustomUserAttribute("vip_member", true);
  mergeBrazeLastSent(userId, { vipMember: true });
  brazeAppLog({
    type: "event",
    message: 'setCustomUserAttribute("vip_member", true)',
  });
}

export async function logAddedItemToCart(product: Product): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;
  braze.logCustomEvent("added_item_to_cart", addedToCartProps(product));
  brazeAppLog({
    type: "event",
    message: 'logCustomEvent("added_item_to_cart")',
    detail: product.name,
  });
  braze.requestImmediateDataFlush();
  await refreshBrazeBannersAndCards();
}

export async function logViewedVipProducts(): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;
  braze.logCustomEvent("viewed_vip_products");
  brazeAppLog({ type: "event", message: 'logCustomEvent("viewed_vip_products")' });
  braze.requestImmediateDataFlush();
  await refreshBrazeBannersAndCards();
}

export async function logEnabledDarkMode(): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;
  braze.logCustomEvent("enabled_dark_mode");
  brazeAppLog({ type: "event", message: 'logCustomEvent("enabled_dark_mode")' });
  braze.requestImmediateDataFlush();
  await refreshBrazeBannersAndCards();
}

/**
 * Web SDK: `logPurchase(productId, price, currencyCode, quantity, purchaseProperties)`.
 */
export async function logCheckoutPurchases(products: Product[]): Promise<void> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return;

  const quantities = new Map<string, { product: Product; qty: number }>();
  for (const p of products) {
    const cur = quantities.get(p.id);
    if (cur) cur.qty += 1;
    else quantities.set(p.id, { product: p, qty: 1 });
  }

  for (const { product, qty } of quantities.values()) {
    const productId = slugProductIdFromName(product.name);
    braze.logPurchase(
      productId,
      product.price,
      "USD",
      qty,
      purchaseProps(product),
    );
    brazeAppLog({
      type: "event",
      message: `logPurchase("${productId}", price=${product.price}, qty=${qty})`,
    });
  }
  braze.requestImmediateDataFlush();
  await refreshBrazeBannersAndCards();
}

/** Mirrors Android `isVipProductsEnabled` (impression + enabled). */
export async function isVipProductsEnabled(): Promise<boolean> {
  const braze = await import("@braze/web-sdk");
  if (!braze.isInitialized?.()) return true;
  braze.refreshFeatureFlags?.();
  braze.logFeatureFlagImpression?.(BRAZE_FEATURE_FLAG_VIP);
  const flag = braze.getFeatureFlag?.(BRAZE_FEATURE_FLAG_VIP);
  return flag?.enabled === true;
}

export function findContentCardForSlot(cards: Card[], slotId: string): Card | null {
  for (const card of cards) {
    if (card.isControl) continue;
    const ex = card.extras ?? {};
    if (
      ex.position_id === slotId ||
      ex.location === slotId ||
      ex.card_id === slotId
    ) {
      return card;
    }
  }
  return null;
}
