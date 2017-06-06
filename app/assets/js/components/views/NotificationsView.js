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
import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';
import HideableView from './HideableView';
import ActivityMutingView from './ActivityMutingView';


const SOME_MORE = 20;

// ms of continuous visibility required for notifications to be marked as read
const NOTIFICATION_READ_TIMEOUT = 1500;

class NotificationsView extends HideableView {

  static propTypes = {
    notifications: PropTypes.object,
    olderItemsOnServer: PropTypes.bool,
    dispatch: PropTypes.func.isRequired,
    notificationsLastRead: PropTypes.shape({
      date: PropTypes.object,
    }),
    grouped: PropTypes.bool.isRequired,
    notificationPermission: PropTypes.string,
    numberToShow: PropTypes.number.isRequired,
  };

  static defaultProps = {
    grouped: true,
  };

  constructor(props) {
    super(props);

    this.state = {
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
      mutingActivity: null,
    };

    this.loadMore = this.loadMore.bind(this);
    this.beginMarkReadTimeout = this.beginMarkReadTimeout.bind(this);
    this.onMutingDismiss = this.onMutingDismiss.bind(this);
  }

  componentDidShow() {
    this.beginMarkReadTimeout();
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

  componentDidHide() {
    clearTimeout(this.timeout);
    this.markNotificationsRead();

    document.removeEventListener('visibilitychange', this.beginMarkReadTimeout);
    window.removeEventListener('focus', this.beginMarkReadTimeout);
    window.removeEventListener('blur', this.beginMarkReadTimeout);
  }

  onMuting(activity) {
    this.setState({
      mutingActivity: activity,
    });
  }

  onMutingDismiss() {
    this.setState({
      mutingActivity: null,
    });
  }

  onMutingSave(activity) {
    return (formValues) => {
      this.props.dispatch(notifications.saveActivityMute(activity, formValues));
      this.onMutingDismiss();
    };
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
    const hasOlderItemsLocally = this.props.numberToShow < streamSize;

    if (hasOlderItemsLocally) {
      return Promise.resolve(this.showMore());
    } else if (this.props.olderItemsOnServer) {
      return this.props.dispatch(notifications.fetchMoreNotifications())
        .then(() => this.showMore());
    }
    return Promise.resolve();
  }

  showMore() {
    this.props.dispatch(notifications.showMoreNotifications(
      this.props.numberToShow + SOME_MORE
    ));
  }

  markNotificationsRead() {
    this.props.dispatch(markNotificationsRead(moment()));
  }

  isUnread(notification) {
    const { notificationsLastRead } = this.props;

    return notificationsLastRead.date !== null
      && moment(notification.date).isAfter(notificationsLastRead.date);
  }

  shouldBeGrouped() {
    const top = takeFromStream(this.props.notifications, 1);
    if (top.length < 1) {
      return false;
    }
    const item = <ActivityItem {...top[0]} />;
    return groupItemsByDate.groupForItem(item) !== groupItemsByDate.maxGroup;
  }

  renderMuting() {
    if (this.state.mutingActivity) {
      const activity = this.state.mutingActivity;
      return (
        <ActivityMutingView
          {...activity}
          onMutingDismiss={ this.onMutingDismiss }
          onMutingSave={ this.onMutingSave(activity) }
          activityType={ activity.type }
          activityTypeDisplayName={ activity.typeDisplayName }
        />
      );
    }
    return null;
  }

  render() {
    const shouldBeGrouped = this.props.grouped && this.shouldBeGrouped();
    const notificationItems = takeFromStream(this.props.notifications, this.props.numberToShow)
      .map(n =>
        <ActivityItem
          key={ n.id }
          grouped={ shouldBeGrouped }
          unread={ this.isUnread(n) }
          mutable
          onMuting={ () => this.onMuting(n) }
          {...n}
        />
      );

    const streamSize = getStreamSize(this.props.notifications);
    const hasAny = streamSize > 0 || this.props.olderItemsOnServer;
    const hasMore = this.props.numberToShow < streamSize || this.props.olderItemsOnServer;
    const browserPushDisabled = this.props.notificationPermission === 'denied';

    return (
      <div>
        {
          this.renderMuting()
        }
        { browserPushDisabled ?
          <div className="permission-warning">
            You have blocked My Warwick from showing system notifications. You'll need to open
            your browser preferences to change that.
          </div>
          : null
        }
        { hasAny ?
          <ScrollRestore url={`/${Routes.NOTIFICATIONS}`}>
            <InfiniteScrollable
              hasMore={ hasMore }
              onLoadMore={ this.loadMore }
              showLoading
              endOfListPhrase="There are no older notifications."
            >
              <GroupedList groupBy={ shouldBeGrouped ? groupItemsByDate : undefined }>
                { notificationItems }
              </GroupedList>
            </InfiniteScrollable>
          </ScrollRestore>
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
    numberToShow: state.notifications.numberToShow,
  };
}

export default connect(select)(NotificationsView);
