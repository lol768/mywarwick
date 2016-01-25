import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';

import { connect } from 'react-redux';

import { takeFromStream, getStreamSize } from '../../stream';
import { readActivitys } from '../../notification-metadata'

const SOME_MORE = 20;

export default class ActivityView extends ReactComponent {

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

  markActivitysRead() {
    this.props.dispatch(readActivitys(moment()));
  }

  componentDidMount() {
    this.markActivitysRead();
  }

  componentDidUpdate() {
    this.markActivitysRead();
  }

  render() {

    let activities = takeFromStream(this.props.activities, this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} {...n} />);

    let hasMore = this.state.numberToShow < getStreamSize(this.props.activities);

    return (
      <InfiniteScrollable hasMore={hasMore} onLoadMore={this.loadMore.bind(this)}>
        <GroupedList groupBy={this.props.grouped ? groupItemsByDate : undefined}>
          {activities.toJS()}
        </GroupedList>
      </InfiniteScrollable>
    );
  }
}

function select(state) {
  return {
    activities: state.get('activities')
  };
}

export default connect(select)(ActivityView);