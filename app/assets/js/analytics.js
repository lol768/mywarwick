/* global ga */

import $ from 'jquery';
import log from 'loglevel';

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
  ga('create', trackingId, 'auto');
}

export function track(page) {
  if (trackingId) {
    log.debug(`Tracking pageview for ${page}`);
    ga('set', 'page', page);
    ga('send', 'pageview');
  }
}
