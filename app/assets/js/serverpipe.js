/* eslint-env browser */
import fetch from 'isomorphic-fetch';
import { getCsrfHeaderName, getCsrfToken } from './csrfToken';
import QueryString from 'qs';
import log from 'loglevel';

export function isIE11() {
  if (!!window.MSInputMethodContext && !!document.documentMode) {
    log.debug('Running on IE11.');
    return true;
  }
  return false;
}

export function addQsToUrl(url, qs) {
  return `${url}?${QueryString.stringify(qs)}`;
}

export function appendTimeStampToQs(originalQueryString, timeStamp = new Date().valueOf()) {
  return {
    ...originalQueryString,
    ts: timeStamp,
  };
}

export function fetchWithCredentials(url, options = {}, queryString = {}) {
  const headers = 'headers' in options ? options.headers : {};
  const finalQueryString = isIE11() ? appendTimeStampToQs(queryString) : queryString;
  headers[getCsrfHeaderName()] = getCsrfToken();
  return fetch(
    addQsToUrl(url, finalQueryString),
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

