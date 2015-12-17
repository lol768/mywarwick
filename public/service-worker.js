
// Set the callback for the install step
self.addEventListener('install', function(event) {
  // Perform install steps
});

self.addEventListener('push', function(event) {
  console.log('Received a push message', event);

  var title = 'Yay a message.';
  var body = 'We have received a push message.';
  var icon = '/images/icon-192x192.png';
  var tag = 'simple-push-demo-notification-tag';

  event.waitUntil(
    self.registration.showNotification(title, {
      body: body,
      icon: icon,
      tag: tag
    })
  );
});

//Proj num: 322372754464
//API key:  AIzaSyAmgkYWgUKgMnPa8Cw1N7-rBHN61MupSvw
