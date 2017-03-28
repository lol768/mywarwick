import React, { PropTypes } from 'react';
import formatDateTime, { formatDate, formatTime, localMoment } from '../../dateFormats';
import moment from 'moment-timezone';
import GroupedList from '../ui/GroupedList';
import TileContent from './TileContent';
import _ from 'lodash';
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

    this.agendaViewSelector = createSelector(_.identity, agendaViewTransform);
  }

  getEventsToday() {
    const events = this.getAgendaViewItems();

    const startOfToday = localMoment().startOf('day');
    const startOfTomorrow = localMoment().add(1, 'day').startOf('day');

    return _.filter(events, e =>
      localMoment(e.start).isBetween(startOfToday, startOfTomorrow, null, '[)')
    );
  }

  getAgendaViewItems() {
    return this.agendaViewSelector(this.props.content.items);
  }

  getLargeBody() {
    const items = this.getAgendaViewItems();

    const events = items.map(event =>
      <AgendaTileItem key={event.id} {...event} />
    );

    return (
      <GroupedList className="tile-list-group" groupBy={groupItemsForAgendaTile}>
        {events}
      </GroupedList>
    );
  }

  static renderSingleEvent(event) {
    if (!event) {
      return null;
    }

    return (
      <Hyperlink href={ event.href } style={{ display: 'inline-block' }}>
        <div className="text-overflow-block">
          <i className="fa fa-fw fa-clock-o"> </i>
          { event.isAllDay ?
            `All day ${formatDate(event.start)}` :
            `${formatDateTime(event.start)}–${formatTime(event.end)}` }
        </div>
        <div className="text-overflow-block">
          <i className="fa fa-fw fa-calendar-check-o"> </i>
          { event.title }
        </div>
        { event.location &&
        <div className="text-overflow-block">
          <i className="fa fa-fw fa-map-marker"> </i>
          { event.location.name }
        </div>
        }
        { event.organiser &&
        <div className="text-overflow-block">
          <i className="fa fa-fw fa-user-o"> </i>
          { event.organiser.name }
        </div>
        }
      </Hyperlink>
    );
  }

  getWideBody() {
    const items = this.getEventsToday();
    const [event1, event2] = this.getAgendaViewItems();

    if (!event1) {
      return (
        <div>
          { this.props.defaultText }
        </div>
      );
    }

    return (
      <div className="container-fluid">
        <div className="row">
          <div className="col-xs-6">
            { AgendaTile.renderSingleEvent(event1) }
          </div>
          <div className="col-xs-6">
            { AgendaTile.renderSingleEvent(event2) }
            { items.length > 2 &&
            <div className="text-right">
              <a href="#" onClick={ this.props.onClickExpand }>
                +{ items.length - 2 } more
              </a>
            </div> }
          </div>
        </div>
      </div>
    );
  }

  getSmallBody() {
    const items = this.getEventsToday();
    const [event] = this.getAgendaViewItems();

    if (!event) {
      return (
        <div>
          { this.props.defaultText }
        </div>
      );
    }

    return (
      <div>
        { AgendaTile.renderSingleEvent(event) }
        { items.length > 1 &&
        <div className="text-right">
          <a href="#" onClick={ this.props.onClickExpand }>
            +{ items.length - 1 } more
          </a>
        </div> }
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
      return formatTime(start);
    }

    return (
      <div>
        { formatTime(start) }&nbsp;–
        <br />
        { formatTime(end) }
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
