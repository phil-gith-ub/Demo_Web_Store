import type { Product } from "../types/product";

export const mockProducts: Product[] = [
  {
    id: "1",
    name: "Wireless Headphones",
    price: 79.99,
    description:
      "Premium noise-cancelling wireless headphones with 30-hour battery life",
    category: "Electronics",
    isVip: false,
  },
  {
    id: "2",
    name: "Smart Watch",
    price: 249.99,
    description:
      "Fitness tracking smartwatch with heart rate monitor and GPS",
    category: "Electronics",
    isVip: true,
  },
  {
    id: "3",
    name: "Coffee Maker",
    price: 89.99,
    description: "Programmable 12-cup coffee maker with thermal carafe",
    category: "Appliances",
    isVip: false,
  },
  {
    id: "4",
    name: "Running Shoes",
    price: 129.99,
    description:
      "Lightweight running shoes with cushioned sole for maximum comfort",
    category: "Clothing",
    isVip: false,
  },
  {
    id: "5",
    name: "Backpack",
    price: 59.99,
    description: "Durable waterproof backpack with laptop compartment",
    category: "Accessories",
    isVip: false,
  },
  {
    id: "6",
    name: "Bluetooth Speaker",
    price: 49.99,
    description:
      "Portable waterproof Bluetooth speaker with 360-degree sound",
    category: "Electronics",
    isVip: false,
  },
  {
    id: "7",
    name: "Yoga Mat",
    price: 34.99,
    description: "Non-slip eco-friendly yoga mat with carrying strap",
    category: "Fitness",
    isVip: false,
  },
  {
    id: "8",
    name: "Desk Lamp",
    price: 39.99,
    description:
      "LED desk lamp with adjustable brightness and color temperature",
    category: "Home & Office",
    isVip: false,
  },
  {
    id: "9",
    name: "Premium Laptop",
    price: 1299.99,
    description: "High-performance laptop with 16GB RAM and 1TB SSD",
    category: "Electronics",
    isVip: true,
  },
  {
    id: "10",
    name: "Designer Watch",
    price: 599.99,
    description: "Luxury designer watch with premium materials",
    category: "Accessories",
    isVip: true,
  },
];

export function getProductById(id: string): Product | undefined {
  return mockProducts.find((p) => p.id === id);
}
