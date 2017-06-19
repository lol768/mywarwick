const TOKEN_META_ELEMENT_SELECTOR = "meta[name=_csrf]";
const HEADER_META_ELEMENT_SELECTOR = "meta[name=_csrf_header]";
let method = "element";
let data = {};
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
export function getCsrfToken() {
  switch (method) {
    case "element":
      return helperGetMetaElementValue(TOKEN_META_ELEMENT_SELECTOR);
    case "userInfo":
      return data['csrfToken'];
      break;
  }
}

/**
 * @returns {string} The current CSRF header name, to be sent via AJAX.
 * @throws MissingCsrfError If the element cannot be found.
 */
export function getCsrfHeaderName() {
  switch (method) {
    case "element":
      return helperGetMetaElementValue(HEADER_META_ELEMENT_SELECTOR);
    case "userInfo":
      return data['csrfHeader'];
      break;
  }
}

export function setMethod(methodName, additionalData={}) {
  method = methodName;
  switch (methodName) {
    case "element": return;
    case "userInfo":
      console.info("[CSRF] Getting data via userInfo system");
      data = additionalData['user'];
      return;
  }
  throw new Error(`Unknown method for accessing CSRF token, ${methodName}.`)
}

class MissingCsrfError extends Error {
  constructor(message) {
    super(message);
    this.name = 'MissingCsrfError';
  }
}