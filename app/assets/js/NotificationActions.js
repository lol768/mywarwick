const Dispatcher = require('./Dispatcher');

export default class NotificationActions{

    static didReceiveNotification(notification) {
        Dispatcher.dispatch({
            type: 'notification',
            notification: notification
        });
    }

    static didReceiveManyNotifications(notifications) {
        Dispatcher.dispatch({
            type: 'fetch-notifications',
            notifications: notifications
        });
    }

}
