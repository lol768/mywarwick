const TOKEN_META_ELEMENT_SELECTOR = "meta[name=_csrf]";
const HEADER_META_ELEMENT_SELECTOR = "meta[name=_csrf_header]";

/**
 * Helper function to grab value from an element given a selector.
 *
 * @param selector The CSS selector to use.
 * @returns String The value attribute's contents.
 */
function helperGetMetaElementValue(selector) {
  let el = document.querySelector(selector);
  if (el === null) {
    throw new MissingCsrfError(`Couldn't find element ${selector}`)
  }
  return el.getAttribute("content");
}

/**
 * @returns {string} The current CSRF token.
 * @throws MissingCsrfError If the element cannot be found.
 */
export function getCsrfTokenFromPage() {
  return helperGetMetaElementValue(TOKEN_META_ELEMENT_SELECTOR);
}

/**
 * @returns {string} The current CSRF header name, to be sent via AJAX.
 * @throws MissingCsrfError If the element cannot be found.
 */
export function getCsrfHeaderNameFromPage() {
  return helperGetMetaElementValue(HEADER_META_ELEMENT_SELECTOR);
}

class MissingCsrfError extends Error {
  constructor(message) {
    super(message);
    this.name = 'MissingCsrfError';
  }
}