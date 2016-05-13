import fetch from 'isomorphic-fetch';

export function fetchWithCredentials(url) {
  return fetch(url, {
    credentials: 'same-origin',
  });
}
