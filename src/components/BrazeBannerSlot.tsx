import {
  useCallback,
  useEffect,
  useLayoutEffect,
  useRef,
  useState,
} from "react";
import type { Banner } from "@braze/web-sdk";
import { DEMOSTORE_DOM_SCAN_EVENT } from "../lib/demostoreDeepLink";
import { BrazeGhostSlot } from "./BrazeGhostSlot";

export type BrazeBannerVariant = "default" | "wide" | "square";

type Props = {
  title: string;
  placementId: string;
  /**
   * `wide` = 2:1 (Android content_banner). `square` = 1:1 (Android tile_banner).
   * `default` = flexible height for Store/Cart.
   */
  variant?: BrazeBannerVariant;
  /**
   * When `variant` is `square`, keep a strict 1:1 box: height follows width as the layout resizes.
   * Use for `tile_banner`; omit for square placeholders that should shrink to short live HTML.
   */
  lockSquareAspect?: boolean;
};

/** Coalesce rapid `subscribeToBannersUpdates` notifications (SDK + our refresh calls). */
const EMPTY_DEBOUNCE_MS = 120;

type BrazeModule = typeof import("@braze/web-sdk");

function bannerContentKey(b: Banner): string {
  return `${b.id}\0${b.html}`;
}

/**
 * Renders a Braze banner via `insertBanner` when available; otherwise shows the dashed placeholder.
 * `allowUserSuppliedJavascript: true` is set in `brazeInit` (required for `insertBanner`).
 *
 * Follows Braze guidance: `insertBanner` replaces the container’s children — do not clear the node
 * first (avoids a blank frame). `subscribeToBannersUpdates` can fire multiple times; we skip
 * redundant inserts when `id` + `html` are unchanged, and debounce transitions to “no banner” so
 * transient nulls do not flash the placeholder over live HTML.
 *
 * @see https://www.braze.com/docs/developer_guide/banners/placements_redirected=1
 */
export function BrazeBannerSlot({
  title,
  placementId,
  variant = "default",
  lockSquareAspect = false,
}: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const aliveRef = useRef(true);
  const [mode, setMode] = useState<"pending" | "empty" | "control" | "live">(
    "pending",
  );
  const lastInsertedKeyRef = useRef<string | null>(null);
  const emptyDebounceRef = useRef<number | null>(null);
  const rafRef = useRef<number | null>(null);

  const cancelEmptyDebounce = useCallback(() => {
    if (emptyDebounceRef.current != null) {
      clearTimeout(emptyDebounceRef.current);
      emptyDebounceRef.current = null;
    }
  }, []);

  const applyBanner = useCallback(
    (braze: BrazeModule) => {
      const el = ref.current;
      if (!el) return;

      const b = braze.getBanner(placementId);

      if (b && !b.isControl) {
        cancelEmptyDebounce();
        const key = bannerContentKey(b);
        if (lastInsertedKeyRef.current === key && el.firstChild) {
          if (aliveRef.current) setMode("live");
          return;
        }
        braze.insertBanner(b, el);
        lastInsertedKeyRef.current = key;
        if (aliveRef.current) setMode("live");
        return;
      }

      if (b?.isControl) {
        cancelEmptyDebounce();
        lastInsertedKeyRef.current = null;
        el.innerHTML = "";
        if (aliveRef.current) setMode("control");
        return;
      }

      cancelEmptyDebounce();
      emptyDebounceRef.current = window.setTimeout(() => {
        emptyDebounceRef.current = null;
        void import("@braze/web-sdk").then((bz) => {
          if (!aliveRef.current || !bz.isInitialized?.()) return;
          const el2 = ref.current;
          if (!el2) return;
          const b2 = bz.getBanner(placementId);
          if (b2 && !b2.isControl) {
            const key2 = bannerContentKey(b2);
            if (lastInsertedKeyRef.current === key2 && el2.firstChild) {
              if (aliveRef.current) setMode("live");
              return;
            }
            bz.insertBanner(b2, el2);
            lastInsertedKeyRef.current = key2;
            if (aliveRef.current) setMode("live");
            return;
          }
          if (b2?.isControl) {
            lastInsertedKeyRef.current = null;
            el2.innerHTML = "";
            if (aliveRef.current) setMode("control");
            return;
          }
          lastInsertedKeyRef.current = null;
          el2.innerHTML = "";
          if (aliveRef.current) setMode("empty");
        });
      }, EMPTY_DEBOUNCE_MS);
    },
    [cancelEmptyDebounce, placementId],
  );

  const run = useCallback(async () => {
    const braze = await import("@braze/web-sdk");
    if (!aliveRef.current) return;
    if (!braze.isInitialized?.() || !ref.current) {
      cancelEmptyDebounce();
      lastInsertedKeyRef.current = null;
      if (aliveRef.current) setMode("empty");
      return;
    }
    applyBanner(braze);
  }, [applyBanner, cancelEmptyDebounce]);

  useEffect(() => {
    lastInsertedKeyRef.current = null;
    cancelEmptyDebounce();
  }, [placementId, cancelEmptyDebounce]);

  useLayoutEffect(() => {
    aliveRef.current = true;
    const scheduleRun = () => {
      if (rafRef.current != null) cancelAnimationFrame(rafRef.current);
      rafRef.current = requestAnimationFrame(() => {
        rafRef.current = null;
        void run();
      });
    };

    void run();
    const onBanners = () => scheduleRun();
    window.addEventListener("braze:banners", onBanners);
    return () => {
      aliveRef.current = false;
      window.removeEventListener("braze:banners", onBanners);
      cancelEmptyDebounce();
      if (rafRef.current != null) cancelAnimationFrame(rafRef.current);
    };
  }, [run, cancelEmptyDebounce]);

  useEffect(() => {
    if (mode !== "live") return;
    queueMicrotask(() => {
      window.dispatchEvent(new Event(DEMOSTORE_DOM_SCAN_EVENT));
    });
  }, [mode, placementId]);

  const sized = variant === "wide" || variant === "square";
  /** Wide/square live: collapse to content height unless square aspect is locked (tile_banner). */
  const intrinsicLiveHeight =
    mode === "live" && sized && !(variant === "square" && lockSquareAspect);
  const slotClass = [
    "braze-banner-slot",
    variant === "wide" && "braze-banner-slot--21",
    variant === "square" && "braze-banner-slot--square",
    lockSquareAspect && "braze-banner-slot--square-locked",
    intrinsicLiveHeight && "braze-banner-slot--intrinsic-height",
    mode === "pending" && variant === "default" && "braze-banner-slot--pending-default",
  ]
    .filter(Boolean)
    .join(" ");

  const ghostClass =
    variant === "wide"
      ? "ghost-slot--fill-banner-21"
      : variant === "square"
        ? "ghost-slot--fill-banner-square"
        : undefined;

  /** Avoid dashed placeholder → live swap on every navigation (obvious flash). */
  const showGhost = mode === "empty" || mode === "control";
  /** Hold 2:1 / 1:1 space while resolving banner; no dashed “empty” UI. */
  const showPendingLayout = mode === "pending" && sized;

  return (
    <div className={slotClass}>
      <div
        ref={ref}
        className={[
          "braze-banner-mount",
          mode === "live" ? "braze-banner-mount--active" : "",
          sized && mode === "live" ? "braze-banner-mount--fill" : "",
        ]
          .filter(Boolean)
          .join(" ")}
      />
      {showPendingLayout ? (
        <div
          className={["braze-banner-pending-fill", ghostClass]
            .filter(Boolean)
            .join(" ")}
          aria-hidden
        />
      ) : null}
      {showGhost ? (
        <BrazeGhostSlot
          title={title}
          placementId={placementId}
          hint={mode === "control" ? "Control banner (no creative)" : undefined}
          className={ghostClass}
        />
      ) : null}
    </div>
  );
}
