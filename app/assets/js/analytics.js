/* global ga */

import $ from 'jquery';
import log from 'loglevel';
import Immutable from 'immutable';
import _ from 'lodash';

/* eslint-disable */
(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
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
}

let analyticsQueue = Immutable.List();

let postNextItemThrottled;

function queue(...args) {
  const time = new Date().getTime();
  analyticsQueue = analyticsQueue.push({ args, time });

  postNextItemThrottled();
}

function getTimeSpentInQueue(timeQueued) {
  const time = new Date().getTime();

  return time - timeQueued;
}

function postNextItem() {
  if (!navigator.onLine || analyticsQueue.isEmpty()) {
    return;
  }

  const { args: [command, fields], time } = analyticsQueue.first();
  analyticsQueue = analyticsQueue.shift();

  ga(command, {
    ...fields,
    queueTime: getTimeSpentInQueue(time),
  });

  if (!analyticsQueue.isEmpty()) {
    postNextItemThrottled();
  }
}

$(() => {
  $(window).on('online', postNextItemThrottled);
});

postNextItemThrottled = _.throttle(postNextItem, 500);

export function track(page) {
  if (trackingId) {
    queue('send', { hitType: 'pageview', page });
  }
}
