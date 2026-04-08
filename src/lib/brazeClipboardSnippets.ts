/**
 * Wrap statements so pasting into DevTools works without a global `braze` binding.
 * No leading comment — only executable code.
 */
export function wrapBrazeForConsolePaste(statementLines: string[]): string {
  const body = statementLines.map((line) => `  ${line}`).join("\n");
  return `;(async () => {
  const braze = await import("@braze/web-sdk");
${body}
})();`;
}
