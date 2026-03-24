type Props = {
  title: string;
  placementId: string;
  hint?: string;
};

/** Dashed placeholder for a Braze banner or content-card region. */
export function BrazeGhostSlot({ title, placementId, hint }: Props) {
  return (
    <div className="ghost-slot" data-placement-id={placementId}>
      <span className="ghost-slot-label">{title}</span>
      <code className="ghost-slot-code">{placementId}</code>
      {hint ? <span className="ghost-slot-meta">{hint}</span> : null}
    </div>
  );
}
