import React, { PropTypes } from 'react';

import { localMoment } from '../../dateFormatter';
import moment from 'moment-timezone';
import GroupedList from '../ui/GroupedList';
import TileContent from './TileContent';

import _ from 'lodash';

const DEFAULT_MAX_ITEMS = 5;

const groupItemsForAgendaTile = {

  description: 'by-date--agenda',

  groupForItem(item, now = localMoment()) {
    const date = localMoment(item.props.start).startOf('day');

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

export default class AgendaTile extends TileContent {

  constructor(props) {
    super(props);
    this.onClickLink = this.onClickLink.bind(this);
  }

  getBody(content) {
    const maxItemsToDisplay = this.props.maxItemsToDisplay ?
      this.props.maxItemsToDisplay : DEFAULT_MAX_ITEMS;
    const itemsToDisplay = this.props.zoomed ?
      content.items : _.take(content.items, maxItemsToDisplay);

    const events = itemsToDisplay.map(event =>
      <AgendaTileItem key={event.id} onClickLink={ this.onClickLink } {...event}/>);

    return (
      <GroupedList groupBy={groupItemsForAgendaTile}>
        {events}
      </GroupedList>
    );
  }

  onClickLink(e) {
    e.stopPropagation();
    if (this.props.editingAny) {
      e.preventDefault();
    }
  }

  static canZoom(content) {
    if (content && content.items) {
      return content.items.length > 1;
    }
    return false;
  }
}

class AgendaTileItem extends React.Component {

  render() {
    const { title, start, href, onClickLink } = this.props;
    return (
      <li className="agenda-item">
        { href ?
          <a href={ href } target="_blank" onClick={ onClickLink }>
            <span title={ title } className="agenda-item__title">{ title }</span>
          </a> :
          <span title={ title } className="agenda-item__title">{ title }</span>
        }
        <span className="agenda-item__date">{localMoment(start).format('HH:mm')}</span>
      </li>
    );
  }
}

AgendaTileItem.propTypes = {
  id: PropTypes.string,
  start: PropTypes.string,
  end: PropTypes.string,
  title: PropTypes.string,
  location: PropTypes.string,
  href: PropTypes.string,
  onClickLink: PropTypes.func,
};
