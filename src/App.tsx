import { useEffect, useRef } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "./components/AppLayout";
import { useBrazeLogs } from "./context/BrazeLogContext";
import { useProfile } from "./context/ProfileContext";
import { brazeOnLogout, initBrazeForIdentifiedUser } from "./lib/brazeInit";
import { BrazeLogsPage } from "./pages/BrazeLogsPage";
import { CartPage } from "./pages/CartPage";
import { ContentPage } from "./pages/ContentPage";
import { ProfilePage } from "./pages/ProfilePage";
import { PurchaseHistoryPage } from "./pages/PurchaseHistoryPage";
import { SettingsPage } from "./pages/SettingsPage";
import { StorePage } from "./pages/StorePage";

function ThemeSync() {
  const { profile, currentUserId, guestDarkMode } = useProfile();
  useEffect(() => {
    const dark = currentUserId
      ? !!profile?.isDarkModeEnabled
      : guestDarkMode;
    document.documentElement.classList.toggle("dark", dark);
  }, [currentUserId, profile?.isDarkModeEnabled, guestDarkMode]);
  return null;
}

/**
 * Delayed Braze setup: no SDK for anonymous visitors.
 * After login: `initialize` (if needed) → `changeUser` → `automaticallyShowInAppMessages` → `openSession` (Braze Web SDK pattern).
 */
function BrazeIdentifiedSync() {
  const { currentUserId } = useProfile();
  const { pushLog } = useBrazeLogs();
  const introLogged = useRef(false);
  const prevUser = useRef<string | null>(null);

  useEffect(() => {
    if (currentUserId) {
      introLogged.current = true;
      const id = currentUserId;
      void initBrazeForIdentifiedUser(id).then((result) => {
        if (result.success) {
          pushLog({
            type: "info",
            message: `Braze Web SDK initialized; changeUser("${id}")`,
          });
        } else {
          pushLog({
            type: "error",
            message: result.message,
          });
        }
      });
      prevUser.current = id;
      return;
    }

    if (prevUser.current !== null) {
      void brazeOnLogout();
      pushLog({
        type: "info",
        message: "Logged out — Braze SDK destroyed (ready for a clean re-init on next login).",
      });
      prevUser.current = null;
      return;
    }

    if (!introLogged.current) {
      introLogged.current = true;
      pushLog({
        type: "info",
        message:
          "Braze Web SDK runs only after you log in with a user ID. Add credentials in Settings, then use Profile → Log in.",
      });
    }
  }, [currentUserId, pushLog]);

  return null;
}

export default function App() {
  return (
    <>
      <ThemeSync />
      <BrazeIdentifiedSync />
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/store" replace />} />
          <Route path="/store" element={<StorePage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/content" element={<ContentPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/logs" element={<BrazeLogsPage />} />
          <Route path="/purchase-history" element={<PurchaseHistoryPage />} />
        </Route>
      </Routes>
    </>
  );
}
