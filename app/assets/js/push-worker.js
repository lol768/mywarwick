/* global clients */

import log from 'loglevel';

// This is mostly to force a serviceworker update if you make a change
// that doesn't change any assets. Increment updateid.
log.debug('push worker (updateid:1)');

// Set the callback for the install step
self.addEventListener('install', () => {
  // Perform install steps
});

self.addEventListener('push', event => {
  log.info('Push event', event);

  if (event.data) {
    const notification = event.data.json();
    log.info('Push event payload', notification);

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
