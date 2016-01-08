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
      numberToShow: SOME_MORE
    };
  }

  loadMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE
    });
  }

  render() {

    let notifications = takeFromStream(this.props.notifications, this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} forceDisplayDay={!this.props.grouped} {...n} />);

    let hasMore = this.state.numberToShow < getStreamSize(this.props.notifications);

    return (
      <InfiniteScrollable hasMore={hasMore} onLoadMore={this.loadMore.bind(this)}>
        <GroupedList groupBy={this.props.grouped ? groupItemsByDate : undefined}>
          {notifications.toJS()}
        </GroupedList>
      </InfiniteScrollable>
    )
  }

}

function select(state) {
  return {
    notifications: state.get('notifications')
  };
}

export default connect(select)(NotificationsView);

