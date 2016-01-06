// Set the callback for the install step
self.addEventListener('install', function (event) {
  // Perform install steps
});

self.addEventListener('push', function (event) {
  event.waitUntil(
    self.registration.pushManager.getSubscription().then(function (subscription) {
      fetch('/api/push/notification', {
        method: 'post',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(subscription),
        credentials: 'same-origin'
      })
        .then(function (response) {
          return response.json();
        })
        .then(function (data) {
          data.map((notification) =>
            self.registration.showNotification(notification.title, {
              body: notification.body,
              icon: notification.icon || '/assets/images/notification-icon.png'
            })
          )
        })
        .catch(function (err) {
          console.log(`Error: ${err}`);
        });
    })
  );
});

self.addEventListener('message', function (event) {
  self.token = event.data.token;
});
