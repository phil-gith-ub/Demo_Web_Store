import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useProfile } from "../context/ProfileContext";
import { isVip } from "../lib/purchaseStorage";

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

  const p = profile;

  return (
    <>
      <h1 className="page-title">Profile</h1>
      <div className="stack profile-stack">
        <div className="form-row">
          <label htmlFor="login-id">User ID</label>
          <input
            id="login-id"
            value={userIdInput}
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

        {currentUserId ? (
          <p className="product-meta">
            VIP status: {isVip(currentUserId) ? "yes" : "no"} (total spent
            &gt; $1000)
          </p>
        ) : (
          <p className="product-meta">
            Log in to persist profile and use checkout. You can still edit fields
            below (stored locally until you log in).
          </p>
        )}

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
          <label htmlFor="cat">Favorite category</label>
          <input
            id="cat"
            value={
              currentUserId && p
                ? p.favoriteProductCategory
                : guest.favoriteProductCategory
            }
            onChange={(e) => {
              const v = e.target.value;
              if (currentUserId && p) {
                updateProfile({ favoriteProductCategory: v });
              } else {
                setGuest((g) => ({ ...g, favoriteProductCategory: v }));
              }
            }}
          />
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
              } else {
                setGuestDarkMode(v);
              }
            }}
          />
          <span>Dark mode</span>
        </label>

        <Link className="profile-history-link" to="/purchase-history">
          Purchase history →
        </Link>
      </div>
    </>
  );
}
