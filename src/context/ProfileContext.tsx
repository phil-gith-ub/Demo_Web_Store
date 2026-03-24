import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { STORAGE } from "../lib/storageKeys";

export type UserProfile = {
  userId: string;
  firstName: string;
  lastName: string;
  email: string;
  mobile: string;
  favoriteProductCategory: string;
  isDarkModeEnabled: boolean;
  paidMembership: boolean;
  customEvents: string[];
};

function emptyProfile(userId: string): UserProfile {
  return {
    userId,
    firstName: "",
    lastName: "",
    email: "",
    mobile: "",
    favoriteProductCategory: "",
    isDarkModeEnabled: false,
    paidMembership: false,
    customEvents: [],
  };
}

function loadProfiles(): Record<string, UserProfile> {
  try {
    const raw = localStorage.getItem(STORAGE.profiles);
    if (!raw) return {};
    const parsed = JSON.parse(raw) as Record<string, UserProfile>;
    return parsed && typeof parsed === "object" ? parsed : {};
  } catch {
    return {};
  }
}

function saveProfiles(map: Record<string, UserProfile>) {
  localStorage.setItem(STORAGE.profiles, JSON.stringify(map));
}

function loadCurrentUserId(): string | null {
  return localStorage.getItem(STORAGE.currentUserId);
}

function saveCurrentUserId(id: string | null) {
  if (id) localStorage.setItem(STORAGE.currentUserId, id);
  else localStorage.removeItem(STORAGE.currentUserId);
}

const GUEST_DARK_KEY = "phil_wed_store_guest_dark";

function loadGuestDarkMode(): boolean {
  return localStorage.getItem(GUEST_DARK_KEY) === "1";
}

type ProfileContextValue = {
  currentUserId: string | null;
  profile: UserProfile | null;
  login: (userId: string) => void;
  logout: () => void;
  updateProfile: (patch: Partial<UserProfile>) => void;
  refreshKey: number;
  /** Used when logged out (persists in localStorage). */
  guestDarkMode: boolean;
  setGuestDarkMode: (enabled: boolean) => void;
};

const ProfileContext = createContext<ProfileContextValue | null>(null);

export function ProfileProvider({ children }: { children: ReactNode }) {
  const [currentUserId, setCurrentUserId] = useState<string | null>(() =>
    loadCurrentUserId(),
  );
  const [profiles, setProfiles] = useState<Record<string, UserProfile>>(() =>
    loadProfiles(),
  );
  const [refreshKey, setRefreshKey] = useState(0);
  const [guestDarkMode, setGuestDarkModeState] = useState(loadGuestDarkMode);

  const profile = currentUserId ? profiles[currentUserId] ?? null : null;

  const setGuestDarkMode = useCallback((enabled: boolean) => {
    setGuestDarkModeState(enabled);
    localStorage.setItem(GUEST_DARK_KEY, enabled ? "1" : "0");
  }, []);

  const login = useCallback((userId: string) => {
    const id = userId.trim();
    if (!id) return;
    const map = loadProfiles();
    if (!map[id]) {
      map[id] = emptyProfile(id);
      saveProfiles(map);
    }
    setProfiles(map);
    saveCurrentUserId(id);
    setCurrentUserId(id);
    setRefreshKey((k) => k + 1);
  }, []);

  const logout = useCallback(() => {
    saveCurrentUserId(null);
    setCurrentUserId(null);
    setRefreshKey((k) => k + 1);
  }, []);

  const updateProfile = useCallback(
    (patch: Partial<UserProfile>) => {
      if (!currentUserId) return;
      setProfiles((prev) => {
        const map = { ...prev };
        const base = map[currentUserId] ?? emptyProfile(currentUserId);
        const next = { ...base, ...patch, userId: currentUserId };
        map[currentUserId] = next;
        saveProfiles(map);
        return map;
      });
    },
    [currentUserId],
  );

  const value = useMemo(
    () => ({
      currentUserId,
      profile,
      login,
      logout,
      updateProfile,
      refreshKey,
      guestDarkMode,
      setGuestDarkMode,
    }),
    [
      currentUserId,
      profile,
      login,
      logout,
      updateProfile,
      refreshKey,
      guestDarkMode,
      setGuestDarkMode,
    ],
  );

  return (
    <ProfileContext.Provider value={value}>{children}</ProfileContext.Provider>
  );
}

export function useProfile() {
  const ctx = useContext(ProfileContext);
  if (!ctx) throw new Error("useProfile outside ProfileProvider");
  return ctx;
}
