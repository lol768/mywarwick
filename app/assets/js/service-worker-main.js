// Set the callback for the install step
self.addEventListener('install', event => {
  // Perform install steps
});

self.addEventListener('push', event => {
  function showNotification(title, body) {
    self.registration.showNotification(title, {
      body: body,
      icon: '/assets/images/notification-icon.png'
    });
  }

  event.waitUntil(
    self.registration.pushManager.getSubscription()
      .then(
        subscription =>
          fetch('/api/push/gcm/notification', {
            method: 'post',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(subscription),
            credentials: 'same-origin'
          }))
      .then(response => {
        if (response.status === 200) {
          return response.json();
        } else if (response.status === 401) {
          // Unauthorized; user is no longer signed in so unregister the service worker
          // Must still show a notification to avoid update message
          showNotification('Start.Warwick', 'Your Start.Warwick session has expired');
          self.registration.unregister();
          throw new Error('User session expired');
        } else {
          throw new Error('Unexpected response status ' + response.status);
        }
      })
      .then(data => {
        if (data.length == 0) {
          // No notifications to display (avoid generic 'start.warwick.ac.uk has updated in the background')
          showNotification('Start.Warwick', 'You have new notifications');
        } else {
          data.map(notification =>
            self.registration.showNotification(notification.title, {
              body: notification.body,
              icon: notification.icon || '/assets/images/notification-icon.png'
            })
          )
        }
      })
      .catch(err => console.error(err))
  );
});

self.addEventListener('message', event => {
  self.token = event.data.token;
});
