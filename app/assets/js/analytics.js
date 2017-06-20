/* global ga */

import localforage from 'localforage';
import $ from 'jquery';
import log from 'loglevel';
import _ from 'lodash-es';
import store from './store';

const MAX_ITEMS_IN_QUEUE = 100;
const QUEUE_STORAGE_KEY = 'gaQueue';

/* eslint-disable */
(function (i, s, o, g, r, a, m) {
  i['GoogleAnalyticsObject'] = r;
  i[r] = i[r] || function () {
      (i[r].q = i[r].q || []).push(arguments)
    }, i[r].l = 1 * new Date();
  a = s.createElement(o),
    m = s.getElementsByTagName(o)[0];
  a.async = 1;
  a.src = g;
  a.onerror = (e) => {
     // NEWSTART-713
    log.info('Caught error loading analytics.js', e);
    e.stopPropagation();
  };
  if (m) m.parentNode.insertBefore(a, m)
})(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');
/* eslint-enable */

const trackingId = $('#app-container').attr('data-analytics-tracking-id');

if (trackingId === undefined) {
  log.warn('Google Analytics is not configured.  In application.conf, set ' +
    'start.analytics.tracking-id to the tracking ID for this property, e.g. UA-XXXXXXXX-X.');
} else {
  log.info(`Google Analytics tracker created with tracking ID ${trackingId}`);

  ga('create', {
    trackingId,
    cookieDomain: 'auto',
  });

  ga(tracker => {
    const clientId = tracker.get('clientId');

    store.dispatch({
      type: 'analytics.client-id',
      payload: clientId,
    });
  });
}

/**
 * Grabs a (truncated) stored analytics queue if there is one,
 * or just an empty array.
 */
function getQueueFromLocalStorage() {
  localforage.getItem(QUEUE_STORAGE_KEY).then(storageItem => {
    if (storageItem !== null && Array.isArray(storageItem)) {
      analyticsQueue = [...storageItem, ...analyticsQueue].slice(-100);
    }
  });
}

let analyticsQueue = [];

let postNextItemThrottled;

let isReady = false;

function queue(...args) {
  const time = _.now();
  analyticsQueue = [
    ...analyticsQueue,
    { args, time },
  ];

  if (analyticsQueue.length === MAX_ITEMS_IN_QUEUE) {
    analyticsQueue = analyticsQueue.shift();
  }

  if (isReady) {
    postNextItemThrottled();
  }
}

function getTimeSpentInQueue(timeQueued) {
  return _.now() - timeQueued;
}

/**
 * Encodes the queue as JSON, and stores to localStorage.
 */
function persistAnalyticsQueue() {
  return localforage.setItem(QUEUE_STORAGE_KEY, analyticsQueue).then(d => {
    log.info("Persist finished for GA queue");
  });
}

function postNextItem() {
  if (!analyticsQueue.length) {
    return;
  }

  if (!navigator.onLine) {
    persistAnalyticsQueue(); // fire and forget
    return;
  }

  const { args: [command, fields], time } = analyticsQueue[0];
  analyticsQueue = analyticsQueue.slice(1);

  // ensure items get cleared out of the queue
  persistAnalyticsQueue().then(param => {
    ga(command, {
      ...fields,
      queueTime: getTimeSpentInQueue(time),
    });

    if (analyticsQueue.length) {
      postNextItemThrottled();
    }
  });
}

$(() => {
  getQueueFromLocalStorage();
  if (isReady) {
    $(window).on('online', postNextItemThrottled);
  }
});

postNextItemThrottled = _.throttle(postNextItem, 500); // eslint-disable-line prefer-const

export function track(page) {
  if (trackingId) {
    queue('send', { hitType: 'pageview', page });
  }
}

export function setUserId(userId) {
  ga('set', 'userId', userId);
}

export function setDimension(index, value) {
  if (value !== null) {
    ga('set', `dimension${index}`, value);
  }
}

export function ready() {
  isReady = true;

  postNextItemThrottled();
}
