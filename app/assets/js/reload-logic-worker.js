/* eslint-env browser */
/* global clients */
/* global self */

/**
 * This code ends up in service-worker, so it wants to be as minimal as possible.
 * This includes not importing lots of libraries into it, because they'll all end
 * up in service-worker.js.
 */

self.addEventListener('message', (event) => {
  if (event.data === 'force-activate') {
    self.skipWaiting();
    clients.claim();
    clients.matchAll().then(clients =>
      clients.forEach(client => client.postMessage('reload-window')));
  }
});
