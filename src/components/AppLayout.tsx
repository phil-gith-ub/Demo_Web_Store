import { NavLink, Outlet, useLocation } from "react-router-dom";
import { TopBanner } from "./TopBanner";
import {
  IconArticle,
  IconCart,
  IconStore,
  IconUser,
  IconConsole,
} from "./NavIcons";
import { useCart } from "../context/CartContext";

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

  return (
    <div className="app-shell">
      {!overlay && <TopBanner />}
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
    </div>
  );
}
