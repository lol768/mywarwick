import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import moment from 'moment';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';

import { connect } from 'react-redux';

import { takeFromStream, getStreamSize } from '../../stream';
import { readNotifications } from '../../notification-metadata';

const SOME_MORE = 20;

class NotificationsView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      numberToShow: SOME_MORE,
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
    };

    this.loadMore = this.loadMore.bind(this);

    if ('permissions' in navigator) {
      navigator.permissions.query({ name: 'notifications' })
        .then(notificationPermissions => {
          /* eslint-disable no-param-reassign */
          /*
           * function parameter reassignment is valid here. See the Permissions API docs ...
           * https://developers.google.com/web/updates/2015/04/permissions-api-for-the-web?hl=en
           */
          notificationPermissions.onchange = this.onBrowserPermissionChange.bind(this);
          /* eslint-enable no-param-reassign */
        });
    }
  }

  loadMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE,
    });
  }

  onBrowserPermissionChange() {
    this.setState({
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
    });
  }

  markNotificationsRead() {
    this.props.dispatch(readNotifications(moment()));
  }

  componentDidMount() {
    this.markNotificationsRead();
  }

  componentDidUpdate() {
    this.markNotificationsRead();
  }


  render() {
    const notifications = takeFromStream(this.props.notifications, this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} forceDisplayDay={!this.props.grouped} {...n} />);

    const hasMore = this.state.numberToShow < getStreamSize(this.props.notifications);

    return (
      <div>
        { this.state.browserPushDisabled ?
          <div className="permission-warning">
            You have blocked Start.Warwick from sending desktop notifications. You'll need to open
            your browser preferences to change that.
          </div>
          : null
        }
        <InfiniteScrollable hasMore={ hasMore } onLoadMore={ this.loadMore }>
          <GroupedList groupBy={ this.props.grouped ? groupItemsByDate : undefined }>
            { notifications.toJS() }
          </GroupedList>
        </InfiniteScrollable>
      </div>
    );
  }

}

function select(state) {
  return {
    notifications: state.get('notifications'),
  };
}

export default connect(select)(NotificationsView);
