import moment from 'moment';

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

export default groupItemsByDate;
