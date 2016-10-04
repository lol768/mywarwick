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
  function showNotification(title, body) {
    self.registration.showNotification(title, {
      body,
      icon: '/assets/images/notification-icon.png',
    });
  }

  event.waitUntil(
    self.registration.pushManager.getSubscription()
      .then(
        subscription =>
          fetch('/api/push/gcm/notification', {
            method: 'post',
            headers: {
              Accept: 'application/json',
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(subscription),
            credentials: 'same-origin',
          })
      )
      .then(response => {
        if (response.status === 200) {
          return response.json();
        } else if (response.status === 401) {
          // Unauthorized; user is no longer signed in so unregister the service worker
          // Must still show a notification to avoid update message
          showNotification('My Warwick', 'You are no longer signed in to My Warwick');
          self.registration.unregister();
          throw new Error('User session expired');
        } else {
          throw new Error(`Unexpected response status ${response.status}`);
        }
      })
      .then(data => {
        if (data.length === 0) {
          // No notifications to display
          // (avoid generic 'my warwick.ac.uk has updated in the background')
          showNotification('My Warwick', 'You have new notifications');
        } else {
          data.map(notification =>
            self.registration.showNotification(notification.title, {
              body: notification.body,
              icon: notification.icon || '/assets/images/notification-icon.png',
            })
          );
        }
      })
      .catch(err => log.error(err))
  );
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
