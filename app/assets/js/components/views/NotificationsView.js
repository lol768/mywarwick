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

// ms of continuous visibility required for notifications to be marked as read
const NOTIFICATION_READ_TIMEOUT = 1500;

class NotificationsView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      numberToShow: SOME_MORE,
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
    };

    this.loadMore = this.loadMore.bind(this);
    this.beginMarkReadTimeout = this.beginMarkReadTimeout.bind(this);
  }

  beginMarkReadTimeout() {
    if (!document.hidden && document.hasFocus()) {
      if (this.timeout) {
        clearTimeout(this.timeout);
      }

      this.timeout = setTimeout(() => {
        this.timeout = null;
        this.markNotificationsRead();
      }, NOTIFICATION_READ_TIMEOUT);
    } else {
      clearTimeout(this.timeout);
      this.timeout = null;
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

  markNotificationsRead() {
    this.props.dispatch(markNotificationsRead(moment()));
  }

  componentWillMount() {
    this.beginMarkReadTimeout();
  }

  componentWillUnmount() {
    clearTimeout(this.timeout);
    this.markNotificationsRead();

    document.removeEventListener('visibilitychange', this.beginMarkReadTimeout);
    window.removeEventListener('focus', this.beginMarkReadTimeout);
    window.removeEventListener('blur', this.beginMarkReadTimeout);
  }

  componentWillReceiveProps(newProps) {
    if (getStreamSize(newProps.notifications) !== getStreamSize(this.props.notifications)) {
      // If there are new notifications (while the view is mounted), mark them
      // as read
      this.beginMarkReadTimeout();
    }
  }

  componentDidMount() {
    document.addEventListener('visibilitychange', this.beginMarkReadTimeout);
    window.addEventListener('focus', this.beginMarkReadTimeout);
    window.addEventListener('blur', this.beginMarkReadTimeout);
  }

  isUnread(notification) {
    const { notificationsLastRead } = this.props;

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
    const browserPushDisabled = this.props.notificationPermission === 'denied';

    return (
      <div>
        { browserPushDisabled ?
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
            <p>
              When there are things that need your attention &ndash;
              coursework due in, library books due back, that kind of thing &ndash;
              you'll see those notifications here.
            </p>
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
    notificationPermission: state.device.notificationPermission,
  };
}

export default connect(select)(NotificationsView);
