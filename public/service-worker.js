// Set the callback for the install step
self.addEventListener('install', function (event) {
  // Perform install steps
});

self.addEventListener('push', function(event) {
  event.waitUntil(
    self.registration.pushManager.getSubscription().then(function(subscription) {
      fetch('/api/push/notification', {
        method: 'post',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(subscription),
        credentials: 'same-origin'
      })
        .then(function(response) {
          console.log(response);
          return response.json(); })
        .then(function(data) {
          self.registration.showNotification(data.title, {
            body: data.body,
            icon: data.icon || 'favicon-196x196.png'
          });
        })
        .catch(function(err) {
          console.log(`Error: ${err}`);
        });
    })
  );
});

self.addEventListener('message', function(event) {
  self.token = event.data.token;
});
