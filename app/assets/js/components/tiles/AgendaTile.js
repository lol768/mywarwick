import React, { PropTypes } from 'react';

import { localMoment } from '../../dateFormatter';
import moment from 'moment-timezone';
import GroupedList from '../ui/GroupedList';
import Tile from './Tile';

import _ from 'lodash';

const groupItemsForAgendaTile = {

  description: 'by-date--agenda',

  groupForItem(item, now = localMoment()) {
    const date = localMoment(item.props.date).startOf('day');

    if (date.isSame(now, 'day')) {
      return 0; // today
    } else if (date.isSame(now.clone().add(1, 'day'), 'day')) {
      return 1; // tomorrow
    }
    return date.unix();
  },

  titleForGroup(group) {
    if (group < 2) {
      return [
        'Today',
        'Tomorrow',
      ][group];
    }
    return moment.unix(group).tz('Europe/London').format('ddd DD/MM/YY');
  },
};

export default class AgendaTile extends Tile {

  getBody(content) {
    const maxItemsToDisplay = this.props.maxItemsToDisplay ? this.props.maxItemsToDisplay : 3;
    const itemsToDisplay = this.isZoomed() ?
      content.items : _.take(content.items, maxItemsToDisplay);

    const events = itemsToDisplay.map(event => <AgendaTileItem key={event.id} {...event}/>);

    return (
      <GroupedList groupBy={groupItemsForAgendaTile}>
        {events}
      </GroupedList>
    );
  }

}

const AgendaTileItem = (props) => (
  <li className="agenda-item">
    <span className="agenda-item__title">{props.title}</span>
    <span className="agenda-item__date">{localMoment(props.date).format('HH:mm')}</span>
  </li>
);

AgendaTileItem.propTypes = {
  date: PropTypes.string,
  title: PropTypes.string,
};
