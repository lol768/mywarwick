/**
 * Catches Javascript errors and sends them to the
 * server for reporting purposes.
 */

const $ = window.jQuery;
export var socket = null;
export var ajaxUrl = '/error/js';

export function init() {
  window.onerror = error;
}

export function error(event) {
  let msg = {
    type: 'js-error',
    time: (new Date().getTime()),
    text: event.toString() // TODO bad?
  };
  if (socket) {
    socket.send(msg);
  } else {
    $.post(ajaxUrl, msg);
  }
}
