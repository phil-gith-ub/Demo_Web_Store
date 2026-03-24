import { useCallback, useEffect, useRef, useState } from "react";
import { BrazeGhostSlot } from "./BrazeGhostSlot";

type Props = {
  title: string;
  placementId: string;
};

/**
 * Renders a Braze banner via `insertBanner` when available; otherwise shows the dashed placeholder.
 * `allowUserSuppliedJavascript: true` is set in `brazeInit` (required for `insertBanner`).
 */
export function BrazeBannerSlot({ title, placementId }: Props) {
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

  return (
    <div className="braze-banner-slot">
      <div
        ref={ref}
        className={
          mode === "live" ? "braze-banner-mount braze-banner-mount--active" : "braze-banner-mount"
        }
      />
      {mode !== "live" ? (
        <BrazeGhostSlot
          title={title}
          placementId={placementId}
          hint={mode === "control" ? "Control banner (no creative)" : undefined}
        />
      ) : null}
    </div>
  );
}
