/* global ga */

import log from 'loglevel';

export function track(page) {
  log.info(`Tracking pageview for ${page}`);
  ga('set', 'page', page);
  ga('send', 'pageview');
}
