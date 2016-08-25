import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import EmptyState from '../ui/EmptyState';

import { connect } from 'react-redux';

import { takeFromStream, getStreamSize } from '../../stream';

const SOME_MORE = 20;

class ActivityView extends ReactComponent {

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

  render() {
    const activities = takeFromStream(this.props.activities, this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} {...n} />);

    const streamSize = getStreamSize(this.props.activities);
    const hasAny = streamSize > 0;
    const hasMore = this.state.numberToShow < streamSize;

    return (
      <div>
        { hasAny ?
          <InfiniteScrollable hasMore={hasMore} onLoadMore={ this.loadMore }>
            <GroupedList groupBy={this.props.grouped ? groupItemsByDate : undefined}>
              {activities}
            </GroupedList>
          </InfiniteScrollable>
          :
          <EmptyState lead="You don't have any activity yet.">
            When you do something at Warwick &ndash;
            like signing in, submitting your coursework, or enrolling for a module &ndash;
            you'll see a record of it here.
          </EmptyState>
        }
      </div>
    );
  }
}

function select(state) {
  return {
    activities: state.activities,
  };
}

export default connect(select)(ActivityView);
