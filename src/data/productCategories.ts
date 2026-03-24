/**
 * Favorite product category options on Profile (matches Android `ProfileScreen` dropdown).
 */
export const PROFILE_FAVORITE_CATEGORIES = [
  "Electronics",
  "Clothing",
  "Appliances",
  "Accessories",
  "Fitness",
] as const;

export type ProfileFavoriteCategory = (typeof PROFILE_FAVORITE_CATEGORIES)[number];
