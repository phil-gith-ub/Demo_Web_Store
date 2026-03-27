import { useMemo, useState } from "react";
import { useProfile } from "../context/ProfileContext";
import {
  CONTENT_PAGE_STANDARD_CUSTOM_EVENTS,
  isValidSnakeCaseCustomEvent,
} from "../lib/contentPageCustomEvents";
import { logBrazeCustomEvent } from "../lib/brazeUserSyncWeb";

type Props = {
  open: boolean;
  onClose: () => void;
  onSent: (success: boolean) => void;
};

const STANDARD = [...CONTENT_PAGE_STANDARD_CUSTOM_EVENTS];

export function ContentCustomEventDialog({ open, onClose, onSent }: Props) {
  const { profile, updateProfile } = useProfile();
  const [selection, setSelection] = useState<string>("");
  const [newEventName, setNewEventName] = useState("");

  const storedEvents = useMemo(
    () =>
      (profile?.customEvents ?? []).filter(
        (e) => !(STANDARD as readonly string[]).includes(e),
      ),
    [profile?.customEvents],
  );

  const optionValues = useMemo(
    () => ["__new__", ...STANDARD, ...storedEvents],
    [storedEvents],
  );

  const optionLabels = useMemo(
    () => ["New Custom Event", ...STANDARD, ...storedEvents],
    [storedEvents],
  );

  const isNew = selection === "__new__";
  const trimmed = newEventName.trim();
  const newValid =
    trimmed.length === 0 || isValidSnakeCaseCustomEvent(trimmed);
  const canSend =
    selection !== "" &&
    (isNew ? isValidSnakeCaseCustomEvent(trimmed) : true);

  const send = async () => {
    if (!canSend) return;
    const eventName = isNew ? trimmed : selection;
    const ok = await logBrazeCustomEvent(eventName);
    if (
      ok &&
      isNew &&
      !(STANDARD as readonly string[]).includes(eventName) &&
      profile &&
      !profile.customEvents.includes(eventName)
    ) {
      updateProfile({ customEvents: [...profile.customEvents, eventName] });
    }
    setSelection("");
    setNewEventName("");
    onSent(ok);
  };

  if (!open) return null;

  return (
    <div
      className="content-dialog-backdrop"
      role="presentation"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div
        className="content-dialog"
        role="dialog"
        aria-labelledby="content-custom-event-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="content-custom-event-title" className="content-dialog-title">
          User action
        </h2>
        <label className="form-row">
          <span>Select custom event</span>
          <select
            className="input"
            value={selection}
            onChange={(e) => {
              const v = e.target.value;
              setSelection(v);
              if (v !== "__new__") setNewEventName("");
            }}
          >
            <option value="">Select custom event</option>
            {optionValues.map((v, i) => (
              <option key={v + i} value={v}>
                {optionLabels[i]}
              </option>
            ))}
          </select>
        </label>
        {isNew ? (
          <label className="form-row" style={{ marginTop: "0.75rem" }}>
            <span>Create new custom event (snake_case)</span>
            <input
              className="input"
              type="text"
              value={newEventName}
              onChange={(e) => setNewEventName(e.target.value)}
              placeholder="example_event"
              autoComplete="off"
            />
            {!newValid && newEventName.length > 0 ? (
              <span className="content-dialog-error">
                Only snake_case is allowed. Example: example_event
              </span>
            ) : null}
          </label>
        ) : null}
        <div className="content-dialog-actions">
          <button type="button" className="btn btn-ghost" onClick={onClose}>
            Cancel
          </button>
          <button
            type="button"
            className="btn btn-primary"
            disabled={!canSend}
            onClick={() => void send()}
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
}
