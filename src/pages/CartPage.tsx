import { BrazeBannerSlot } from "../components/BrazeBannerSlot";
import { useProfile } from "../context/ProfileContext";
import { useCart } from "../context/CartContext";
import { useNavigate } from "react-router-dom";
import {
  logCheckoutPurchases,
  syncVipStatusToBraze,
} from "../lib/brazeUserSyncWeb";
import { recordPurchase, isVip } from "../lib/purchaseStorage";

export function CartPage() {
  const { items, itemCount, removeFromCart, clearCart, totalPrice } =
    useCart();
  const { currentUserId } = useProfile();
  const navigate = useNavigate();

  const checkout = () => {
    if (!currentUserId || items.length === 0) return;
    const lineItems = [...items];
    recordPurchase(currentUserId, lineItems);
    void logCheckoutPurchases(lineItems);
    void syncVipStatusToBraze(currentUserId);
    clearCart();
    navigate("/profile");
  };

  return (
    <>
      <h1 className="page-title">Cart</h1>
      <BrazeBannerSlot title="Cart banner" placementId="cart_banner" />
      {itemCount === 0 ? (
        <p>Your cart is empty.</p>
      ) : (
        <>
          <ul className="stack" style={{ listStyle: "none", padding: 0 }}>
            {items.map((product, i) => (
              <li
                key={`${product.id}-${i}`}
                className="product-card cart-line"
              >
                <div style={{ flex: 1 }}>
                  <strong>{product.name}</strong>
                  <div className="product-meta">${product.price.toFixed(2)}</div>
                </div>
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() => removeFromCart(product)}
                >
                  Remove
                </button>
              </li>
            ))}
          </ul>
          <p className="price">Total: ${totalPrice.toFixed(2)}</p>
          <button
            type="button"
            className="btn btn-primary"
            disabled={!currentUserId}
            onClick={checkout}
            title={
              !currentUserId ? "Log in on the Profile page to check out" : ""
            }
          >
            Check out
          </button>
          {!currentUserId ? (
            <p className="product-meta">
              Log in under Profile to complete checkout.
            </p>
          ) : null}
          {currentUserId && isVip(currentUserId) ? (
            <p className="product-meta">You have VIP status (spent &gt; $1000).</p>
          ) : null}
        </>
      )}
    </>
  );
}
