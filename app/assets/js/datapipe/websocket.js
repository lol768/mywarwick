
var Rx = require('rx');
var log = require('loglevel');
var DataPipe = require('../datapipe');

export default class SocketDataPipe extends DataPipe {
  constructor(options) {
    this.url = options.url || ("wss://" + window.location.hostname + (location.port ? ':' + location.port : '') + options.path);
    this.stream = new Rx.ReplaySubject(1);
    this.connect();
  }

  messageReceived(message: String) {
    this.stream.onNext(message);
  }

  getUpdateStream(): Rx.Observable {
    return this.stream.asObservable();
  }

  connect() {
    var self = this;
    var ws = new WebSocket(this.url);
    this.connected = false;
    ws.onopen = () => {
      this.connected = true;
      //log.info('WS Sending request');
      ws.send('{"message":"Client message from main.js"}');
    };
    ws.onclose = () => {
      this.connected = false;
      log.info("WS Closed");
    };
    ws.onmessage = (message) => this.messageReceived(message);
  }
}