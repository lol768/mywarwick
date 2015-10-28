import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactDOM from 'react-dom';
import $ from 'jquery';

import ActivityItem from '../ui/ActivityItem';

import { connect } from 'react-redux';

import { getStreamPartition } from '../../stream';

import InfiniteScrollable from '../ui/InfiniteScrollable';

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
    let notifications = this.props.notifications
      .take(this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} {...n} />);

    let hasMore = this.state.numberToShow < this.props.notifications.count();

    return (
      <InfiniteScrollable hasMore={hasMore} onLoadMore={this.loadMore.bind(this)}>
        {notifications}
      </InfiniteScrollable>
    )
  }

}

function select(state) {
  return {
    notifications: getStreamPartition(state.get('notifications'), 0)
  };
}

export default connect(select)(NotificationsView);
