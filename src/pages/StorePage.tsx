import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { mockProducts } from "../data/mockProducts";
import { useCart } from "../context/CartContext";
import { BrazeGhostSlot } from "../components/BrazeGhostSlot";

const ALL = "All";

export function StorePage() {
  const { addToCart } = useCart();
  const [searchParams] = useSearchParams();
  const [category, setCategory] = useState(ALL);
  /** TODO: Braze feature flag `enable_vip_products` */
  const [vipFeatureEnabled] = useState(true);
  const [showVipOnly, setShowVipOnly] = useState(false);

  useEffect(() => {
    if (searchParams.get("vip") === "1") {
      setShowVipOnly(true);
    }
  }, [searchParams]);

  const categories = useMemo(() => {
    const set = new Set(mockProducts.map((p) => p.category));
    return [ALL, ...[...set].sort()];
  }, []);

  const filtered = useMemo(() => {
    let list = mockProducts;
    if (category !== ALL) {
      list = list.filter((p) => p.category === category);
    }
    if (vipFeatureEnabled && showVipOnly) {
      list = list.filter((p) => p.isVip);
    }
    return list;
  }, [category, showVipOnly, vipFeatureEnabled]);

  return (
    <>
      <h1 className="page-title">Store</h1>
      <BrazeGhostSlot title="Store banner" placementId="store_page_banner" />
      <div className="tabs" role="tablist">
        {categories.map((c) => (
          <button
            key={c}
            type="button"
            role="tab"
            aria-selected={category === c}
            className={"tab" + (category === c ? " active" : "")}
            onClick={() => setCategory(c)}
          >
            {c}
          </button>
        ))}
        {vipFeatureEnabled ? (
          <button
            type="button"
            className={"tab" + (showVipOnly ? " active" : "")}
            onClick={() => setShowVipOnly((v) => !v)}
          >
            VIP products
          </button>
        ) : null}
      </div>
      <div className="card-grid">
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
              onClick={() => addToCart(product)}
            >
              Add to cart
            </button>
          </article>
        ))}
      </div>
    </>
  );
}
