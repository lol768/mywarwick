import fetch from 'isomorphic-fetch';
import { getCsrfHeaderName, getCsrfToken } from './csrfToken';

export function fetchWithCredentials(url, options = {}) {
  const headers = 'headers' in options ? options.headers : {};
  headers[getCsrfHeaderName()] = getCsrfToken();
  return fetch(url, {
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
