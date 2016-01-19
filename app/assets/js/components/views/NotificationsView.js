import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';

import { connect } from 'react-redux';

import { takeFromStream, getStreamSize } from '../../stream';

const SOME_MORE = 20;

class NotificationsView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      numberToShow: SOME_MORE,
      browserPushDisabled: window.Notification.permission === "denied"
    };

    navigator.permissions.query({name:'notifications'})
      .then(notificationPermissions => {
        notificationPermissions.onchange = this.onBrowserPermissionChange.bind(this);
      });
  }

  loadMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE
    });
  }

  onBrowserPermissionChange() {
    this.setState({
      browserPushDisabled: window.Notification.permission === "denied"
    });
  }


  render() {

    let notifications = takeFromStream(this.props.notifications, this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} forceDisplayDay={!this.props.grouped} {...n} />);

    let hasMore = this.state.numberToShow < getStreamSize(this.props.notifications);

    return (
      <div>
        { this.state.browserPushDisabled ?
          <div className="permission-warning">
            You have blocked start from sending notifications. You'll need to open your browser preferences to change that.
          </div>
          : null
        }
        <InfiniteScrollable hasMore={hasMore} onLoadMore={this.loadMore.bind(this)}>
          <GroupedList groupBy={this.props.grouped ? groupItemsByDate : undefined}>
            {notifications.toJS()}
          </GroupedList>
        </InfiniteScrollable>
      </div>
    )
  }

}

function select(state) {
  return {
    notifications: state.get('notifications')
  };
}

export default connect(select)(NotificationsView);

