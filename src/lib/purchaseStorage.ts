import type { Product, PurchaseOrder } from "../types/product";
import { STORAGE } from "./storageKeys";

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
  return order;
}

export function getTotalSpent(userId: string): number {
  return getPurchaseHistory(userId).reduce((s, o) => s + o.totalAmount, 0);
}

/** VIP when lifetime spend for the user exceeds $1000 */
export function isVip(userId: string): boolean {
  return getTotalSpent(userId) > 1000;
}
