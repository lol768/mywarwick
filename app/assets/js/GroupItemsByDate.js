import { localMoment } from './dateFormats';
import * as invariant from 'invariant';

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
  } else if (date.isSame(tomorrow, 'day')) {
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

// Speed things up by assuming that items on the same date are in the same group
// (which they are!)
export function getGroupedItems(items, now) {
  // Precondition: items are sorted by date (direction doesn't matter)

  const groups = [];

  let currentDate = null;
  let currentGroup = null;
  let currentGroupItems = null;

  items.forEach(item => {
    const date = localMoment(item.props.date);

    if (!date.isSame(currentDate, 'day')) {
      // This item has a different date to the one before it
      currentDate = date;

      const newGroup = groupForItem(item, now);

      if (newGroup !== currentGroup) {
        if (currentGroup !== null) {
          invariant(groups.find((pair) => pair[0] === newGroup) !== undefined,
            'Tried to create new group with existing group number. '
             + 'Normally caused by items not in order'
          );
          // The previous group is finished; add it to the list
          groups.push([currentGroup, currentGroupItems]);
        }

        // Start putting items into a new group
        currentGroup = newGroup;
        currentGroupItems = [];
      }
    }

    currentGroupItems.push(item);
  });

  if (currentGroup !== null) {
    // The previous group is finished; add it to the list
    groups.push([currentGroup, currentGroupItems]);
  }

  return groups;
}
