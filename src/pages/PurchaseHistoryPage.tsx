import { Link } from "react-router-dom";
import { useProfile } from "../context/ProfileContext";
import { getPurchaseHistory } from "../lib/purchaseStorage";

export function PurchaseHistoryPage() {
  const { currentUserId } = useProfile();
  const orders =
    currentUserId ? getPurchaseHistory(currentUserId) : [];

  return (
    <>
      <h1 className="page-title">Purchase history</h1>
      <p>
        <Link to="/profile">← Back to profile</Link>
      </p>
      {!currentUserId ? (
        <p>Log in to see purchase history.</p>
      ) : orders.length === 0 ? (
        <p>No purchases yet.</p>
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
  );
}
