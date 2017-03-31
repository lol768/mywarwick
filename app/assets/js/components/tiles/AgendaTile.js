import React, { PropTypes } from 'react';
import { localMoment } from '../../dateFormats';
import moment from 'moment-timezone';
import GroupedList from '../ui/GroupedList';
import TileContent from './TileContent';
import _ from 'lodash-es';
import classNames from 'classnames';
import Hyperlink from '../ui/Hyperlink';
import { createSelector } from 'reselect';

const moduleColours = [
  '#00b2dd', // Bright Sky blue
  '#7ecbb6', // Bright Emerald green
  '#ef4050', // Bright Ruby red
  '#f47920', // Bright Burnt orange
  '#ffc233', // Bright Gold
];

const colourForModule = _.memoize(() => {
  const nextColour = moduleColours.shift();
  moduleColours.push(nextColour);
  return nextColour;
});

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

  return _.flow(
    i => _.flatMap(i, e => {
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
    }),
    i => _.filter(i, e => startOfToday.isBefore(e.start)),
    i => _.sortBy(i, e => e.start)
  )(items);
};

export default class AgendaTile extends TileContent {

  constructor(props) {
    super(props);
    this.state = {
      defaultMaxItems: { small: null, wide: 2, large: 5, tall: 100 }[props.size],
    };

    this.agendaViewSelector = createSelector(_.identity, agendaViewTransform);
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      defaultMaxItems: { small: null, wide: 2, large: 5, tall: 100 }[nextProps.size],
    });
  }

  numEventsToday(events) {
    const startOfToday = localMoment().startOf('day');
    const startOfTomorrow = localMoment().add(1, 'day').startOf('day');

    return _.flow(
      xs => _.filter(xs,
        e => localMoment(e.start).isBetween(startOfToday, startOfTomorrow, null, '[)')
      ),
      _.size,
    )(events);
  }

  getNextEvent(items) {
    const nextEvent = _.find(items, e => !e.isAllDay && localMoment(e.start).isSameOrAfter());
    const trunc = text => _.truncate(text, { length: 30 });

    if (!nextEvent) {
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
      text: `Next: ${trunc(nextEvent.title)} at ${localMoment(nextEvent.start).format('HH:mm')}`,
      href: nextEvent.href,
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
      <GroupedList className="tile-list-group" groupBy={groupItemsForAgendaTile}>
        {events}
      </GroupedList>
    );
  }

  getSmallBody() {
    const items = this.agendaViewSelector(this.props.content.items);

    const numEventsToday = this.numEventsToday(items);

    const callout = (
      <span>
        <span className="tile__callout">
          {numEventsToday}
        </span>
        &nbsp;event{numEventsToday === 1 ? null : 's'} today
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

  renderDate() {
    const { isAllDay, start, end, parent } = this.props;

    if (isAllDay) {
      return 'All day';
    }

    if (!parent || (start && !end)) {
      return localMoment(start).format('HH:mm');
    }

    return (
      <div>
        { localMoment(start).format('HH:mm') }&nbsp;–
        <br />
        { localMoment(end).format('HH:mm') }
      </div>
    );
  }

  renderMarker() {
    const { parent } = this.props;

    if (parent) {
      return (
        <div className="agenda-item__cell" style={{ paddingLeft: '.5em', paddingRight: '.25em' }}>
          <i className="fa fa-circle" style={{ color: colourForModule(parent.shortName) }}> </i>
        </div>
      );
    }

    return null;
  }

  renderParent() {
    const { parent } = this.props;

    if (parent) {
      return (
        <div>
          { parent.shortName } { parent.fullName }
        </div>
      );
    }

    return null;
  }

  renderTitle() {
    const { title, href } = this.props;

    return (
      <span title={ title } className={ classNames({
        'tile-list-item__title': true,
        'text--dotted-underline': href,
      }) }
      >
        <Hyperlink href={ href }>{ title }</Hyperlink>
      </span>
    );
  }

  renderLocation() {
    const { location } = this.props;

    if (!location) {
      return null;
    }

    if (location.href) {
      return (
        <span className="tile-list-item__location text--light">
          (<Hyperlink href={ location.href } className="text--dotted-underline">
            { location.name }
            &nbsp;
            <i className="fa fa-map-marker"> </i>
          </Hyperlink>)
        </span>
      );
    }

    return (
      <span className="tile-list-item__location text--light">
        ({ location.name })
      </span>
    );
  }

  renderStaff() {
    const { staff } = this.props;

    if (!staff || staff.length === 0) {
      return null;
    }

    return (
      <div className="text--translucent">
        <i className="fa fa-user-o"> </i>
        &nbsp;
        { staff.map(person => `${person.firstName} ${person.lastName}`).join(', ') }
      </div>
    );
  }

  render() {
    const content = (
      <div className="agenda-item">
        <div className="agenda-item__cell" style={{ paddingRight: '.25em' }}>
          { this.renderDate() }
        </div>
        { this.renderMarker() }
        <div className="agenda-item__cell" style={{ paddingLeft: '.5em' }}>
          { this.renderParent() }
          { this.renderTitle() }
          { ' ' }
          { this.renderLocation() }
          { this.renderStaff() }
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
  parent: React.PropTypes.shape({
    shortName: React.PropTypes.string,
    fullName: React.PropTypes.string,
  }),
  type: PropTypes.string,
  staff: React.PropTypes.arrayOf(React.PropTypes.shape({
    email: React.PropTypes.string,
    lastName: React.PropTypes.string,
    firstName: React.PropTypes.string,
    userType: React.PropTypes.string,
    universityId: React.PropTypes.string,
  })),
};
