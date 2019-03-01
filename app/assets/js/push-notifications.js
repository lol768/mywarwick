/* eslint-env browser */

import log from 'loglevel';
import { fetchWithCredentials } from './serverpipe';
import store from './store';
import * as device from './state/device';

function uploadSubscription(subscription) {
  return fetchWithCredentials('/api/push/web/subscribe', {
    method: 'post',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(subscription),
  });
}

// Once the service worker is registered set the initial state
export function init() {
  // Check if push messaging is supported
  if (!('PushManager' in window)) {
    log.warn('Push messaging isn\'t supported');
    return;
  }

  // Are Notifications supported?
  if (!('Notification' in window && 'showNotification' in ServiceWorkerRegistration.prototype)) {
    log.warn('Notifications aren\'t supported');
    return;
  }

  // If the user has disabled notifications
  if (Notification.permission === 'denied') {
    log.warn('The user has disabled notifications');
    return;
  }

  // Do we already have a push message subscription?
  navigator.serviceWorker.ready
    .then(registration => registration.pushManager.getSubscription())
    .then((subscription) => {
      if (!subscription) {
        return null;
      }
      // found subscription, send update to server for fresh timez
      return uploadSubscription(subscription);
    })
    .catch(e => log.warn('Error during getSubscription()', e));
}

export function subscribe() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.ready
      .then(registration => registration.pushManager.subscribe({ userVisibleOnly: true }))
      .then(subscription => uploadSubscription(subscription))
      .catch((e) => {
        if (Notification.permission === 'denied') {
          log.warn('Permission for Notifications was denied', e);
        } else {
          log.error('Unable to subscribe to push', e);
        }
      })
      .then(() => store.dispatch(device.updateNotificationPermissions));
  } else {
    // for browsers not supporting service worker
    Notification.requestPermission(() => {
      store.dispatch(device.updateNotificationPermissions);
    });
  }
}
