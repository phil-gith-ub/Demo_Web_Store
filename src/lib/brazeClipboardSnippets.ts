/**
 * Wrap Braze SDK calls for pasting into the browser console (IIFE avoids `const` redeclare errors).
 */
export function wrapBrazeConsoleSnippet(statementLines: string[]): string {
  const body = statementLines.map((line) => `  ${line}`).join("\n");
  return `// Paste into browser console, then press Enter
;(async () => {
  const braze = await import("@braze/web-sdk");
${body}
})();`;
}
