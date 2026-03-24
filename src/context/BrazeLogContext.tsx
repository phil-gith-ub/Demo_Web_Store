import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";

export type BrazeLogEntry = {
  id: string;
  at: string;
  type: "info" | "request" | "response" | "event" | "error";
  message: string;
  detail?: string;
};

type BrazeLogContextValue = {
  logs: BrazeLogEntry[];
  pushLog: (entry: Omit<BrazeLogEntry, "id" | "at">) => void;
  clearLogs: () => void;
};

const BrazeLogContext = createContext<BrazeLogContextValue | null>(null);

export function BrazeLogProvider({ children }: { children: ReactNode }) {
  const [logs, setLogs] = useState<BrazeLogEntry[]>([]);

  const pushLog = useCallback((entry: Omit<BrazeLogEntry, "id" | "at">) => {
    const full: BrazeLogEntry = {
      ...entry,
      id: crypto.randomUUID(),
      at: new Date().toISOString(),
    };
    setLogs((prev) => [full, ...prev].slice(0, 500));
  }, []);

  const clearLogs = useCallback(() => setLogs([]), []);

  const value = useMemo(
    () => ({ logs, pushLog, clearLogs }),
    [logs, pushLog, clearLogs],
  );

  return (
    <BrazeLogContext.Provider value={value}>{children}</BrazeLogContext.Provider>
  );
}

export function useBrazeLogs() {
  const ctx = useContext(BrazeLogContext);
  if (!ctx) throw new Error("useBrazeLogs outside BrazeLogProvider");
  return ctx;
}
