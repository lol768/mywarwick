/* global clients */

/**
 * This code ends up in service-worker, so it wants to be as minimal as possible.
 * This includes not importing lots of libraries into it, because they'll all end
 * up in service-worker.js.
 */

// deliberately not using a logging library here, for conciseness
function log(...args) {
  console.log(...args); // eslint-disable-line no-console
}

// This is mostly to force a serviceworker update if you make a change
// that doesn't change any assets. Increment updateid.
log('push worker (updateid:1)');

// Set the callback for the install step
self.addEventListener('install', () => {
  // Perform install steps
});

self.addEventListener('push', event => {
  log('Push event', event);

  if (event.data) {
    const notification = event.data.json();
    log('Push event payload', notification);

    self.registration.showNotification(notification.title, {
      body: notification.text,
      icon: '/assets/images/notification-icon.png',
    });
  }
});

self.addEventListener('message', event => {
  self.token = event.data.token;
});

self.addEventListener('notificationclick', event => {
  event.notification.close();

  event.waitUntil(
    clients.matchAll({
      type: 'window',
    }).then(clientList => {
      clientList.forEach(client => {
        if (/\/notifications$/i.test(client.url) && 'focus' in client) {
          return client.focus();
        }

        return null;
      });
      if (clients.openWindow) {
        return clients.openWindow('/notifications');
      }

      return null;
    })
  );
});
