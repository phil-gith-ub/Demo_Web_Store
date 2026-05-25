# Demo Store — Braze Web SDK Integration App

An interactive storefront application built to test, demonstrate, and debug the **Braze Web SDK** in real time. 

---

## 🔒 Security First: No Hardcoded Secrets
This codebase contains **zero hardcoded API keys or secrets**. 
All Braze credentials (Web API key, SDK endpoint, and optional REST key) are configured dynamically through the in-app **Settings** UI and stored securely in your browser's local storage (`localStorage`). This makes it perfectly safe to share and publish on GitHub.

---

## 🚀 Quick Start

Download the **index.html** file from the **static-html-site** directory and open it in your browser.


## Setup & Run

### 1. Install Dependencies
```bash
npm install
```

### 2. Run Locally in Dev Mode
Starts a local dev server with hot module reloading.
```bash
npm run dev
```

### 3. Build for Production
Compiles optimization bundles for deployment.
```bash
npm run build
```

### 4. Build as a Single-File Static HTML Site
Vite is configured to package the entire app (HTML, CSS, JS, and SVG icons) into a **single, self-contained `index.html` file** that can be opened directly in any browser without a web server.
```bash
npm run build:static-html
# Output located in: static-html-site/index.html
```

---

## 📖 How to Use the App

1. **Configure Credentials**: 
   - Click the gear icon (**⚙ Settings**) in the top right corner.
   - Enter your **Web API Key** and **SDK Endpoint** (e.g., `sdk.iam-01.braze.com`).
   - *(Optional)* Add a REST API key and REST URL for importing user profile attributes.
   - Click **Save credentials**.

2. **Identify a User**:
   - Navigate to the **Profile** page.
   - Enter an External User ID (or click "Generate ID").
   - Click **Log in**. This initializes the SDK and associates the session with that user.

3. **Check SDK Logs**:
   - Click the menu icon (**☰ Logs**) in the top-right header to view a live timeline of all Braze SDK events, event triggers, and error messages.

4. **Copy SDK Shortcuts from the Console Page**:
   - Use the dedicated **Console** page in the left sidebar to generate, customize, and copy Braze SDK code snippets.
   - Paste these snippets into your browser's Developer Console to quickly inspect the SDK status, change properties, or manually trigger events!

---

## 📡 Configured Braze Channels & Placements

Use these specific references when configuring campaigns, cards, or flags on your Braze dashboard:

### 1. Banners (In-App Placements)
To render banners at specific places in the store, target these placement IDs:
* **Store Home Page**: `store_page_banner`
* **Shopping Cart**: `cart_banner`
* **Content (Hero Slot)**: `content_banner`
* **Content (Tile Slot)**: `tile_banner`

### 2. Content Cards
To target content card feeds on the **Content** tab, set a Key-Value Pair Extra on your card:
* Key: `location`
* Value: `tile_1` (for Tile Slot 1) or `tile_2` (for Tile Slot 2)

### 3. Feature Flags
To test Feature Flags, create a flag in Braze:
* **VIP Products Access**: `enable_vip_products` (unlocks a secret product selection tab when enabled)

### 4. Deep Links & Web Paths
To redirect users, these scheme URIs (for mobile/native) and web paths are supported:
* **Store page**: `demostore://store` ➔ `/store`
* **Shopping Cart**: `demostore://cart` ➔ `/cart`
* **Profile / Login**: `demostore://profile` ➔ `/profile`
* **VIP products tab**: `demostore://vip` ➔ `/store?vip=1`
* **Purchase History**: `demostore://purchase-history` or `demostore://history` ➔ `/purchase-history`
