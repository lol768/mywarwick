import log from 'loglevel';

let isPushEnabled = false;

function unsubscriptionToServer() {

}

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
        // TODO: subscribe button should be disabled while we check status of subscription
        if (!subscription) {
          // TODO: subscribe button should be set to FALSE
          return;
        }

        // Found a subscription, send to server to keep up to date
        subscriptionToServer(subscription);

        // TODO: subscribe button should be set to TRUE
      })
      .catch(err => {
        log.warn('Error during getSubscription()', err.stack);
      });
  });
}

function subscribe() {
  // TODO: disable subscription button so it can't be changed while processing subscription

  navigator.serviceWorker.ready.then(serviceWorkerRegistration => {
    serviceWorkerRegistration.pushManager.subscribe({ userVisibleOnly: true })
      .then(subscription => {
        // The subscription was successful
        isPushEnabled = true;
        // TODO: enable subscription button and set to TRUE

        return subscriptionToServer(subscription);
      })
      .catch(e => {
        if (Notification.permission === 'denied') {
          log.warn('Permission for Notifications was denied');
          // TODO: subscribe button should be disabled
        } else {
          log.error('Unable to subscribe to push.', e);
          // TODO: subscribe button should be enabled
        }
      });
  });
}

function unsubscribe() {
  // TODO: disable subscribe button while we process unsubscription

  navigator.serviceWorker.ready.then(serviceWorkerRegistration => {
    // get subscription obj to call unsubscribe() on
    serviceWorkerRegistration.pushManager.getSubscription().then(pushSubscription => {
      // Check we have a subscription to unsubscribe
      if (!pushSubscription) {
        // No subscription object
        // TODO: subscription button enabled and TRUE
        return;
      }

      const subscriptionId = pushSubscription.subscriptionId;
      unsubscriptionToServer(subscriptionId);

      // We have a subscription, so call unsubscribe() on it
      pushSubscription.unsubscribe().then(() => {
        // TODO: enable subscribe button a set FALSE
        isPushEnabled = false;
      }).catch(e => {
        // We failed to unsubscribe, this can lead to
        // an unusual state, so may be best to remove
        // the users data from your data store and
        // inform the user that you have done so

        log.info('Unsubscription error: ', e);
        // pushButton.disabled = false;
        // pushButton.textContent = 'Enable Push Messages';
      });
    }).catch((e) => {
      log.error('Error thrown while unsubscribing from push messaging.', e);
    });
  });
}

export function handlePushSubscribe() {
  if (isPushEnabled) {
    unsubscribe();
  } else {
    subscribe();
  }
}
