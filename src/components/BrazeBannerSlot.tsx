import { useCallback, useEffect, useRef, useState } from "react";
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
};

/**
 * Renders a Braze banner via `insertBanner` when available; otherwise shows the dashed placeholder.
 * `allowUserSuppliedJavascript: true` is set in `brazeInit` (required for `insertBanner`).
 */
export function BrazeBannerSlot({
  title,
  placementId,
  variant = "default",
}: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const [mode, setMode] = useState<"empty" | "control" | "live">("empty");

  const run = useCallback(async () => {
    const braze = await import("@braze/web-sdk");
    const el = ref.current;
    if (!braze.isInitialized?.() || !el) {
      setMode("empty");
      return;
    }
    const b = braze.getBanner(placementId);
    el.innerHTML = "";
    if (b && !b.isControl) {
      braze.insertBanner(b, el);
      setMode("live");
    } else if (b?.isControl) {
      setMode("control");
    } else {
      setMode("empty");
    }
  }, [placementId]);

  useEffect(() => {
    void run();
    const onBanners = () => void run();
    window.addEventListener("braze:banners", onBanners);
    return () => window.removeEventListener("braze:banners", onBanners);
  }, [run]);

  const sized = variant === "wide" || variant === "square";
  const slotClass = [
    "braze-banner-slot",
    variant === "wide" && "braze-banner-slot--21",
    variant === "square" && "braze-banner-slot--square",
  ]
    .filter(Boolean)
    .join(" ");

  const ghostClass =
    variant === "wide"
      ? "ghost-slot--fill-banner-21"
      : variant === "square"
        ? "ghost-slot--fill-banner-square"
        : undefined;

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
      {mode !== "live" ? (
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
