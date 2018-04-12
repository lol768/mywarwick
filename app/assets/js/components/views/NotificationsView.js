/* eslint-env browser */

import React from 'react';
import * as PropTypes from 'prop-types';
import moment from 'moment';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import EmptyState from '../ui/EmptyState';
import { getStreamSize, takeFromStream } from '../../stream';
import { markNotificationsRead } from '../../state/notification-metadata';
import * as notifications from '../../state/notifications';
import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';
import HideableView from './HideableView';
import ActivityMutingView from './ActivityMutingView';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import isEmbedded from '../../embedHelper';

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
    isFiltered: PropTypes.bool.isRequired,
  };

  static defaultProps = {
    grouped: true,
  };

  static renderEmpty(isFiltered) {
    if (isFiltered) {
      return (
        <EmptyState lead="You don't have any alerts to display.">
          <p>
            There may be others hidden due to your Alert filter settings.
          </p>
        </EmptyState>
      );
    }
    return (
      <EmptyState lead="You don't have any alerts yet.">
        <p>
          When there are things that need your attention &ndash;
          coursework due in, library books due back, that kind of thing &ndash;
          you’ll see those alerts here.
        </p>
      </EmptyState>
    );
  }

  constructor(props) {
    super(props);

    this.state = {
      browserPushDisabled: 'Notification' in window && Notification.permission === 'denied',
      mutingActivity: null,
    };

    this.loadMore = this.loadMore.bind(this);
    this.beginMarkReadTimeout = this.beginMarkReadTimeout.bind(this);
    this.onMuting = this.onMuting.bind(this);
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
    return (formNameValues) => {
      this.props.dispatch(notifications.saveActivityMute(activity, formNameValues));
      this.onMutingDismiss();
    };
  }

  beginMarkReadTimeout() {
    if (!document.hidden && (document.hasFocus() || isEmbedded())) {
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
      this.props.numberToShow + SOME_MORE,
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
        (<ActivityItem
          key={ n.id }
          grouped={ shouldBeGrouped }
          unread={ this.isUnread(n) }
          muteable
          onMuting={ this.onMuting }
          {...n}
        />),
      );

    const streamSize = getStreamSize(this.props.notifications);
    const hasAny = streamSize > 0 || this.props.olderItemsOnServer;
    const hasMore = this.props.numberToShow < streamSize || this.props.olderItemsOnServer;
    const browserPushDisabled = this.props.notificationPermission === 'denied';

    return (
      <div>
        <ReactCSSTransitionGroup
          transitionName="grow-shrink-modal"
          transitionAppear
          transitionAppearTimeout={200}
          transitionEnter
          transitionEnterTimeout={200}
          transitionLeave
          transitionLeaveTimeout={200}
        >
          { this.renderMuting() }
        </ReactCSSTransitionGroup>
        { !isEmbedded() && browserPushDisabled ?
          <div className="permission-warning">
            You have blocked My Warwick from showing system notifications. You&apos;ll need to open
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
              endOfListPhrase="There are no older alerts."
            >
              <GroupedList groupBy={ shouldBeGrouped ? groupItemsByDate : undefined }>
                { notificationItems }
              </GroupedList>
            </InfiniteScrollable>
          </ScrollRestore>
          : NotificationsView.renderEmpty(this.props.isFiltered)
        }
      </div>
    );
  }
}

function select(state) {
  return {
    notifications: state.notifications.filteredStream,
    notificationsLastRead: state.notificationsLastRead,
    isFiltered: _.filter(
      _.keys(state.notifications.filter),
      value => value !== undefined,
    ).length > 0,
    olderItemsOnServer: state.notifications.olderItemsOnServer,
    notificationPermission: state.device.notificationPermission,
    numberToShow: state.notifications.numberToShow,
  };
}

export default connect(select)(NotificationsView);
