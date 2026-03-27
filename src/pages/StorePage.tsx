import { useEffect, useLayoutEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { BrazeBannerSlot } from "../components/BrazeBannerSlot";
import { useProfile } from "../context/ProfileContext";
import { useCart } from "../context/CartContext";
import { mockProducts } from "../data/mockProducts";
import {
  isVipProductsEnabled,
  logAddedItemToCart,
  logViewedVipProducts,
} from "../lib/brazeUserSyncWeb";
import { isVip } from "../lib/purchaseStorage";

const TAB_ALL = "ALL";
const TAB_VIP = "VIP";

export function StorePage() {
  const { addToCart } = useCart();
  const { currentUserId, refreshKey } = useProfile();
  const [searchParams] = useSearchParams();
  const [category, setCategory] = useState<string>(TAB_ALL);
  const [vipFeatureEnabled, setVipFeatureEnabled] = useState(true);
  const [isVipUser, setIsVipUser] = useState(false);

  const productCategories = useMemo(() => {
    const set = new Set(mockProducts.map((p) => p.category));
    return [...set].sort();
  }, []);

  const tabs = useMemo(() => {
    const row = [TAB_ALL, ...productCategories];
    if (vipFeatureEnabled) row.push(TAB_VIP);
    return row;
  }, [productCategories, vipFeatureEnabled]);

  useEffect(() => {
    if (searchParams.get("vip") === "1" && vipFeatureEnabled) {
      setCategory(TAB_VIP);
    }
  }, [searchParams, vipFeatureEnabled]);

  useEffect(() => {
    if (!currentUserId) {
      setVipFeatureEnabled(true);
      setIsVipUser(false);
      return;
    }
    setIsVipUser(isVip(currentUserId));
    let cancelled = false;
    const load = async () => {
      await new Promise((r) => setTimeout(r, 500));
      if (cancelled) return;
      const braze = await import("@braze/web-sdk");
      let tries = 0;
      while (!braze.isInitialized?.() && tries++ < 50) {
        await new Promise((r) => setTimeout(r, 100));
        if (cancelled) return;
      }
      const enabled = await isVipProductsEnabled();
      if (!cancelled) setVipFeatureEnabled(enabled);
      if (!cancelled) setIsVipUser(isVip(currentUserId));
    };
    void load();
    const onReady = () => void load();
    window.addEventListener("braze:identified-ready", onReady);
    return () => {
      cancelled = true;
      window.removeEventListener("braze:identified-ready", onReady);
    };
  }, [currentUserId, refreshKey]);

  useEffect(() => {
    if (category === TAB_VIP && !vipFeatureEnabled) {
      setCategory(TAB_ALL);
    }
  }, [category, vipFeatureEnabled]);

  const filtered = useMemo(() => {
    const all = mockProducts;
    if (category === TAB_VIP) {
      return isVipUser ? all.filter((p) => p.isVip) : [];
    }
    if (category === TAB_ALL) {
      return isVipUser ? all : all.filter((p) => !p.isVip);
    }
    return all.filter((p) => {
      if (p.category !== category) return false;
      return isVipUser || !p.isVip;
    });
  }, [category, isVipUser]);

  const tabLabel = (c: string) => {
    if (c === TAB_ALL) return "All Products";
    if (c === TAB_VIP) return "VIP Products";
    return c;
  };

  const selectCategory = (c: string) => {
    setCategory(c);
    if (c === TAB_VIP) void logViewedVipProducts();
  };

  const tabsRef = useRef<HTMLDivElement>(null);
  const [tabBarWidth, setTabBarWidth] = useState<number | null>(null);

  useLayoutEffect(() => {
    const el = tabsRef.current;
    if (!el) return;
    const ro = new ResizeObserver((entries) => {
      const w = entries[0]?.contentRect.width;
      if (w != null) setTabBarWidth(Math.round(w));
    });
    ro.observe(el);
    return () => ro.disconnect();
  }, [tabs]);

  const alignedStyle =
    tabBarWidth != null
      ? ({ width: tabBarWidth, maxWidth: "100%" } as const)
      : undefined;

  return (
    <>
      <h1 className="page-title">Store</h1>
      <div className="store-page-aligned-block" style={alignedStyle}>
        <BrazeBannerSlot title="Store banner" placementId="store_page_banner" />
      </div>
      <div
        ref={tabsRef}
        className="tabs store-tabs"
        role="tablist"
        aria-label="Product categories"
      >
        {tabs.map((c) => (
          <button
            key={c}
            type="button"
            role="tab"
            aria-selected={category === c}
            className={"tab" + (category === c ? " active" : "")}
            onClick={() => selectCategory(c)}
          >
            {tabLabel(c)}
          </button>
        ))}
      </div>

      {category === TAB_VIP && !isVipUser ? (
        <div
          className="vip-empty-card store-page-aligned-block"
          style={alignedStyle}
          role="status"
        >
          <p className="vip-empty-card-text">
            You have not reached VIP member status
          </p>
        </div>
      ) : (
        <div className="card-grid store-page-aligned-block" style={alignedStyle}>
          {filtered.map((product) => (
            <article key={product.id + product.name} className="product-card">
              {product.isVip ? (
                <span className="vip-pill">VIP</span>
              ) : null}
              <h3>{product.name}</h3>
              <p className="product-meta">{product.category}</p>
              <p className="product-meta product-desc">{product.description}</p>
              <p className="price">${product.price.toFixed(2)}</p>
              <button
                type="button"
                className="btn btn-primary product-add"
                onClick={() => {
                  addToCart(product);
                  if (currentUserId) void logAddedItemToCart(product);
                }}
              >
                Add to cart
              </button>
            </article>
          ))}
        </div>
      )}
    </>
  );
}
