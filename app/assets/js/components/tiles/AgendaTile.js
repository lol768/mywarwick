import React, { PropTypes } from 'react';

import { localMoment } from '../../dateFormatter';
import moment from 'moment-timezone';
import GroupedList from '../ui/GroupedList';
import TileContent from './TileContent';
import _ from 'lodash';

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
    this.state = {
      defaultMaxItems: { small: null, wide: 2, large: 5 }[props.size],
    };
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      defaultMaxItems: { small: null, wide: 2, large: 5 }[nextProps.size],
    });
  }

  numEventsToday(events) {
    const now = localMoment();
    const todayEvents = _.takeWhile(events, (e) => localMoment(e.start).isSame(now, 'day'));
    return todayEvents.length;
  }

  getLargeBody() {
    const { content } = this.props;

    const maxItemsToDisplay = this.props.maxItemsToDisplay || this.state.defaultMaxItems;
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

  getSmallBody() {
    const { content } = this.props;

    const nextEvent = content.items[0];
    const truncTitle = _.trunc(nextEvent.title, { length: 30 });
    const text = (
      <span className="tile__text">
        Next: {truncTitle} at {localMoment(nextEvent.start).format('HH:mm')}
      </span>
    );

    const numEventsToday = this.numEventsToday(content.items);

    const callout = (
      <span className="tile__callout">
        {numEventsToday}
        <small> event{numEventsToday === 1 ? null : 's'} today</small>
      </span>
    );

    if (numEventsToday === 0) {
      return (
        <div className="tile__item">
          { callout }
        </div>
      );
    }

    return (
      <div className="tile__item">
        { callout }
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
        <span title={ title } className="tile-list-item__title text--dotted-underline">{ title }</span>
        <span className="tile-list-item__date">
          { end ? localMoment(start).format('HH:mm') : 'all-day' }
        </span>
      </span>
    );

    return (
      <div className="tile-list-item">
        { href ?
          <a href={ href } target="_blank" onClick={ onClickLink }>
            { content }
          </a> :
          content
        }
      </div>
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
