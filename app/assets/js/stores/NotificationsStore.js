const FluxStore = require('flux/lib/FluxStore');

const Dispatcher = require('../Dispatcher');
const NotificationActions = require('../NotificationActions');
const Immutable = require('immutable');

const localforage = require('localforage');

localforage.getItem('NotificationsStore', function(err, value) {
    if (err) {
        console.error('');
    } else {
        if (value != null) {
            NotificationActions.didReceiveManyNotifications(value);
        }
    }
});

var notifications = Immutable.List();

class NotificationsStore extends FluxStore {

    getNotifications() {
        return notifications;
    }

    __onDispatch(action) {
        switch (action.type) {
            case 'many-notifications':
                notifications = Immutable.List(action.notifications);
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