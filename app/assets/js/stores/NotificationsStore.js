const FluxStore = require('flux/lib/FluxStore');

const Dispatcher = require('../Dispatcher');
const NotificationActions = require('../NotificationActions');
const Immutable = require('immutable');

const localforage = require('localforage');

const SocketDatapipe = require('../SocketDatapipe');

var notifications = Immutable.List();

class NotificationsStore extends FluxStore {

    getNotifications() {
        return notifications;
    }

    getNotificationIDs() {
        var ids = [];
        notifications.map((item) => {
            ids.push(item.key)
        });
        return ids;
    }


    __onDispatch(action) {
        switch (action.type) {
            case 'fetch-notifications':
                notifications = Immutable.List(action.notifications);
                localforage.setItem('NotificationsStore', notifications.toJSON());
                this.__emitChange();
                break;

            case 'notification':
                notifications = notifications.unshift(action.notification); // unshift returns new length, might be useful
                localforage.setItem('NotificationsStore', notifications.toJSON());
                this.__emitChange();
                break;

            default:
            // no-op
        }
    }
}

export default new NotificationsStore(Dispatcher);