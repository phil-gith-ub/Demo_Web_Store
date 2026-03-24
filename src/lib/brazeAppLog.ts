import type { BrazeLogEntry } from "../context/BrazeLogContext";

type Sink = ((entry: Omit<BrazeLogEntry, "id" | "at">) => void) | null;

let sink: Sink = null;

export function registerBrazeAppLogSink(fn: Sink) {
  sink = fn;
}

export function brazeAppLog(entry: Omit<BrazeLogEntry, "id" | "at">) {
  sink?.(entry);
}
