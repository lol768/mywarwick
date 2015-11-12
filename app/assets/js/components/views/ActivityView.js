import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import moment from 'moment';

import ActivityItem from '../ui/ActivityItem';
import GroupedList from '../ui/GroupedList';

export default class ActivityView extends ReactComponent {

  render() {
    return (
      <GroupedList groupBy={groupItemsByDate}>
        <ActivityItem key="a"
                      text="You changed your preferred photo"
                      source="Photos"
                      date={moment().subtract(1, 'days')}/>
        <ActivityItem key="b"
                      text="You submitted coursework for LA118 Intro to Criminal Law"
                      source="Tabula"
                      date={moment().add(1, 'days')}/>
        <ActivityItem key="c"
                      text="You signed in using Edge on Windows 10"
                      source="Web Sign-On"
                      date={moment().subtract(2, 'months')}/>
      </GroupedList>
    );
  }

}

// A way to describe a grouping strategy
let groupItemsByDate = {

  // Describe how things are being grouped
  // Exposed as a CSS class on the grouped list
  description: 'by-date',

  // Which group an item belongs in
  // Return an arbitrary identifier that is the same for all items in the same group
  groupForItem(item) {
    var date = moment(item.props.date).startOf('day');

    if (date.isSame(moment(), 'day')) {
      return 'Today';
    } else if (date.isSame(moment().subtract(1, 'day'), 'day')) {
      return 'Yesterday';
    } else if (date.isSame(moment(), 'week')) {
      return 'This Week';
    } else if (date.isSame(moment().subtract(1, 'week'), 'week')) {
      return 'Last Week';
    } else {
      return 'Older';
    }
  },

  // The title to be displayed for items in the group
  // Return a nice title for the user to look at, from the group identifier
  titleForGroup(group) {
    return group;
  }

};
