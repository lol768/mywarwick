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

  numEventsToday(events) {
    const now = new Date().toISOString().slice(0, 10);
    const todayEvents = _.takeWhile(events, (e) => e.start.slice(0, 10) === now);
    return todayEvents.length;
  }

  getLargeBody(content) {
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

  getWideBody(content) {
    return this.getLargeBody(content);
  }

  getSmallBody(content) {
    const nextEvent = content.items[0];
    const truncTitle = _.trunc(nextEvent.title, { length: 30 });
    const text = (
      <span className="tile__text">
          Next: {truncTitle} at {localMoment(nextEvent.start).format('HH:mm')}
      </span>
    );

    return (
      <div className="tile__item">
        <span className="tile__callout">{this.numEventsToday(content.items)}
          <small> events today</small>
        </span>
        { nextEvent.href ?
          <a href={ nextEvent.href } target="_blank" onClick={ this.onClickLink }>
            { text }
          </a> :
          text
        }
      </div>
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

export class AgendaTileItem extends React.Component {
  render() {
    const { title, start, end, href, onClickLink } = this.props;

    const content = (
      <span>
        <span title={ title } className="agenda-item__title">{ title }</span>
        <span className="agenda-item__date">
          { end ? localMoment(start).format('HH:mm') : 'all-day' }
        </span>
      </span>
    );

    return (
      <li className="agenda-item">
        { href ?
          <a href={ href } target="_blank" onClick={ onClickLink }>
            { content }
          </a> :
          content
        }
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
