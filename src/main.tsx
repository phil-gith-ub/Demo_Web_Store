import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { ErrorBoundary } from "./components/ErrorBoundary";
import { BrazeLogProvider } from "./context/BrazeLogContext";
import { CartProvider } from "./context/CartContext";
import { ProfileProvider } from "./context/ProfileContext";
import "./index.css";

const rawBase = import.meta.env.BASE_URL;
const routerBasename =
  rawBase.length > 1 && rawBase.endsWith("/")
    ? rawBase.slice(0, -1)
    : rawBase;

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <ErrorBoundary>
      <BrowserRouter basename={routerBasename}>
        <BrazeLogProvider>
          <ProfileProvider>
            <CartProvider>
              <App />
            </CartProvider>
          </ProfileProvider>
        </BrazeLogProvider>
      </BrowserRouter>
    </ErrorBoundary>
  </StrictMode>,
);
