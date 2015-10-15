const FluxStore = require('flux/lib/FluxStore');
const Immutable = require('immutable');
const localforage = require('localforage');

const Dispatcher = require('../Dispatcher');
const NotificationActions = require('../NotificationActions');
const SocketDatapipe = require('../SocketDatapipe');


localforage.getItem('NotificationsStore', function (err, value) {
    if (err) {
        console.error('problem reading notifications from local storage: ' + err);
    } else {
        if (value != null) {
            NotificationActions.didFetchFromLocalStorage(value);
        }
    }
});

//TODO I'm sure this should happen somewhere more sensible
SocketDatapipe.send({
    tileId: "1",
    data: {
        type: "fetch-notifications" // since last login
    }
});

var notifications = Immutable.List();

// send request for notifications missed while offline
class NotificationsStore extends FluxStore {

    getNotifications() {
        return notifications;
    }

    __onDispatch(action) {
        switch (action.type) {
            case 'localstorage-notifications':
                notifications = Immutable.List(action.notifications);
                this.__emitChange();
                break;

            case 'fetch-notifications':
                $(action.notifications).each(function (i, elem) {
                    notifications = notifications.unshift(elem);
                });
                localforage.setItem('NotificationsStore', notifications.toJSON());
                this.__emitChange();
                break;

            case 'notification':
                notifications = notifications.unshift(action.notification);
                localforage.setItem('NotificationsStore', notifications.toJSON());
                this.__emitChange();
                break;

            default:
            // no-op
        }
    }
}

export default new NotificationsStore(Dispatcher);