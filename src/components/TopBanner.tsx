import { useNavigate } from "react-router-dom";

export function TopBanner() {
  const navigate = useNavigate();

  return (
    <header className="app-header">
      <button
        type="button"
        className="icon-btn"
        aria-label="Braze SDK logs"
        onClick={() => navigate("/logs")}
        title="Logs"
      >
        ☰
      </button>
      <h1>Demo Store</h1>
      <div className="app-header-actions">
        <button
          type="button"
          className="icon-btn"
          aria-label="Settings"
          onClick={() => navigate("/settings")}
          title="Settings"
        >
          ⚙
        </button>
      </div>
    </header>
  );
}
