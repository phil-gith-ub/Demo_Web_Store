import { useEffect, useRef } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "./components/AppLayout";
import { useBrazeLogs } from "./context/BrazeLogContext";
import { useProfile } from "./context/ProfileContext";
import { registerBrazeAppLogSink } from "./lib/brazeAppLog";
import {
  fetchBrazeUserProfileRest,
  isBrazeRestImportConfigured,
} from "./lib/brazeRestProfile";
import { brazeOnLogout, initBrazeForIdentifiedUser } from "./lib/brazeInit";
import { applyBrazeTotalRevenue } from "./lib/purchaseStorage";
import { syncVipStatusToBraze } from "./lib/brazeUserSyncWeb";
import { BrazeLogsPage } from "./pages/BrazeLogsPage";
import { CartPage } from "./pages/CartPage";
import { ContentPage } from "./pages/ContentPage";
import { ProfilePage } from "./pages/ProfilePage";
import { PurchaseHistoryPage } from "./pages/PurchaseHistoryPage";
import { SettingsPage } from "./pages/SettingsPage";
import { StorePage } from "./pages/StorePage";
import { DemostoreLinkInterceptor } from "./components/DemostoreLinkInterceptor";

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
 * After login: `initialize` (if needed) → subscribe (IAM / content cards / banners) → `changeUser` (SDK opens session).
 */
function BrazeIdentifiedSync() {
  const { currentUserId, refreshKey } = useProfile();
  const { pushLog } = useBrazeLogs();
  const introLogged = useRef(false);
  const prevUser = useRef<string | null>(null);

  useEffect(() => {
    registerBrazeAppLogSink(pushLog);
    return () => registerBrazeAppLogSink(null);
  }, [pushLog]);

  useEffect(() => {
    if (currentUserId) {
      introLogged.current = true;
      const id = currentUserId;
      void initBrazeForIdentifiedUser(id, refreshKey, {
        /* `refreshKey` resets to 0 on page load — only >0 after an in-app login/logout cycle, so not on session restore. */
        logLoginEvent: refreshKey > 0,
      }).then((result) => {
        if (result.success) {
          pushLog({
            type: "info",
            message: `Braze Web SDK initialized; changeUser("${id}") (session via SDK)`,
          });
          window.dispatchEvent(new CustomEvent("braze:identified-ready"));
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
      const leaving = prevUser.current;
      void brazeOnLogout(leaving);
      pushLog({
        type: "info",
        message:
          "Logged out — Braze logged_out flushed, SDK destroyed (clean re-init on next login).",
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
  }, [currentUserId, refreshKey, pushLog]);

  return null;
}

/** Pull profile + Braze `total_revenue` on login (not tied to Profile route). */
function BrazeRestProfileImportSync() {
  const { currentUserId, refreshKey, updateProfile } = useProfile();
  const { pushLog } = useBrazeLogs();

  useEffect(() => {
    if (!currentUserId) return;
    if (!isBrazeRestImportConfigured()) return;
    const ac = new AbortController();
    void (async () => {
      const r = await fetchBrazeUserProfileRest(currentUserId, ac.signal);
      if (ac.signal.aborted) return;
      if (!r.ok) {
        pushLog({ type: "error", message: `Braze REST import: ${r.message}` });
        return;
      }
      updateProfile(r.patch);
      if (r.totalRevenueUsd != null) {
        applyBrazeTotalRevenue(currentUserId, r.totalRevenueUsd);
      }
      const waitForSdkThenSyncVip = async () => {
        const braze = await import("@braze/web-sdk");
        for (let i = 0; i < 80; i++) {
          if (braze.isInitialized?.()) {
            await syncVipStatusToBraze(currentUserId);
            return;
          }
          await new Promise((resolve) => setTimeout(resolve, 50));
        }
      };
      void waitForSdkThenSyncVip();
      pushLog({
        type: "info",
        message: "Profile fields loaded from Braze (REST).",
      });
    })();
    return () => ac.abort();
  }, [currentUserId, refreshKey, pushLog, updateProfile]);

  return null;
}

export default function App() {
  return (
    <>
      <ThemeSync />
      <BrazeIdentifiedSync />
      <BrazeRestProfileImportSync />
      <DemostoreLinkInterceptor />
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
