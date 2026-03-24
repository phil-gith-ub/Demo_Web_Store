export type Product = {
  id: string;
  name: string;
  price: number;
  description: string;
  category: string;
  isVip: boolean;
};

export type PurchaseOrder = {
  orderId: string;
  userId: string;
  purchaseDate: string;
  products: Product[];
  totalAmount: number;
};
