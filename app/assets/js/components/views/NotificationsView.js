import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import ActivityItem from '../ui/ActivityItem';

import { connect } from 'react-redux';

import { getStreamPartition } from '../../stream';

class NotificationsView extends ReactComponent {

    render() {
        var notifications = this.props.notifications.map(n => <ActivityItem key={n.id} {...n} />);

        return <div>{notifications}</div>;
    }

}

function select(state) {
    return {
        notifications: getStreamPartition(state.get('notifications'), 0)
    };
}

export default connect(select)(NotificationsView);
