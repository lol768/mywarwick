import { fetchWithCredentials } from '../serverpipe';
import log from 'loglevel';

export const API_BASE = '/api/news/images';

export function put(file) {
  const data = new FormData();
  data.append('image', file);

  return fetchWithCredentials(API_BASE, {
    method: 'POST',
    body: data,
  })
    .then(response => response.json())
    .then(json => {
      if (json.success) {
        return json.data;
      }

      throw new Error(json.errors[0].message);
    })
    .catch(e => {
      log.error(e);
      throw e;
    });
}

