import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import moment from 'moment';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';

import { connect } from 'react-redux';

import { takeFromStream, getStreamSize } from '../../stream';
import { readActivities } from '../../notification-metadata';

const SOME_MORE = 20;

export default class ActivityView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      numberToShow: SOME_MORE,
    };
    this.loadMore = this.loadMore.bind(this);
  }

  loadMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE,
    });
  }

  markActivitiesRead() {
    this.props.dispatch(readActivities(moment()));
  }

  componentDidMount() {
    this.markActivitiesRead();
  }

  componentDidUpdate() {
    this.markActivitiesRead();
  }

  render() {
    const activities = takeFromStream(this.props.activities, this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} {...n} />);

    const hasMore = this.state.numberToShow < getStreamSize(this.props.activities);

    return (
      <InfiniteScrollable hasMore={hasMore} onLoadMore={ this.loadMore }>
        <GroupedList groupBy={this.props.grouped ? groupItemsByDate : undefined}>
          {activities.toJS()}
        </GroupedList>
      </InfiniteScrollable>
    );
  }
}

function select(state) {
  return {
    activities: state.get('activities'),
  };
}

export default connect(select)(ActivityView);
