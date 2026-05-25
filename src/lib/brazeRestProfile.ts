import type { UserProfile } from "../context/ProfileContext";
import { brazeAppLog } from "./brazeAppLog";
import { mergeBrazeLastSent } from "./brazeSyncState";
import {
  getBrazeSettings,
  normalizeBrazeRestEndpoint,
} from "./brazeSettings";

/** Matches `syncUserToBraze` custom keys. */
const FIELDS_TO_EXPORT = [
  "external_id",
  "email",
  "first_name",
  "last_name",
  "phone",
  "total_revenue",
  "custom_attributes",
] as const;

type BrazeExportUser = {
  external_id?: string;
  email?: string | null;
  first_name?: string | null;
  last_name?: string | null;
  phone?: string | null;
  phone_number?: string | null;
  total_revenue?: number | string | null;
  custom_attributes?: Record<string, unknown> | null;
};

type ExportIdsResponse = {
  message?: string;
  users?: BrazeExportUser[];
  invalid_user_ids?: string[];
};

export function isBrazeRestImportConfigured(): boolean {
  const s = getBrazeSettings();
  if (!s.restApiKey.trim()) return false;
  if (import.meta.env.DEV) return true;
  return !!normalizeBrazeRestEndpoint(s.restEndpoint);
}

/**
 * In dev, requests go to `/braze-rest/...` (Vite proxy → Braze REST host) to avoid browser CORS.
 * In production, uses the saved REST URL (only works if your host allows CORS or you add a same-origin proxy).
 */
function restRequestUrl(apiPath: string): string {
  const path = apiPath.startsWith("/") ? apiPath : `/${apiPath}`;
  if (import.meta.env.DEV) {
    return `/braze-rest${path}`;
  }
  const base = normalizeBrazeRestEndpoint(getBrazeSettings().restEndpoint);
  return `${base}${path}`;
}

function str(v: unknown): string {
  if (v == null) return "";
  if (typeof v === "string") return v;
  return String(v);
}

function boolFromCustom(v: unknown): boolean | undefined {
  if (typeof v === "boolean") return v;
  if (v === "true") return true;
  if (v === "false") return false;
  return undefined;
}

/** Braze user export `total_revenue` (USD). */
function totalRevenueUsdFromRow(row: BrazeExportUser): number | undefined {
  const v = row.total_revenue;
  if (v == null) return undefined;
  if (typeof v === "number" && Number.isFinite(v)) return v >= 0 ? v : undefined;
  if (typeof v === "string") {
    const n = parseFloat(v);
    return Number.isFinite(n) && n >= 0 ? n : undefined;
  }
  return undefined;
}

export function mapBrazeExportToProfilePatch(
  row: BrazeExportUser,
  userId: string,
): Partial<UserProfile> {
  const custom = row.custom_attributes ?? {};
  const paid = boolFromCustom(custom.paid_membership);

  const patch: Partial<UserProfile> = {
    userId,
    firstName: str(row.first_name),
    lastName: str(row.last_name),
    email: str(row.email),
    mobile: str(row.phone ?? row.phone_number),
    favoriteProductCategory: str(custom.favorite_product_category),
  };
  if (paid !== undefined) {
    patch.paidMembership = paid;
  }
  const dark = boolFromCustom(custom.is_dark_mode_enabled);
  if (dark !== undefined) {
    patch.isDarkModeEnabled = dark;
  }
  return patch;
}

function syncLastSentAfterImport(userId: string, patch: Partial<UserProfile>) {
  mergeBrazeLastSent(userId, {
    firstName: patch.firstName?.trim() || null,
    lastName: patch.lastName?.trim() || null,
    email: patch.email?.trim() || null,
    mobile: patch.mobile?.trim() || null,
    favoriteProductCategory: patch.favoriteProductCategory?.trim() || null,
    ...(patch.paidMembership !== undefined
      ? { paidMembership: patch.paidMembership }
      : {}),
  });
}

export type BrazeRestImportResult =
  | { ok: true; patch: Partial<UserProfile>; totalRevenueUsd?: number }
  | { ok: false; message: string };

/**
 * POST /users/export/ids — Braze REST API (Bearer REST key).
 * @see https://www.braze.com/docs/api/endpoints/export/user_data/post_users_identifier/
 */
export async function fetchBrazeUserProfileRest(
  externalId: string,
  signal?: AbortSignal,
): Promise<BrazeRestImportResult> {
  const id = externalId.trim();
  if (!id) {
    return { ok: false, message: "User ID is empty." };
  }

  const { restApiKey } = getBrazeSettings();
  const key = restApiKey.trim();
  if (!key) {
    return {
      ok: false,
      message:
        "Add a Braze REST API key in Settings (not the Web SDK key) to import profiles.",
    };
  }

  if (!import.meta.env.DEV) {
    const base = normalizeBrazeRestEndpoint(getBrazeSettings().restEndpoint);
    if (!base) {
      return {
        ok: false,
        message:
          "Add your Braze REST instance URL in Settings (e.g. https://todd.braze.com for the demo stack).",
      };
    }
  }

  const url = restRequestUrl("/users/export/ids");
  let res: Response;
  try {
    res = await fetch(url, {
      method: "POST",
      signal,
      headers: {
        Authorization: `Bearer ${key}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        external_ids: [id],
        fields_to_export: [...FIELDS_TO_EXPORT],
      }),
    });
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    if (/Failed to fetch|NetworkError|load failed/i.test(msg)) {
      return {
        ok: false,
        message:
          import.meta.env.DEV
            ? "Network error — ensure Vite proxy is set (BRAZE_REST_PROXY_TARGET in .env) and matches your Braze REST instance."
            : `Network or CORS error: ${msg}. Braze REST is usually called from a server or same-origin proxy.`,
      };
    }
    return { ok: false, message: msg };
  }

  let json: ExportIdsResponse;
  try {
    json = (await res.json()) as ExportIdsResponse;
  } catch {
    return {
      ok: false,
      message: `Braze REST returned non-JSON (HTTP ${res.status}).`,
    };
  }

  if (!res.ok) {
    const detail =
      typeof json.message === "string" ? json.message : JSON.stringify(json);
    let hint = "";
    if (res.status === 401) {
      hint =
        " Check: use a REST API key from Braze (Manage Settings → API Keys), not the Web SDK key; key must include users export permission; and BRAZE_REST_PROXY_TARGET / REST URL must match your stack (demo: https://todd.braze.com; prod clusters differ by region).";
    }
    return {
      ok: false,
      message: `Braze REST HTTP ${res.status}: ${detail}.${hint}`,
    };
  }

  const users = json.users;
  if (!users?.length) {
    const invalid = json.invalid_user_ids?.length
      ? ` Unknown IDs: ${json.invalid_user_ids.join(", ")}.`
      : "";
    return {
      ok: false,
      message: `No user profile returned for "${id}".${invalid}`,
    };
  }

  const row = users[0];
  const patch = mapBrazeExportToProfilePatch(row, id);
  syncLastSentAfterImport(id, patch);
  const totalRevenueUsd = totalRevenueUsdFromRow(row);

  brazeAppLog({
    type: "info",
    message: `REST import: loaded profile fields for "${id}" from Braze.`,
  });

  return {
    ok: true,
    patch,
    ...(totalRevenueUsd != null ? { totalRevenueUsd } : {}),
  };
}
