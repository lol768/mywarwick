import Rx from 'rx';
import log from 'loglevel';
import DataPipe from '../datapipe';

import RestartableWebSocket from './restartable-websocket';

export default class SocketDataPipe extends DataPipe {
  constructor(options) {
    super();
    const port = window.location.port ? `:${location.port}` : '';
    this.url = options.url || (`wss://${window.location.hostname}${port}${options.path}`);
    this.stream = new Rx.ReplaySubject(1);
    this.ws = new RestartableWebSocket(this.url);
    this.ws.onmessage = this.messageReceived.bind(this);
    this.ws.onopen = () => {
      this.onopen();
    };
    this.onopen = () => ({});
    this.messageId = 0;
  }

  /* eslint-disable no-unused-vars */
  requestData(info) {
    /* this.send({
     type: 'request-data',

     }); */
  }
  /* eslint-enable no-unused-vars */

  /**
   * Sends an object up the pipe. Returns a unique messageId that
   * can be used to track replies.
   */
  send(obj) {
    const messageId = this.messageId++;
    obj.messageId = messageId;
    this.ws.send(JSON.stringify(obj));
    return messageId;
  }

  /**
   * Sends a message as per `send`, but returns an observable object
   * that waits for a reply to that message (signified by the replyTo property).
   *
   * There is no timeout to this observable, so you may wish to add your
   * own by composing a new Observable, since messages can get lost.
   */
  ask(obj) {
    const messageId = this.send(obj);
    return this.getUpdateStream().first(msg => msg.replyTo === messageId);
  }

  messageReceived(event) {
    log.debug('Message event received:', event);
    const data = JSON.parse(event.data);
    this.stream.onNext(data);
  }

  getUpdateStream() {
    return this.stream.asObservable();
  }

  connect() {

  }
}
