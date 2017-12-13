import fetch from 'isomorphic-fetch';
import { getCsrfHeaderName, getCsrfToken } from './csrfToken';
import QueryString from 'query-string';

export function fetchWithCredentials(url, options = {}, queryString) {
  const headers = 'headers' in options ? options.headers : {};
  headers[getCsrfHeaderName()] = getCsrfToken();
  return fetch(
    queryString ? addQsToUrl(url, queryString) : url,
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

export function addQsToUrl(url, qs) {
  return `${url}?${QueryString.stringify(qs)}`;
}