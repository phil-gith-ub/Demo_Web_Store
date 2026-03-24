type Props = {
  title: string;
  placementId: string;
  hint?: string;
  /** Extra classes (e.g. aspect-ratio helpers on Content page). */
  className?: string;
};

/** Dashed placeholder for a Braze banner or content-card region. */
export function BrazeGhostSlot({ title, placementId, hint, className }: Props) {
  return (
    <div
      className={["ghost-slot", className].filter(Boolean).join(" ")}
      data-placement-id={placementId}
    >
      <span className="ghost-slot-label">{title}</span>
      <code className="ghost-slot-code">{placementId}</code>
      {hint ? <span className="ghost-slot-meta">{hint}</span> : null}
    </div>
  );
}
