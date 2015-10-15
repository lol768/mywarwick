const Dispatcher = require('./Dispatcher');

export default class NotificationActions{

    static didReceiveNotification(notification) {
        Dispatcher.dispatch({
            type: 'notification',
            notification: notification
        });
    }

    static didFetchFromServer(notifications) {
        Dispatcher.dispatch({
            type: 'fetch-notifications',
            notifications: notifications.notifications
        });
    }

    static didFetchFromLocalStorage(notifications) {
        Dispatcher.dispatch({
            type: 'localstorage-notifications',
            notifications: notifications
        });
    }

}
