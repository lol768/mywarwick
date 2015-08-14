
const Rx = require('rx');
const log = require('loglevel');
const DataPipe = require('../datapipe');

const RestartableWebSocket = require('./restartable-websocket');

export default class SocketDataPipe extends DataPipe {
  constructor(options) {
    super();
    this.url = options.url || ("wss://" + window.location.hostname + (location.port ? ':' + location.port : '') + options.path);
    this.stream = new Rx.ReplaySubject(1);
    this.ws = new RestartableWebSocket(this.url);
    this.ws.onmessage = this.messageReceived.bind(this);
  }

  send(obj) {
    this.ws.send(obj.toString());
  }

  messageReceived(event) {
    log.debug("Message event received:", event);
    let data = JSON.parse(event.data);
    this.stream.onNext(data);
  }

  getUpdateStream(): Rx.Observable {
    return this.stream.asObservable();
  }

  connect() {

  }
}