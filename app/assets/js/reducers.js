import Immutable from 'immutable';

import { NAVIGATE, DID_RECEIVE_NOTIFICATION, DID_FETCH_NOTIFICATIONS, NEWS_FETCH, NEWS_FETCH_SUCCESS, NEWS_FETCH_FAILURE } from './actions';

const initialState = Immutable.fromJS({
    path: '/',
    notifications: [],
    news: {
        fetching: false,
        items: []
    }
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
        case NEWS_FETCH:
            return state.mergeDeep({
                news: {
                    fetching: true
                }
            });
        case NEWS_FETCH_SUCCESS:
            return state.mergeDeep({
                news: {
                    fetching: false,
                    items: action.items
                }
            });
        case NEWS_FETCH_FAILURE:
            return state.mergeDeep({
                news: {
                    fetching: false
                }
            });
        default:
            return state;
    }
}
