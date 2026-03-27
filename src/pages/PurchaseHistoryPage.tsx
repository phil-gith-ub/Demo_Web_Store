import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useProfile } from "../context/ProfileContext";
import {
  getEffectiveTotalSpent,
  getPurchaseHistory,
  subscribeSpendUpdated,
} from "../lib/purchaseStorage";

export function PurchaseHistoryPage() {
  const { currentUserId } = useProfile();
  const [, setSpendTick] = useState(0);

  useEffect(() => {
    return subscribeSpendUpdated((id) => {
      if (id === currentUserId) setSpendTick((t) => t + 1);
    });
  }, [currentUserId]);

  const orders =
    currentUserId ? getPurchaseHistory(currentUserId) : [];
  const lifetimeTotal =
    currentUserId != null && currentUserId !== ""
      ? getEffectiveTotalSpent(currentUserId)
      : 0;

  return (
    <>
      <h1 className="page-title">Purchase history</h1>
      <p>
        <Link to="/profile">← Back to profile</Link>
      </p>
      {!currentUserId ? (
        <p>Log in to see purchase history.</p>
      ) : (
        <>
          <section
            className="product-card"
            style={{ marginBottom: "1.25rem" }}
            aria-labelledby="lifetime-spend-heading"
          >
            <h2 id="lifetime-spend-heading" className="page-title" style={{ fontSize: "1.1rem", margin: "0 0 0.5rem" }}>
              Total purchases (lifetime)
            </h2>
            <p className="price" style={{ margin: 0 }}>
              ${lifetimeTotal.toFixed(2)}
            </p>
            <p className="product-meta" style={{ margin: "0.5rem 0 0" }}>
              Includes orders on this device. When you log in, we also read Braze{" "}
              <code>total_revenue</code> so spend from other devices counts toward this total and VIP.
            </p>
          </section>
          {orders.length === 0 ? (
            <p>No purchases on this device yet.</p>
          ) : (
            <ul className="stack" style={{ listStyle: "none", padding: 0 }}>
              {orders
                .slice()
                .reverse()
                .map((order) => (
                  <li key={order.orderId} className="product-card">
                    <div className="product-meta">
                      {new Date(order.purchaseDate).toLocaleString()}
                    </div>
                    <div>Order {order.orderId.slice(0, 8)}…</div>
                    <ul>
                      {order.products.map((pr) => (
                        <li key={pr.id + pr.name}>
                          {pr.name} — ${pr.price.toFixed(2)}
                        </li>
                      ))}
                    </ul>
                    <div className="price">
                      Total: ${order.totalAmount.toFixed(2)}
                    </div>
                  </li>
                ))}
            </ul>
          )}
        </>
      )}
    </>
  );
}
