import Immutable from 'immutable';

import { NAVIGATE, DID_RECEIVE_NOTIFICATION, DID_FETCH_NOTIFICATIONS } from './actions';

const initialState = Immutable.Map({
    path: '/',
    notifications: Immutable.List()
});

export function app(state = initialState, action) {
    switch (action.type) {
        case NAVIGATE:
            return state.set('path', action.path);
        case DID_RECEIVE_NOTIFICATION:
        {
            let notifications = state.get('notifications').unshift(action.notification);

            return state.set('notifications', notifications);
        }
        case DID_FETCH_NOTIFICATIONS:
        {
            let notifications = state.get('notifications').concat(action.notifications);

            return state.set('notifications', notifications);
        }
        default:
            return state;
    }
}
