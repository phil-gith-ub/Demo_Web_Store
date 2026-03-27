import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { BrazeLogEntry } from "../context/BrazeLogContext";
import { useBrazeLogs } from "../context/BrazeLogContext";

/** Above this length, or any newline → collapsed by default until expanded. */
const COLLAPSE_THRESHOLD = 200;
const PREVIEW_LINE_MAX = 140;

function needsCollapse(text: string | undefined): boolean {
  if (!text) return false;
  return text.length > COLLAPSE_THRESHOLD || text.includes("\n");
}

/** One-line-ish preview for collapsed state. */
function previewText(text: string): string {
  const firstLine = text.split("\n")[0] ?? "";
  const extraLines = text.includes("\n") ? text.split("\n").length - 1 : 0;
  let out =
    firstLine.length > PREVIEW_LINE_MAX
      ? `${firstLine.slice(0, PREVIEW_LINE_MAX)}…`
      : firstLine;
  if (extraLines > 0) {
    out += ` (+${extraLines} more line${extraLines === 1 ? "" : "s"})`;
  }
  return out || "…";
}

function LogEntryRow({ entry: l }: { entry: BrazeLogEntry }) {
  const [expanded, setExpanded] = useState(false);
  const collapseMessage = needsCollapse(l.message);
  const collapseDetail = needsCollapse(l.detail);
  const collapsible = collapseMessage || collapseDetail;

  return (
    <li className="log-item">
      <div className="log-item-meta">
        <span>
          <time dateTime={l.at}>{l.at}</time> · {l.type}
        </span>
        {collapsible ? (
          <button
            type="button"
            className="btn btn-ghost btn-small log-item-expand"
            aria-expanded={expanded}
            aria-label={expanded ? "Collapse log payload" : "Expand log payload"}
            onClick={() => setExpanded((e) => !e)}
          >
            {expanded ? "Collapse" : "Expand"}
          </button>
        ) : null}
      </div>
      {collapsible && !expanded ? (
        <>
          <div className="log-item-preview">
            {collapseMessage ? previewText(l.message) : l.message}
          </div>
          {l.detail ? (
            <div className="log-item-preview product-meta">
              {collapseDetail ? previewText(l.detail) : l.detail}
            </div>
          ) : null}
        </>
      ) : (
        <>
          {collapsible ? (
            <pre className="log-item-payload">{l.message}</pre>
          ) : (
            <div>{l.message}</div>
          )}
          {l.detail ? (
            collapsible ? (
              <pre className="log-item-payload log-item-payload--detail">{l.detail}</pre>
            ) : (
              <div className="product-meta">{l.detail}</div>
            )
          ) : null}
        </>
      )}
    </li>
  );
}

export function BrazeLogsPage() {
  const navigate = useNavigate();
  const { logs, clearLogs } = useBrazeLogs();

  const copyAll = () => {
    const text = logs
      .map(
        (l) =>
          `[${l.at}] ${l.type.toUpperCase()}: ${l.message}${l.detail ? ` — ${l.detail}` : ""}`,
      )
      .join("\n");
    void navigator.clipboard.writeText(text);
  };

  return (
    <>
      <h1 className="page-title">Braze SDK logs</h1>
      <p style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }}>
        <button type="button" className="btn btn-ghost" onClick={() => navigate(-1)}>
          ← Close
        </button>
        <button type="button" className="btn btn-ghost" onClick={copyAll}>
          Copy all
        </button>
        <button type="button" className="btn btn-ghost" onClick={clearLogs}>
          Clear
        </button>
      </p>
      {logs.length === 0 ? (
        <p className="product-meta">No log entries yet.</p>
      ) : (
        <ul className="log-list">
          {logs.map((l) => (
            <LogEntryRow key={l.id} entry={l} />
          ))}
        </ul>
      )}
    </>
  );
}
