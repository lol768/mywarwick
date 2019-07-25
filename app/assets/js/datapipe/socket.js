/* eslint-env browser */

import log from 'loglevel';
import DataPipe from './index';

import RestartableWebSocket from './restartable-websocket';

export default class SocketDataPipe extends DataPipe {
  constructor(options) {
    super();
    const port = window.location.port ? `:${location.port}` : '';
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    this.url = options.url || (`${protocol}//${window.location.hostname}${port}${options.path}`);
    this.initialiseSocket();
    this.messageId = 0;
    this.subscribers = [];
    this.onopen = () => ({});
  }

  initialiseSocket() {
    this.ws = new RestartableWebSocket(this.url);
    this.ws.onmessage = this.messageReceived.bind(this);
    this.ws.onopen = () => {
      this.onopen();
    };
  }

  /**
   * Sends an object up the pipe. Returns a unique messageId that
   * can be used to track replies.
   */
  send(obj) {
    const messageId = this.messageId++;
    this.ws.send(JSON.stringify(Object.assign(obj, { messageId })));
    return messageId;
  }

  messageReceived(event) {
    log.debug('Message event received:', event);
    const data = JSON.parse(event.data);
    this.subscribers.forEach((sub) => {
      sub(data);
    });
  }

  subscribe(handler) {
    this.subscribers.push(handler);
  }

  connect() {

  }

  reconnect() {
    log.debug('Received request to reconnect WS...');
    this.ws.close();
    this.initialiseSocket();
  }
}
