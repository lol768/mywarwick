import React, { PropTypes } from 'react';
import { localMoment } from '../../dateFormats';
import moment from 'moment-timezone';
import GroupedList from '../ui/GroupedList';
import TileContent from './TileContent';
import _ from 'lodash';
import classNames from 'classnames';
import Hyperlink from '../ui/Hyperlink';
import { createSelector } from 'reselect';

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

// Create an agenda view for the given calendar events.  All-day events
// spanning multiple days appear on each day.  Events are sorted by start
// time, and any events ending before the start of the current day are
// excluded.
const agendaViewTransform = (items) => {
  const startOfToday = localMoment().startOf('day');

  return _(items)
    .flatMap(e => {
      if (e.isAllDay) {
        const date = localMoment(e.start);
        const end = localMoment(e.end);

        const instances = [];

        while (date.isBefore(end)) {
          instances.push({
            ...e,
            start: date.format(),
          });

          date.add(1, 'day');
        }

        return instances;
      }

      return e;
    })
    .filter(e => startOfToday.isBefore(e.start))
    .sortBy(e => e.start)
    .value();
};

export default class AgendaTile extends TileContent {

  constructor(props) {
    super(props);
    this.state = {
      defaultMaxItems: { small: null, wide: 2, large: 5 }[props.size],
    };

    this.agendaViewSelector = createSelector(_.identity, agendaViewTransform);
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      defaultMaxItems: { small: null, wide: 2, large: 5 }[nextProps.size],
    });
  }

  numEventsToday(events) {
    const startOfToday = localMoment().startOf('day');
    const startOfTomorrow = localMoment().add(1, 'day').startOf('day');

    return _(events)
      .filter(e => localMoment(e.start).isBetween(startOfToday, startOfTomorrow, null, '[)'))
      .size();
  }

  getNextEvent(items) {
    const timedEvent = _.find(items, e => !e.isAllDay);
    const trunc = text => _.truncate(text, { length: 30 });

    if (!timedEvent) {
      // items are all-day events
      if (items.length === 1) {
        return {
          text: `All day: ${trunc(items[0].title)}`,
          href: items[0].href,
        };
      }
      return {
        text: `You have ${items.length} all day events`,
      };
    }

    return {
      text: `Next: ${trunc(timedEvent.title)} at ${localMoment(timedEvent.start).format('HH:mm')}`,
      href: timedEvent.href,
    };
  }

  getLargeBody() {
    const items = this.agendaViewSelector(this.props.content.items);

    const maxItemsToDisplay = this.props.maxItemsToDisplay || this.state.defaultMaxItems;
    const itemsToDisplay = this.props.zoomed ?
      items : _.take(items, maxItemsToDisplay);

    const events = itemsToDisplay.map(event =>
      <AgendaTileItem key={event.id} {...event} />
    );

    return (
      <GroupedList groupBy={groupItemsForAgendaTile}>
        {events}
      </GroupedList>
    );
  }

  getSmallBody() {
    const items = this.agendaViewSelector(this.props.content.items);

    const numEventsToday = this.numEventsToday(items);

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

    // only getNextEvent when we know numEventsToday > 0
    const { text, href } = this.getNextEvent(items);

    return (
      <div className="tile__item">
        { callout }
        <Hyperlink href={href}>
          <span className="tile__text">
           { text }
          </span>
        </Hyperlink>
      </div>
    );
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
    const { title, start, isAllDay, href, location } = this.props;

    const content = (
      <div>
        <div className="col-xs-2">
          { isAllDay ? 'All day' : localMoment(start).format('HH:mm') }
        </div>
        <div className="col-xs-10">
          <span title={title}
            className={classNames('tile-list-item__title', 'text--align-bottom',
            { 'text--dotted-underline': href })}
          >
            <Hyperlink href={href}>{ title }</Hyperlink>
         </span>
          {location ?
            <span className="tile-list-item__location text--align-bottom text--light">
              &nbsp;- <Hyperlink href={ location.href }>{ location.name }</Hyperlink>
            </span>
            : null}
        </div>
      </div>
    );

    return (
      <div className="tile-list-item">
        { content }
      </div>
    );
  }
}

AgendaTileItem.propTypes = {
  id: PropTypes.string,
  start: PropTypes.string,
  end: PropTypes.string,
  isAllDay: PropTypes.bool,
  title: PropTypes.string,
  location: React.PropTypes.shape({
    name: React.PropTypes.string,
    href: React.PropTypes.string,
  }),
  href: PropTypes.string,
};
