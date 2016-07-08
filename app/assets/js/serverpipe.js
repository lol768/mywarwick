import fetch from 'isomorphic-fetch';

export function fetchWithCredentials(url, options = {}) {
  return fetch(url, {
    credentials: 'same-origin',
    ...options,
  });
}

export function postJsonWithCredentials(url, body, options = {}) {
  return fetchWithCredentials(url, {
    method: 'POST',
    body: JSON.stringify(body),
    headers: {
      'Content-Type': 'application/json',
    },
    ...options,
  });
}
