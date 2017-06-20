import * as u from './userinfo-base';

const TOKEN_META_ELEMENT_SELECTOR = 'meta[name=_csrf]';
const HEADER_META_ELEMENT_SELECTOR = 'meta[name=_csrf_header]';
let method = 'element';
let data = {};

class MissingCsrfError extends Error {
  constructor(message) {
    super(message);
    this.name = 'MissingCsrfError';
  }
}

/**
 * Helper function to grab value from an element given a selector.
 *
 * @param selector The CSS selector to use.
 * @returns String The value attribute's contents.
 */
function helperGetMetaElementValue(selector) {
  const el = document.querySelector(selector);
  if (el === null) {
    throw new MissingCsrfError(`Couldn't find element ${selector}`);
  }
  return el.getAttribute('content');
}

/**
 * @returns {string} The current CSRF token.
 * @throws MissingCsrfError If the element cannot be found.
 */
export function getCsrfToken() {
  switch (method) {
    case 'element':
      return helperGetMetaElementValue(TOKEN_META_ELEMENT_SELECTOR);
    case 'userInfo':
      return data.csrfToken;
    default:
      throw new Error(`Unknown method for accessing CSRF token, ${method}.`);
  }
}

/**
 * @returns {string} The current CSRF header name, to be sent via AJAX.
 * @throws MissingCsrfError If the element cannot be found.
 */
export function getCsrfHeaderName() {
  switch (method) {
    case 'element':
      return helperGetMetaElementValue(HEADER_META_ELEMENT_SELECTOR);
    case 'userInfo':
      return data.csrfHeader;
    default:
      throw new Error(`Unknown method for accessing CSRF token, ${method}.`);
  }
}

export function setMethod(methodName, additionalData = {}) {
  method = methodName;
  switch (methodName) {
    case 'element': return;
    case 'userInfo':
      data = additionalData.user;
      return;
    default:
      throw new Error(`Unknown method for accessing CSRF token, ${methodName}.`);
  }
}

/**
 * When we're back online, we'll perform an AJAX request
 * to try and grab the CSRF token to prevent future POSTs
 * from failing, which would be bad.
 */
function updateTokenViaAjax() {
  if (method === 'userInfo' && 'csrfHeader' in data) {
    return; // we already have a token
  }

  u.fetchUserInfo().then((responseData) => {
    if (responseData !== null && 'user' in responseData) {
      setMethod('userInfo', responseData);
    }
  });
}

$(() => {
  $(window).on('online', updateTokenViaAjax);
});