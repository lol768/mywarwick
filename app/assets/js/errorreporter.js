/**
 * Catches Javascript errors and sends them to the
 * server for reporting purposes.
 */

import fetch from 'isomorphic-fetch';
import log from 'loglevel';
import _ from 'lodash';

let errors = [];
let postErrorsThrottled;

function postErrors() {
  fetch('/api/errors/js', {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(errors),
  }).then(() => {
    log.info('Errors posted to server');
    errors = [];
  }).catch((e) => {
    log.warn('Failed to post errors to server', e);
    postErrorsThrottled();
  });
}

postErrorsThrottled = _.throttle(postErrors, 5000);

function onError(message, source, line, column, error) {
  errors.push({
    time: new Date().getTime(),
    message,
    source,
    line,
    column,
    stack: error.stack || error,
  });

  postErrorsThrottled();
}

export default function init() {
  window.onerror = onError;
}
