import log from 'loglevel';

function subscriptionToServer(subscription) {
  fetch('/api/push/gcm/subscribe', {
    method: 'post',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(subscription),
    credentials: 'same-origin',
  });
}

// Once the service worker is registered set the initial state
export function init() {
  // Are Notifications supported in the service worker?
  if (!('Notification' in window || 'showNotification' in ServiceWorkerRegistration.prototype)) {
    log.warn('Notifications aren\'t supported.');
    return;
  }

// If the user has disabled notifications
  if (Notification.permission === 'denied') {
    log.warn('The user has disabled notifications.');
    return;
  }

  // Check if push messaging is supported
  if (!('PushManager' in window)) {
    log.warn('Push messaging isn\'t supported.');
    return;
  }

  navigator.serviceWorker.ready.then(serviceWorkerRegistration => {
    // Do we already have a push message subscription?
    serviceWorkerRegistration.pushManager.getSubscription()
      .then(subscription => {
        if (!subscription) {
          return;
        }
        // found subscription, send update to server for fresh timez
        subscriptionToServer(subscription);
      })
      .catch(err => {
        log.warn('Error during getSubscription()', err);
      });
  });
}

export function subscribe() {
  navigator.serviceWorker.ready.then(serviceWorkerRegistration => {
    serviceWorkerRegistration.pushManager.subscribe({ userVisibleOnly: true })
      .then(subscriptionToServer)
      .catch(e => {
        if (Notification.permission === 'denied') {
          log.warn('Permission for Notifications was denied');
        } else {
          log.error('Unable to subscribe to push.', e);
        }
      });
  });
}
