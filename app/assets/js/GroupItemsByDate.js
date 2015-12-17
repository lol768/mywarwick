import moment from 'moment';

// A way to describe a grouping strategy

// Describe how things are being grouped
// Exposed as a CSS class on the grouped list
export const description = 'by-date';

// Which group an item belongs in
// Return an arbitrary identifier that is the same for all items in the same group
export function groupForItem(item, now = moment()) {
  var date = moment(item.props.date).startOf('day');

  if (date.isSame(now, 'day')) {
    return 0;
  } else if (date.isSame(now.clone().subtract(1, 'day'), 'day')) {
    return 1;
  } else if (date.isSame(now, 'isoWeek')) {
    return 2;
  } else if (date.isSame(now.clone().subtract(1, 'week'), 'isoWeek')) {
    return 3;
  } else {
    return 4;
  }
}

// The title to be displayed for items in the group
// Return a nice title for the user to look at, from the group identifier
export function titleForGroup(group) {
  return [
    'Today',
    'Yesterday',
    'This Week',
    'Last Week',
    'Older'
  ][group];
}
