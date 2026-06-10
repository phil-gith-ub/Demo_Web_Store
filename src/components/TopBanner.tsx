import { useNavigate } from "react-router-dom";

type Props = {
  unviewedCount: number;
  onNotificationClick: () => void;
};

export function TopBanner({ unviewedCount, onNotificationClick }: Props) {
  const navigate = useNavigate();

  return (
    <header className="app-header">
      <div className="app-header-nav">
        <button
          type="button"
          className="icon-btn header-icon-btn"
          aria-label="Braze SDK logs"
          onClick={() => navigate("/logs")}
          title="Logs"
        >
          ☰
        </button>
      </div>
      <h1>Demo Store</h1>
      <div className="app-header-actions">
        <button
          type="button"
          className="icon-btn header-icon-btn header-bell-btn"
          aria-label="Notifications"
          onClick={onNotificationClick}
          title="Notifications"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="bell-svg" aria-hidden>
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          {unviewedCount > 0 && (
            <span className="bell-badge" aria-label={`${unviewedCount} unread`}>
              {unviewedCount}
            </span>
          )}
        </button>
        <button
          type="button"
          className="icon-btn header-icon-btn"
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

