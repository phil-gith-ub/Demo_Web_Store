import { Component, type ErrorInfo, type ReactNode } from "react";

type Props = { children: ReactNode };

type State = { error: Error | null };

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("App error:", error, info.componentStack);
  }

  render() {
    if (this.state.error) {
      return (
        <div
          style={{
            fontFamily: "system-ui, sans-serif",
            padding: "2rem",
            maxWidth: 560,
            margin: "0 auto",
          }}
        >
          <h1 style={{ marginTop: 0 }}>Something broke</h1>
          <p>
            Open DevTools → <strong>Console</strong> for the full stack. Common
            causes: opening the app as a <code>file://</code> URL (use{" "}
            <code>npm run dev</code> and the <code>http://localhost:…</code>{" "}
            link), or a browser extension blocking scripts.
          </p>
          <pre
            style={{
              background: "#f4f4f5",
              padding: "1rem",
              overflow: "auto",
              fontSize: 13,
            }}
          >
            {this.state.error.message}
          </pre>
        </div>
      );
    }
    return this.props.children;
  }
}
