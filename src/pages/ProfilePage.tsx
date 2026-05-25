import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { BrazeWebPushPanel } from "../components/BrazeWebPushPanel";
import { useProfile } from "../context/ProfileContext";
import { PROFILE_FAVORITE_CATEGORIES } from "../data/productCategories";
import { logEnabledDarkMode, syncUserToBraze } from "../lib/brazeUserSyncWeb";
import { isVip, subscribeSpendUpdated } from "../lib/purchaseStorage";

type GuestFields = {
  firstName: string;
  lastName: string;
  email: string;
  mobile: string;
  favoriteProductCategory: string;
  paidMembership: boolean;
};

const emptyGuest: GuestFields = {
  firstName: "",
  lastName: "",
  email: "",
  mobile: "",
  favoriteProductCategory: "",
  paidMembership: false,
};

export function ProfilePage() {
  const {
    currentUserId,
    profile,
    login,
    logout,
    updateProfile,
    guestDarkMode,
    setGuestDarkMode,
  } = useProfile();
  const [userIdInput, setUserIdInput] = useState(currentUserId ?? "");
  const [guest, setGuest] = useState<GuestFields>(emptyGuest);

  useEffect(() => {
    setUserIdInput(currentUserId ?? "");
  }, [currentUserId]);

  const [, spendBump] = useState(0);
  useEffect(() => {
    return subscribeSpendUpdated((id) => {
      if (id === currentUserId) spendBump((n) => n + 1);
    });
  }, [currentUserId]);

  const p = profile;
  const userIdLocked = Boolean(currentUserId && p);

  const favoriteCategoryValue =
    currentUserId && p ? p.favoriteProductCategory : guest.favoriteProductCategory;
  const legacyCategory =
    favoriteCategoryValue.trim() &&
    !PROFILE_FAVORITE_CATEGORIES.includes(
      favoriteCategoryValue as (typeof PROFILE_FAVORITE_CATEGORIES)[number],
    )
      ? favoriteCategoryValue
      : null;

  return (
    <>
      <h1 className="page-title">Profile</h1>
      <div className="stack profile-stack">
        <div className="form-row">
          <label htmlFor="login-id">User ID</label>
          <input
            id="login-id"
            className={userIdLocked ? "profile-user-id--locked" : undefined}
            value={userIdInput}
            readOnly={userIdLocked}
            autoComplete={userIdLocked ? "off" : "username"}
            title={
              userIdLocked
                ? "Log out to change your user ID"
                : undefined
            }
            onChange={(e) => setUserIdInput(e.target.value)}
            placeholder="Enter user id to log in"
          />
          <div className="profile-actions">
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => login(userIdInput)}
            >
              Log in
            </button>
            {currentUserId ? (
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => {
                  logout();
                  setUserIdInput("");
                }}
              >
                Log out
              </button>
            ) : null}
          </div>
        </div>

        {!currentUserId ? (
          <p className="product-meta">
            Log in to persist profile and use checkout. You can still edit fields
            below (stored locally until you log in).
          </p>
        ) : null}

        <div className="form-row">
          <label htmlFor="fn">First name</label>
          <input
            id="fn"
            value={currentUserId && p ? p.firstName : guest.firstName}
            onChange={(e) => {
              const v = e.target.value;
              if (currentUserId && p) updateProfile({ firstName: v });
              else setGuest((g) => ({ ...g, firstName: v }));
            }}
          />
        </div>
        <div className="form-row">
          <label htmlFor="ln">Last name</label>
          <input
            id="ln"
            value={currentUserId && p ? p.lastName : guest.lastName}
            onChange={(e) => {
              const v = e.target.value;
              if (currentUserId && p) updateProfile({ lastName: v });
              else setGuest((g) => ({ ...g, lastName: v }));
            }}
          />
        </div>
        <div className="form-row">
          <label htmlFor="em">Email</label>
          <input
            id="em"
            type="email"
            value={currentUserId && p ? p.email : guest.email}
            onChange={(e) => {
              const v = e.target.value;
              if (currentUserId && p) updateProfile({ email: v });
              else setGuest((g) => ({ ...g, email: v }));
            }}
          />
        </div>
        <div className="form-row">
          <label htmlFor="ph">Mobile</label>
          <input
            id="ph"
            value={currentUserId && p ? p.mobile : guest.mobile}
            onChange={(e) => {
              const v = e.target.value;
              if (currentUserId && p) updateProfile({ mobile: v });
              else setGuest((g) => ({ ...g, mobile: v }));
            }}
          />
        </div>
        <div className="form-row">
          <label htmlFor="cat">Favorite product category</label>
          <select
            id="cat"
            value={favoriteCategoryValue}
            onChange={(e) => {
              const v = e.target.value;
              if (currentUserId && p) {
                updateProfile({ favoriteProductCategory: v });
              } else {
                setGuest((g) => ({ ...g, favoriteProductCategory: v }));
              }
            }}
          >
            <option value="">—</option>
            {legacyCategory ? (
              <option value={legacyCategory}>
                {legacyCategory} (not in list)
              </option>
            ) : null}
            {PROFILE_FAVORITE_CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </div>

        <label className="toggle-row">
          <input
            type="checkbox"
            checked={currentUserId && p ? p.paidMembership : guest.paidMembership}
            onChange={(e) => {
              const v = e.target.checked;
              if (currentUserId && p) updateProfile({ paidMembership: v });
              else setGuest((g) => ({ ...g, paidMembership: v }));
            }}
          />
          <span>Paid membership</span>
        </label>

        <label className="toggle-row">
          <input
            type="checkbox"
            checked={
              currentUserId && p ? p.isDarkModeEnabled : guestDarkMode
            }
            onChange={(e) => {
              const v = e.target.checked;
              if (currentUserId && p) {
                updateProfile({ isDarkModeEnabled: v });
                if (v) void logEnabledDarkMode();
              } else {
                setGuestDarkMode(v);
              }
            }}
          />
          <span>Dark mode</span>
        </label>

        {/* syncUserToBraze sends only changed profile fields (delta), like Android Save profile. */}
        {currentUserId && p ? (
          <div className="profile-actions">
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => void syncUserToBraze(p)}
            >
              Save profile to Braze
            </button>
          </div>
        ) : null}

        {currentUserId ? (
          <div
            className={`profile-vip-status${isVip(currentUserId) ? " profile-vip-status--active" : ""}`}
            role="status"
            aria-label="VIP membership status"
          >
            <span className="profile-vip-status__badge">VIP</span>
            <div className="profile-vip-status__body">
              <span className="profile-vip-status__title">
                {isVip(currentUserId)
                  ? "VIP access active"
                  : "Not a VIP yet"}
              </span>
              {!isVip(currentUserId) ? (
                <p className="profile-vip-status__hint">
                  Spend over $1,000 in total to unlock VIP products and offers.
                </p>
              ) : null}
            </div>
          </div>
        ) : null}

        <Link className="profile-history-link" to="/purchase-history">
          Purchase history →
        </Link>

        <BrazeWebPushPanel />
      </div>
    </>
  );
}
