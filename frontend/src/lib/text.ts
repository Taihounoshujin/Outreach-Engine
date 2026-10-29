/**
 * Turns an HTML email body into plain text for previews.
 *
 * DOMParser builds an inert document: scripts don't run, images and other
 * resources aren't fetched, event handlers never fire. We only read its
 * textContent and render that as ordinary React text, so even a template full
 * of hostile markup shows up as harmless characters. This is the safe way to
 * preview stored HTML — never dangerouslySetInnerHTML.
 */
export function htmlToPlainText(html: string): string {
  const doc = new DOMParser().parseFromString(html, 'text/html');
  return (doc.body.textContent ?? '').replace(/\s+/g, ' ').trim();
}

export function truncate(text: string, max: number): string {
  return text.length <= max ? text : `${text.slice(0, max - 1).trimEnd()}…`;
}