import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import EmptyState from '../ui/EmptyState';
import { connect } from 'react-redux';
import { takeFromStream, getStreamSize } from '../../stream';
import * as notifications from '../../state/notifications';
import log from 'loglevel';

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
    const streamSize = getStreamSize(this.props.activities);
    const hasOlderItemsLocally = this.state.numberToShow < streamSize;

    if (hasOlderItemsLocally) {
      return Promise.resolve(this.showMore());
    } else if (this.props.olderItemsOnServer) {
      return this.props.dispatch(notifications.fetchMoreActivities())
        .then(() => this.showMore())
        .catch((e) => {
          if (e instanceof notifications.UnnecessaryFetchError) {
            log.debug(`Unnecessary fetch: ${e.message}`);
          } else {
            throw e;
          }
        });
    }
    return Promise.resolve();
  }

  showMore() {
    this.setState({
      numberToShow: this.state.numberToShow + SOME_MORE,
    });
  }

  render() {
    const activities = takeFromStream(this.props.activities, this.state.numberToShow)
      .map(n => <ActivityItem key={n.id} grouped={this.props.grouped} {...n} />);

    const streamSize = getStreamSize(this.props.activities);
    const hasAny = streamSize > 0;
    const hasMore = this.state.numberToShow < streamSize || this.props.olderItemsOnServer;

    return (
      <div>
        { hasAny ?
          <InfiniteScrollable hasMore={ hasMore } onLoadMore={ this.loadMore } showLoading>
            <GroupedList groupBy={this.props.grouped ? groupItemsByDate : undefined}>
              {activities}
            </GroupedList>
          </InfiniteScrollable>
          :
          <EmptyState lead="You don't have any activity yet.">
            <p>
              When you do something at Warwick &ndash;
              like signing in, submitting your coursework, or enrolling for a module &ndash;
              you'll see a record of it here.
            </p>
          </EmptyState>
        }
      </div>
    );
  }
}

ActivityView.defaultProps = {
  grouped: true,
};

function select(state) {
  return {
    activities: state.activities.stream,
    olderItemsOnServer: state.activities.olderItemsOnServer,
  };
}

export default connect(select)(ActivityView);
