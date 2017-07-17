/* eslint-env browser */

export default function isEmbedded() {
  return window !== window.parent;
}
