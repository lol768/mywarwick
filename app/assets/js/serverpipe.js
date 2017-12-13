/* eslint-env browser */
import fetch from 'isomorphic-fetch';
import { getCsrfHeaderName, getCsrfToken } from './csrfToken';
import log from 'loglevel';
import Url from 'url';

function isIE11() {
  if (!!window.MSInputMethodContext && !!document.documentMode) {
    log.debug('Running on IE11.');
    return true;
  }
  return false;
}

export function addQsToUrl(url, qs) {
  const parsedUrl = Url.parse(url, true);
  parsedUrl.search = null;
  parsedUrl.query = {
    ...parsedUrl.query,
    ...qs,
  };
  return Url.format(parsedUrl);
}

export function generateTimeStampQsForUrl(timeStamp = new Date().valueOf()) {
  return {
    ts: timeStamp,
  };
}

export function fetchWithCredentials(url, options = {}) {
  const headers = 'headers' in options ? options.headers : {};
  headers[getCsrfHeaderName()] = getCsrfToken();
  return fetch(
    isIE11() ? addQsToUrl(url, generateTimeStampQsForUrl()) : url,
    {
      credentials: 'same-origin',
      headers,
      ...options,
    });
}

export function postJsonWithCredentials(url, body, options = {}) {
  const defaultHeaders = { 'Content-Type': 'application/json' };
  const headers = 'headers' in options ? [...options.headers, defaultHeaders] : defaultHeaders;

  headers[getCsrfHeaderName()] = getCsrfToken();
  return fetchWithCredentials(url, {
    method: 'POST',
    body: JSON.stringify(body),
    headers,
    ...options,
  });
}

