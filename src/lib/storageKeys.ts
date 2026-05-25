export const STORAGE = {
  cartItems: "phil_wed_store_cart_items",
  currentUserId: "phil_wed_store_current_user",
  profiles: "phil_wed_store_profiles",
  purchases: "phil_wed_store_purchase_history",
  /** Cached Braze `total_revenue` per user (USD), merged with local orders for lifetime spend / VIP. */
  brazeTotalRevenueByUser: "phil_wed_store_braze_total_revenue",
  brazeLastSent: "phil_wed_store_braze_last_sent",
} as const;
