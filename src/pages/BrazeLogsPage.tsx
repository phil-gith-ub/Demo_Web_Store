import { useNavigate } from "react-router-dom";
import { useBrazeLogs } from "../context/BrazeLogContext";

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
            <li key={l.id} className="log-item">
              <div>
                <time dateTime={l.at}>{l.at}</time> · {l.type}
              </div>
              <div>{l.message}</div>
              {l.detail ? <div className="product-meta">{l.detail}</div> : null}
            </li>
          ))}
        </ul>
      )}
    </>
  );
}
