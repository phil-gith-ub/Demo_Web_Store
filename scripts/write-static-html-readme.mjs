import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const dir = path.join(path.dirname(fileURLToPath(import.meta.url)), "..", "static-html-site");
const text = `Demo Store — static folder (no Vite dev server)
============================================================

Generate or refresh this folder from the project root:

  npm run build:static-html

Open the app
------------
• Double-click index.html, or open it via file:// in the browser.
• Routing uses the hash (e.g. #/store) so navigation works without a server.

Optional: any static file server (Python: python3 -m http.server) also works; use http://localhost:…/index.html

Notes
-----
• This is the same React app, compiled to plain HTML + JS + CSS (not a hand-written vanilla rewrite).
• The dev-only /braze-rest proxy is not available here. REST profile import uses the REST instance URL from Settings; it may be blocked by CORS unless that host allows your origin.
• Re-run npm run build:static-html after source changes; built asset filenames change when content changes.
`;

fs.mkdirSync(dir, { recursive: true });
fs.writeFileSync(path.join(dir, "README.txt"), text, "utf8");
console.log("Wrote static-html-site/README.txt");
