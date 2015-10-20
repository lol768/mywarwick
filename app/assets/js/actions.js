export const NAVIGATE = 'NAVIGATE';
export const DID_RECEIVE_NOTIFICATION = 'DID_RECEIVE_NOTIFICATION';
export const DID_FETCH_NOTIFICATIONS = 'DID_FETCH_NOTIFICATIONS';

export function navigate(path) {
    return {
        type: NAVIGATE,
        path: path
    };
}

export function didReceiveNotification(notification) {
    return {
        type: DID_RECEIVE_NOTIFICATION,
        notification: notification
    };
}

export function didFetchNotifications(notifications) {
    return {
        type: DID_FETCH_NOTIFICATIONS,
        notifications: notifications
    };
}
