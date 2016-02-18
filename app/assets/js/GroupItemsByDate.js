import { localMoment } from './dateFormatter';

// A way to describe a grouping strategy

// Describe how things are being grouped
// Exposed as a CSS class on the grouped list
export const description = 'by-date';

// Which group an item belongs in
// Return an arbitrary identifier that is the same for all items in the same group
export function groupForItem(item, now = localMoment()) {
  const date = localMoment(item.props.date).startOf('day');
  const tomorrow = now.clone().add(1, 'day');

  // date is next week but not tomorrow
  if (date.isSame(now.clone().add(1, 'week'), 'isoWeek') && date.isAfter(tomorrow)) {
    return 0;
    // later this week but not tomorrow
  } else if (date.isSame(now, 'isoWeek') && date.isAfter(tomorrow)) {
    return 1;
    // tomorrow
  } else if (date.isSame(now.clone().add(1, 'day'), 'day')) {
    return 2;
    // today
  } else if (date.isSame(now, 'day')) {
    return 3;
    // yesterday
  } else if (date.isSame(now.clone().subtract(1, 'day'), 'day')) {
    return 4;
    // earlier this week
  } else if (date.isSame(now, 'isoWeek')) {
    return 5;
    // last week
  } else if (date.isSame(now.clone().subtract(1, 'week'), 'isoWeek')) {
    return 6;
    // older
  }
  return 7;
}

// The title to be displayed for items in the group
// Return a nice title for the user to look at, from the group identifier
export function titleForGroup(group) {
  return [
    'Next Week',
    'Later This Week',
    'Tomorrow',
    'Today',
    'Yesterday',
    'Earlier This Week',
    'Last Week',
    'Older',
  ][group];
}
