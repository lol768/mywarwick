import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import _ from 'lodash';

import ListHeader from './ListHeader';
import { localMoment } from '../../dateFormatter';
import moment from 'moment-timezone';

export default class GroupedAgendaList extends ReactComponent {

  render() {

    // Group the child nodes using the object passed to the groupBy property
    let groups = _(this.props.children)
    // don't pass directly to groupBy - tramples on your default args.
      .groupBy(obj => groupForItem(obj))
      .pairs()
      .sortBy(([group, items]) => group)
      .map(([group, items]) => (
        // Title the group with a list header
        <div key={'group-' + group} className="list-group">
          <ListHeader key={'group-header-' + group} title={listHeadingForGroup(group)}/>
          {items}
        </div>
      ))
      .value();

    return (
      <ul
        className={"list-group list-group--grouped list-group--grouped-" + description}>
        {groups}
      </ul>
    );
  }

}

function groupForItem(item, now = localMoment()) {
  var date = localMoment(item.props.date).startOf('day');

  // today
  if (date.isSame(now, 'day')) {
    return 0;
  } // tomorrow
  else if (date.isSame(now.clone().add(1, 'day'), 'day')) {
    return 1;
  }
  else {
    return date.unix();
  }
}

function listHeadingForGroup(group) {
  if (group < 2) {
    return [
      "Today",
      "Tomorrow"
    ][group]
  } else {
    let groupDate = moment.unix(group).tz('Europe/London');
    return groupDate.format("ddd DD/MM/YY");
  }
}

