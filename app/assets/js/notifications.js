import Immutable from 'immutable';
import { uniq, sortByOrder } from 'lodash';

import store from './store';
import { registerReducer } from './reducers';

export const NOTIFICATION_RECEIVE = 'notifications.receive';
export const NOTIFICATION_FETCH = 'notifications.fetch';

import { onReceive } from './stream';

export function receivedNotification(notification) {
    return {
        type: NOTIFICATION_RECEIVE,
        notification: notification
    };
}

export function fetchedNotifications(notifications) {
    return {
        type: NOTIFICATION_FETCH,
        notifications: notifications
    };
}

export function mergeNotifications(notifications, newNotifications) {
    return Immutable.List(onReceive(notifications.toJS(), newNotifications));
}

registerReducer('notifications', (state = Immutable.List(), action) => {
    switch (action.type) {
        case NOTIFICATION_RECEIVE:
            return mergeNotifications(state, [action.notification]);
        case NOTIFICATION_FETCH:
            return mergeNotifications(state, action.notifications);
        default:
            return state;
    }
});
