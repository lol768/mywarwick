import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import InfiniteScrollable from '../ui/InfiniteScrollable';
import EmptyState from '../ui/EmptyState';
import { getStreamSize, takeFromStream } from '../../stream';
import * as notifications from '../../state/notifications';
import { Routes } from '../AppRoot';
import ScrollRestore from '../ui/ScrollRestore';

const SOME_MORE = 20;

class ActivityView extends React.PureComponent {
  static propTypes = {
    activities: PropTypes.object,
    olderItemsOnServer: PropTypes.bool,
    dispatch: PropTypes.func.isRequired,
    grouped: PropTypes.bool.isRequired,
    numberToShow: PropTypes.number.isRequired,
    isFiltered: PropTypes.bool.isRequired,
  };

  static defaultProps = {
    grouped: true,
  };

  static renderEmpty(isFiltered) {
    if (isFiltered) {
      return (
        <EmptyState lead="You don't have any activity to display.">
          <p>
            There may be others hidden due to your Activity filter settings.
          </p>
        </EmptyState>
      );
    }
    return (
      <EmptyState lead="You don't have any activity yet.">
        <p>
          When you do something at Warwick &ndash;
          like signing in, submitting your coursework, or enrolling for a module &ndash;
          you&apos;ll see a record of it here.
        </p>
      </EmptyState>
    );
  }

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
      this.props.numberToShow + SOME_MORE,
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
          <ScrollRestore url={`/${Routes.ACTIVITY}`}>
            <InfiniteScrollable
              hasMore={ hasMore }
              onLoadMore={ this.loadMore }
              showLoading
              endOfListPhrase="There are no older activities."
            >
              <GroupedList groupBy={shouldBeGrouped ? groupItemsByDate : undefined}>
                {activities}
              </GroupedList>
            </InfiniteScrollable>
          </ScrollRestore>
          : ActivityView.renderEmpty(this.props.isFiltered)
        }
      </div>
    );
  }
}

function select(state) {
  return {
    activities: state.activities.filteredStream,
    isFiltered: _.filter(_.keys(state.activities.filter), value => value !== undefined).length > 0,
    olderItemsOnServer: state.activities.olderItemsOnServer,
    numberToShow: state.activities.numberToShow,
  };
}

export default connect(select)(ActivityView);
