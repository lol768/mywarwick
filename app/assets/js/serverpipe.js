import fetch from 'isomorphic-fetch';

export function fetchWithCredentials(url, options = {}) {
  return fetch(url, {
    credentials: 'same-origin',
    ...options,
  });
}
