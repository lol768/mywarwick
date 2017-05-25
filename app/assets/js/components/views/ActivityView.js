import React, { PropTypes } from 'react';
import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import EmptyState from '../ui/EmptyState';
import { connect } from 'react-redux';
import { getStreamSize, takeFromStream } from '../../stream';
import * as notifications from '../../state/notifications';
import { Routes } from '../AppRoot';
import ScrollRestore from '../ui/ScrollRestore';
import HideableView from './HideableView';

const SOME_MORE = 20;

class ActivityView extends HideableView {

  static propTypes = {
    hiddenView: PropTypes.bool.isRequired,
    activities: PropTypes.object,
    olderItemsOnServer: PropTypes.bool,
    dispatch: PropTypes.func.isRequired,
    grouped: PropTypes.bool.isRequired,
    numberToShow: PropTypes.number.isRequired,
  };

  static defaultProps = {
    grouped: true,
  };

  constructor(props) {
    super(props);

    this.loadMore = this.loadMore.bind(this);
  }

  loadMore() {
    const streamSize = getStreamSize(this.props.activities);
    const hasOlderItemsLocally = this.props.numberToShow < streamSize;

    if (hasOlderItemsLocally) {
      return Promise.resolve(this.showMore());
    } else if (this.props.olderItemsOnServer) {
      return this.props.dispatch(notifications.fetchMoreActivities())
        .then(() => this.showMore());
    }
    return Promise.resolve();
  }

  showMore() {
    this.props.dispatch(notifications.showMoreActivities(
      this.props.numberToShow + SOME_MORE
    ));
  }

  shouldBeGrouped() {
    const top = takeFromStream(this.props.activities, 1);
    if (top.length < 1) {
      return false;
    }
    const item = <ActivityItem {...top[0]} />;
    return groupItemsByDate.groupForItem(item) !== groupItemsByDate.maxGroup;
  }

  render() {
    const shouldBeGrouped = this.props.grouped && this.shouldBeGrouped();
    const activities = takeFromStream(this.props.activities, this.props.numberToShow)
      .map(n => <ActivityItem key={n.id} grouped={shouldBeGrouped} {...n} />);

    const streamSize = getStreamSize(this.props.activities);
    const hasAny = streamSize > 0 || this.props.olderItemsOnServer;
    const hasMore = this.props.numberToShow < streamSize || this.props.olderItemsOnServer;

    return (
      <div>
        { hasAny ?
          <ScrollRestore url={`/${Routes.ACTIVITY}`} hiddenView={ this.props.hiddenView }>
            <InfiniteScrollable
              hasMore={ hasMore }
              onLoadMore={ this.loadMore }
              showLoading
              endOfListPhrase="There are no older activities."
              hiddenView={ this.props.hiddenView }
            >
              <GroupedList groupBy={shouldBeGrouped ? groupItemsByDate : undefined}>
                {activities}
              </GroupedList>
            </InfiniteScrollable>
          </ScrollRestore>
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

function select(state) {
  return {
    activities: state.activities.stream,
    olderItemsOnServer: state.activities.olderItemsOnServer,
    numberToShow: state.activities.numberToShow,
  };
}

export default connect(select)(ActivityView);
