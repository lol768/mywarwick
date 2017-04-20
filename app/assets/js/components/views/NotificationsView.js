import React, { PropTypes } from 'react';
import moment from 'moment';
import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import EmptyState from '../ui/EmptyState';
import { connect } from 'react-redux';
import { getStreamSize, takeFromStream } from '../../stream';
import { markNotificationsRead } from '../../state/notification-metadata';
import * as notifications from '../../state/notifications';

const SOME_MORE = 20;

// ms of continuous visibility required for notifications to be marked as read
const NOTIFICATION_READ_TIMEOUT = 1500;

class NotificationsView extends React.Component {

  static propTypes = {
    notifications: PropTypes.object,
    olderItemsOnServer: PropTypes.bool,
    dispatch: PropTypes.func.isRequired,
    notificationsLastRead: PropTypes.shape({
      date: PropTypes.object,
    }),
    grouped: PropTypes.bool.isRequired,
    notificationPermission: PropTypes.string,
  };

  static defaultProps = {
    grouped: true,
  };

  constructor(props) {
    super(props);

    this.state = {
      numberToShow: SOME_MORE,
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
    };

    this.loadMore = this.loadMore.bind(this);
    this.beginMarkReadTimeout = this.beginMarkReadTimeout.bind(this);
  }

  componentWillMount() {
    this.beginMarkReadTimeout();
  }

  componentDidMount() {
    document.addEventListener('visibilitychange', this.beginMarkReadTimeout);
    window.addEventListener('focus', this.beginMarkReadTimeout);
    window.addEventListener('blur', this.beginMarkReadTimeout);
  }

  componentWillReceiveProps(newProps) {
    if (getStreamSize(newProps.notifications) !== getStreamSize(this.props.notifications)) {
      // If there are new notifications (while the view is mounted), mark them
      // as read
      this.beginMarkReadTimeout();
    }
  }

  componentWillUnmount() {
    clearTimeout(this.timeout);
    this.markNotificationsRead();

    document.removeEventListener('visibilitychange', this.beginMarkReadTimeout);
    window.removeEventListener('focus', this.beginMarkReadTimeout);
    window.removeEventListener('blur', this.beginMarkReadTimeout);
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
      return Promise.resolve(this.showMore());
    } else if (this.props.olderItemsOnServer) {
      return this.props.dispatch(notifications.fetchMoreNotifications())
        .then(() => this.showMore());
    }
    return Promise.resolve();
  }

  showMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE,
    });
  }

  markNotificationsRead() {
    this.props.dispatch(markNotificationsRead(moment()));
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
    const hasAny = streamSize > 0 || this.props.olderItemsOnServer;
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
          <InfiniteScrollable
            hasMore={ hasMore }
            onLoadMore={ this.loadMore }
            showLoading
            endOfListPhrase="There are no older notifications."
          >
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

function select(state) {
  return {
    notifications: state.notifications.stream,
    notificationsLastRead: state.notificationsLastRead,
    olderItemsOnServer: state.notifications.olderItemsOnServer,
    notificationPermission: state.device.notificationPermission,
  };
}

export default connect(select)(NotificationsView);
