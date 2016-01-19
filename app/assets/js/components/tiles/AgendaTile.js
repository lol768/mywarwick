import React from 'react';

import { formatDate, localMoment } from '../../dateFormatter';
import moment from 'moment-timezone';
import GroupedList from '../ui/GroupedList';
import * as groupItemsByDate from '../../GroupItemsByDate';
import Tile from './Tile';

import _ from 'lodash';

export default class AgendaTile extends Tile {

  getBody(content) {
    let maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    let itemsToDisplay = this.isZoomed() ? content.items : _.take(content.items, maxItemsToDisplay);

    let events = itemsToDisplay.map(event => <AgendaTileItem key={`${event.title}-${event.date}`} {...event}/>);

    return (
      <GroupedList groupBy={groupItemsForAgendaTile}>
        {events}
      </GroupedList>
    );
  }

}

let AgendaTileItem = (props) => (
  <li className="agenda-item">
    <span className="agenda-item__title">{props.title}</span>
    <span className="agenda-item__date">{localMoment(props.date).format("HH:mm")}</span>
  </li>
);

let groupItemsForAgendaTile = {

  description: 'by-date--agenda',

  groupForItem(item, now = localMoment()) {
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
  },

  titleForGroup(group) {
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
};