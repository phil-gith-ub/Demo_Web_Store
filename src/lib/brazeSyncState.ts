import { STORAGE } from "./storageKeys";

export type BrazeLastSentValues = {
  firstName: string | null;
  lastName: string | null;
  email: string | null;
  mobile: string | null;
  favoriteProductCategory: string | null;
  activeMember: boolean | null;
  vipMember: boolean | null;
  paidMembership: boolean | null;
};

function empty(): BrazeLastSentValues {
  return {
    firstName: null,
    lastName: null,
    email: null,
    mobile: null,
    favoriteProductCategory: null,
    activeMember: null,
    vipMember: null,
    paidMembership: null,
  };
}

export function loadBrazeLastSent(userId: string): BrazeLastSentValues {
  try {
    const raw = localStorage.getItem(STORAGE.brazeLastSent);
    if (!raw) return empty();
    const all = JSON.parse(raw) as Record<string, Partial<BrazeLastSentValues>>;
    const row = all[userId];
    if (!row) return empty();
    return { ...empty(), ...row };
  } catch {
    return empty();
  }
}

export function mergeBrazeLastSent(
  userId: string,
  patch: Partial<BrazeLastSentValues>,
): void {
  const cur = loadBrazeLastSent(userId);
  const next = { ...cur, ...patch };
  try {
    const raw = localStorage.getItem(STORAGE.brazeLastSent);
    const all = raw
      ? (JSON.parse(raw) as Record<string, BrazeLastSentValues>)
      : {};
    all[userId] = next;
    localStorage.setItem(STORAGE.brazeLastSent, JSON.stringify(all));
  } catch {
    /* ignore quota / parse */
  }
}
