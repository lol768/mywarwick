/**
 * Catches Javascript errors and sends them to the
 * server for reporting purposes.
 */

import fetch from 'isomorphic-fetch';

function onError(message, source, line, column, error) {
  const body = {
    time: new Date().getTime(),
    message,
    source,
    line,
    column,
    stack: error.stack || error,
  };

  fetch('/api/errors/js', {
    credentials: 'same-origin',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });
}

export default function init() {
  window.onerror = onError;
}
