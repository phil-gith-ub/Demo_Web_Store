import type { Product, PurchaseOrder } from "../types/product";
import { STORAGE } from "./storageKeys";

const SPEND_UPDATED = "phil-store:spend-updated";

function dispatchSpendUpdated(userId: string) {
  window.dispatchEvent(
    new CustomEvent(SPEND_UPDATED, { detail: { userId } }),
  );
}

export function subscribeSpendUpdated(
  fn: (userId: string) => void,
): () => void {
  const listener = (e: Event) => {
    const id = (e as CustomEvent<{ userId?: string }>).detail?.userId;
    if (id) fn(id);
  };
  window.addEventListener(SPEND_UPDATED, listener);
  return () => window.removeEventListener(SPEND_UPDATED, listener);
}

function loadMap(): Record<string, PurchaseOrder[]> {
  try {
    const raw = localStorage.getItem(STORAGE.purchases);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as Record<string, PurchaseOrder[]>;
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch {
    return {};
  }
}

function saveMap(map: Record<string, PurchaseOrder[]>) {
  localStorage.setItem(STORAGE.purchases, JSON.stringify(map));
}

function loadBrazeRevenueMap(): Record<string, number> {
  try {
    const raw = localStorage.getItem(STORAGE.brazeTotalRevenueByUser);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as Record<string, number>;
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch {
    return {};
  }
}

function saveBrazeRevenueMap(map: Record<string, number>) {
  localStorage.setItem(STORAGE.brazeTotalRevenueByUser, JSON.stringify(map));
}

/** Latest Braze REST `total_revenue` seen for this user (USD). */
export function getCachedBrazeTotalRevenue(userId: string): number {
  const v = loadBrazeRevenueMap()[userId];
  return typeof v === "number" && Number.isFinite(v) ? v : 0;
}

/**
 * Store Braze lifetime revenue from REST import. Uses max with prior cache so a stale response
 * does not drop cross-device totals already applied.
 */
export function applyBrazeTotalRevenue(userId: string, totalRevenueUsd: number): void {
  if (!userId.trim() || !Number.isFinite(totalRevenueUsd) || totalRevenueUsd < 0) return;
  const map = loadBrazeRevenueMap();
  const prev = map[userId] ?? 0;
  map[userId] = Math.max(prev, totalRevenueUsd);
  saveBrazeRevenueMap(map);
  dispatchSpendUpdated(userId);
}

export function getPurchaseHistory(userId: string): PurchaseOrder[] {
  return loadMap()[userId] ?? [];
}

export function recordPurchase(userId: string, products: Product[]): PurchaseOrder {
  const map = loadMap();
  const list = map[userId] ?? [];
  const order: PurchaseOrder = {
    orderId: crypto.randomUUID(),
    userId,
    purchaseDate: new Date().toISOString(),
    products: [...products],
    totalAmount: products.reduce((s, p) => s + p.price, 0),
  };
  list.push(order);
  map[userId] = list;
  saveMap(map);
  dispatchSpendUpdated(userId);
  return order;
}

export function getTotalSpent(userId: string): number {
  return getPurchaseHistory(userId).reduce((s, o) => s + o.totalAmount, 0);
}

/** Max of local checkout history and cached Braze `total_revenue` (cross-device). */
export function getEffectiveTotalSpent(userId: string): number {
  return Math.max(getTotalSpent(userId), getCachedBrazeTotalRevenue(userId));
}

/** VIP when effective lifetime spend exceeds $1000 */
export function isVip(userId: string): boolean {
  return getEffectiveTotalSpent(userId) > 1000;
}
