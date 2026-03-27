/** Matches Android `ContentScreen` STANDARD_CUSTOM_EVENTS. */
export const CONTENT_PAGE_STANDARD_CUSTOM_EVENTS = [
  "enable_push",
  "push_notification",
  "user_action_button",
  "added_item_to_cart",
  "viewed_vip_products",
  "enabled_dark_mode",
  "logged_in",
  "logged_out",
] as const;

export function isValidSnakeCaseCustomEvent(s: string): boolean {
  return s.length > 0 && /^[a-z0-9_]+$/.test(s);
}
