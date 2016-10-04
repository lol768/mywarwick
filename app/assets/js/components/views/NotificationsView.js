import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import moment from 'moment';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import EmptyState from '../ui/EmptyState';

import { connect } from 'react-redux';

import { takeFromStream, getStreamSize } from '../../stream';
import { markNotificationsRead } from '../../state/notification-metadata';
import * as notifications from '../../state/notifications';

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
    const streamSize = getStreamSize(this.props.notifications);
    const hasOlderItemsLocally = this.state.numberToShow < streamSize;

    if (hasOlderItemsLocally) {
      this.showMore();
    } else if (this.props.olderItemsOnServer) {
      this.props.dispatch(notifications.fetchMoreNotifications())
        .then(() => this.showMore());
    }
  }

  showMore() {
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
    this.props.dispatch(markNotificationsRead(moment()));
  }

  componentWillMount() {
    const { notificationsLastRead } = this.props;

    // Store the current notifications last-read time so we can highlight new
    // notifications as they arrive
    this.setState({ notificationsLastRead });
  }

  componentWillReceiveProps(newProps) {
    const was = this.state.notificationsLastRead;
    const is = newProps.notificationsLastRead;

    if (!was.fetched && is.fetched) {
      // Only update cached last-read time upon fetch.  Otherwise retain the
      // same value as long as this component is mounted
      this.setState({ notificationsLastRead: is });
    }

    if (getStreamSize(newProps.notifications) !== getStreamSize(this.props.notifications)) {
      // If there are new notifications (while the view is mounted), mark them
      // as read
      this.markNotificationsRead();
    }
  }

  componentDidMount() {
    this.markNotificationsRead();
  }

  isUnread(notification) {
    const { notificationsLastRead } = this.state;

    return notificationsLastRead.date === null
      || moment(notification.date).isAfter(notificationsLastRead.date);
  }

  render() {
    const notificationItems = takeFromStream(this.props.notifications, this.state.numberToShow)
      .map(n =>
        <ActivityItem
          key={ n.id }
          grouped={ this.props.grouped }
          unread={ this.isUnread(n) }
          {...n}
        />
      );

    const streamSize = getStreamSize(this.props.notifications);
    const hasAny = streamSize > 0;
    const hasMore = this.state.numberToShow < streamSize || this.props.olderItemsOnServer;

    return (
      <div>
        { this.state.browserPushDisabled ?
          <div className="permission-warning">
            You have blocked My Warwick from showing system notifications. You'll need to open
            your browser preferences to change that.
          </div>
          : null
        }
        { hasAny ?
          <InfiniteScrollable hasMore={ hasMore } onLoadMore={ this.loadMore }>
            <GroupedList groupBy={ this.props.grouped ? groupItemsByDate : undefined }>
              { notificationItems }
            </GroupedList>
          </InfiniteScrollable>
          :
          <EmptyState lead="You don't have any notifications yet.">
            When there are things that need your attention &ndash;
            coursework due in, library books due back, that kind of thing &ndash;
            you'll see those notifications here.
          </EmptyState>
        }
      </div>
    );
  }

}

NotificationsView.defaultProps = {
  grouped: true,
};

function select(state) {
  return {
    notifications: state.notifications.stream,
    notificationsLastRead: state.notificationsLastRead,
    olderItemsOnServer: state.notifications.olderItemsOnServer,
  };
}

export default connect(select)(NotificationsView);
