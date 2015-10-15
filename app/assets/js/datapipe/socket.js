const Rx = require('rx');
const log = require('loglevel');
const DataPipe = require('../datapipe');

const RestartableWebSocket = require('./restartable-websocket');

const NotificationActions = require('../NotificationActions');

export default class SocketDataPipe extends DataPipe {
    constructor(options) {
        super();
        this.url = options.url || ("wss://" + window.location.hostname + (location.port ? ':' + location.port : '') + options.path);
        this.stream = new Rx.ReplaySubject(1);
        this.ws = new RestartableWebSocket(this.url);
        this.ws.onmessage = this.messageReceived.bind(this);

        this.messageId = 0;
    }

    requestData(info) {
        /*this.send({
         type: 'request-data',

         });*/
    }

    /**
     * Sends an object up the pipe. Returns a unique messageId that
     * can be used to track replies.
     */
    send(obj) {
        const messageId = this.messageId++;
        obj.messageId = messageId;
        this.ws.send(JSON.stringify(obj));
        return messageId
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
        return this.getUpdateStream().first(msg => msg.replyTo == messageId);
    }

    messageReceived(event) {
        log.debug("Message event received:", event);
        let data = JSON.parse(event.data);
        this.stream.onNext(data);

        //TODO implement proper message routing
        switch (data.type) {
            case 'fetch-notifications':
                NotificationActions.didFetchFromServer(data);
                break;
            case 'notification':
                NotificationActions.didReceiveNotification(data);
                break;
            default:
            // nowt
        }
    }

    getUpdateStream():Rx.Observable {
        return this.stream.asObservable();
    }

    connect() {

    }
}