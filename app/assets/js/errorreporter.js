/**
 * Catches Javascript errors and sends them to the
 * server for reporting purposes.
 */

const $ = window.jQuery;
export const socket = null;
export const ajaxUrl = '/error/js';

export function error(event) {
  const msg = {
    type: 'js-error',
    time: (new Date().getTime()),
    text: event.toString(), // TODO bad?
  };
  if (socket) {
    socket.send(msg);
  } else {
    $.post(ajaxUrl, msg);
  }
}

export function init() {
  window.onerror = error;
}
