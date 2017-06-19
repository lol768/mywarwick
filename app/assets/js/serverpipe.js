import fetch from 'isomorphic-fetch';
import {getCsrfHeaderNameFromPage, getCsrfTokenFromPage} from "./csrfToken";

export function fetchWithCredentials(url, options = {}) {
  let headers = "headers" in options ? options["headers"] : {};
  headers[getCsrfHeaderNameFromPage()] = getCsrfTokenFromPage();
  return fetch(url, {
    credentials: 'same-origin',
    headers: headers,
    ...options,
  });
}

export function postJsonWithCredentials(url, body, options = {}) {
  const defaultHeaders = {'Content-Type': 'application/json'};
  let headers = "headers" in options ? Object.assign(options['headers'], defaultHeaders) : defaultHeaders;

  headers[getCsrfHeaderNameFromPage()] = getCsrfTokenFromPage();
  return fetchWithCredentials(url, {
    method: 'POST',
    body: JSON.stringify(body),
    headers: headers,
    ...options,
  });
}
