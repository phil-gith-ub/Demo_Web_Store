import { useEffect, useState } from "react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import { TopBanner } from "./TopBanner";
import { BrazeNotificationPanel } from "./BrazeNotificationPanel";
import {
  IconArticle,
  IconCart,
  IconStore,
  IconUser,
  IconConsole,
} from "./NavIcons";
import { useCart } from "../context/CartContext";
import type { ContentCards } from "@braze/web-sdk";

const nav = [
  { to: "/store", label: "Store", Icon: IconStore },
  { to: "/cart", label: "Cart", Icon: IconCart },
  { to: "/content", label: "Content", Icon: IconArticle },
  { to: "/profile", label: "Profile", Icon: IconUser },
  { to: "/console", label: "Console", Icon: IconConsole },
] as const;

export function AppLayout() {
  const { pathname } = useLocation();
  const { itemCount } = useCart();
  const overlay = pathname === "/settings" || pathname === "/logs";

  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const [unviewedCount, setUnviewedCount] = useState(0);

  useEffect(() => {
    const syncCount = async () => {
      const braze = await import("@braze/web-sdk");
      if (braze.isInitialized?.()) {
        const cc = braze.getCachedContentCards();
        setUnviewedCount(cc?.getUnviewedCardCount() ?? 0);
      }
    };
    void syncCount();
    const onContentCards = (ev: Event) => {
      try {
        const detail = (ev as CustomEvent<ContentCards>).detail;
        if (detail && typeof detail.getUnviewedCardCount === "function") {
          setUnviewedCount(detail.getUnviewedCardCount());
          return;
        }
      } catch {
        /* ignore */
      }
      void syncCount();
    };
    window.addEventListener("braze:content-cards", onContentCards);
    window.addEventListener("braze:identified-ready", syncCount);
    return () => {
      window.removeEventListener("braze:content-cards", onContentCards);
      window.removeEventListener("braze:identified-ready", syncCount);
    };
  }, []);

  return (
    <div className="app-shell">
      {!overlay && (
        <TopBanner
          unviewedCount={unviewedCount}
          onNotificationClick={() => setIsNotificationOpen((prev) => !prev)}
        />
      )}
      {overlay ? (
        <main className="app-main">
          <div className="page-center">
            <Outlet />
          </div>
        </main>
      ) : (
        <div className="app-body">
          <nav className="app-sidebar" aria-label="Main">
            <div className="app-sidebar-nav">
              {nav.map(({ to, label, Icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  className={({ isActive }) =>
                    "nav-link" + (isActive ? " active" : "")
                  }
                  end={to === "/store"}
                >
                  <Icon />
                  <span className="nav-link-text">
                    {label}
                    {to === "/cart" && itemCount > 0 ? (
                      <span className="badge">{itemCount}</span>
                    ) : null}
                  </span>
                </NavLink>
              ))}
            </div>
          </nav>
          <main className="app-main">
            <div className="page-center">
              <Outlet />
            </div>
          </main>
        </div>
      )}
      <BrazeNotificationPanel
        isOpen={isNotificationOpen}
        onClose={() => setIsNotificationOpen(false)}
      />
    </div>
  );
}

