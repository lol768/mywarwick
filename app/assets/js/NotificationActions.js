const Dispatcher = require('./Dispatcher');

export default class NotificationActions{

    static didReceiveNotification(notification) {
        Dispatcher.dispatch({
            type: 'notification',
            notification: notification
        });
    }

    // either a bunch from server or form local storage
    static didFetchNotifications(notifications) {
        Dispatcher.dispatch({
            type: 'fetch-notifications',
            notifications: notifications
        });
    }

}
