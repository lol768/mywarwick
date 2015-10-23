import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import ActivityItem from '../ui/ActivityItem';

import { connect } from 'react-redux';

class NotificationsView extends ReactComponent {

    render() {
        var notifications = this.props.notifications.map(notification => <ActivityItem {...notification} />);

        return <div>{notifications}</div>;
    }

}

function select(state) {
    return {
        notifications: state.get('notifications')
    };
}

export default connect(select)(NotificationsView);
