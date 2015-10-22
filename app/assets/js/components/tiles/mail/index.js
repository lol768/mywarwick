import { connect } from 'react-redux';
import Immutable from 'immutable';
import { registerReducer } from '../../../reducers';
import store from '../../../store';

import { MAIL_RECEIVE, receiveMail } from './actions';
import tile from './tile';

export default connect((state) => state.get('mail').toJS())(tile);

const initialState = Immutable.fromJS({
    messages: []
});

registerReducer('mail', (state = initialState, action) => {
    switch (action.type) {
        case MAIL_RECEIVE:
            return state.update('messages', (messages) => messages.unshift(action.message));
        default:
            return state;
    }
});

store.dispatch(receiveMail({
    id: 'mail-1',
    from: 'Squirrell, Linda',
    subject: 'IT Induction Day - Reminder',
    date: 'Monday'
}));

setTimeout(() => {
    store.dispatch(receiveMail({
        id: 'mail-2',
        from: 'Kennedy, Christelle',
        subject: 'Departmental meeting',
        date: 'Yesterday'
    }));
}, 1000);

setTimeout(() => {
    store.dispatch(receiveMail({
        id: 'mail-3',
        from: 'IT Service Desk',
        subject: 'Emergency RFC0021650 - Network',
        date: '09:48'
    }));
}, 2000);
