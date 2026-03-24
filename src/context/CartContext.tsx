import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type { Product } from "../types/product";
import { STORAGE } from "../lib/storageKeys";

function loadCart(): Product[] {
  try {
    const raw = localStorage.getItem(STORAGE.cartItems);
    if (!raw) return [];
    const parsed = JSON.parse(raw) as Product[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function saveCart(items: Product[]) {
  localStorage.setItem(STORAGE.cartItems, JSON.stringify(items));
}

type CartContextValue = {
  items: Product[];
  itemCount: number;
  addToCart: (product: Product) => void;
  removeFromCart: (product: Product) => void;
  clearCart: () => void;
  totalPrice: number;
};

const CartContext = createContext<CartContextValue | null>(null);

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<Product[]>(() => loadCart());

  const persist = useCallback((next: Product[]) => {
    setItems(next);
    saveCart(next);
  }, []);

  const addToCart = useCallback(
    (product: Product) => {
      persist([...items, product]);
    },
    [items, persist],
  );

  const removeFromCart = useCallback(
    (product: Product) => {
      const idx = items.findIndex((p) => p.id === product.id);
      if (idx < 0) return;
      const next = [...items];
      next.splice(idx, 1);
      persist(next);
    },
    [items, persist],
  );

  const clearCart = useCallback(() => persist([]), [persist]);

  const value = useMemo(
    () => ({
      items,
      itemCount: items.length,
      addToCart,
      removeFromCart,
      clearCart,
      totalPrice: items.reduce((s, p) => s + p.price, 0),
    }),
    [items, addToCart, removeFromCart, clearCart],
  );

  return (
    <CartContext.Provider value={value}>{children}</CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart outside CartProvider");
  return ctx;
}
