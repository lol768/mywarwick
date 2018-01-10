import React from 'react';
import * as PropTypes from 'prop-types';
import moment from 'moment-timezone';
import { createSelector } from 'reselect';
import warning from 'warning';
import _ from 'lodash-es';
import classNames from 'classnames';
import GroupedList from '../ui/GroupedList';
import { formatDateTime, formatDate, formatTime, localMoment } from '../../dateFormats';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from './TileContent';
import Hyperlink from '../ui/Hyperlink';
import DismissableInfoModal from '../ui/DismissableInfoModal';

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
    switch (parseInt(group, 10)) {
      case 0: return 'Today';
      case 1: return 'Tomorrow';
      default: {
        const date = moment.unix(group).tz('Europe/London');

        if (date.isSame(moment(), 'year')) {
          return date.format('ddd Do MMMM');
        }

        return date.format('ddd Do MMMM YYYY');
      }
    }
  },
};

// Create an agenda view for the given calendar events.  All-day events
// spanning multiple days appear on each day.  Events are sorted by start
// time, and any events ending before the start of the current day are
// excluded.
const agendaViewTransform = (items) => {
  const startOfToday = localMoment().startOf('day');

  return _.flow(
    i => _.flatMap(i, (e) => {
      if (e.isAllDay) {
        const date = localMoment(e.start);
        const end = (e.end !== undefined) ? localMoment(e.end) : localMoment(e.start);

        const instances = [];

        while (date.isBefore(end)) {
          instances.push({
            ...e,
            start: date.format(),
          });

          date.add(1, 'day');
        }

        if (instances.length === 0) {
          instances.push({
            ...e,
            start: date.format(),
          });
        }

        return instances;
      }

      return e;
    }),
    i => _.filter(i, e => startOfToday.isBefore(e.start)),
    i => _.sortBy(i, e => e.start),
  )(items);
};

const FAClock = props => <i className={`fa fa-clock-o${props.fw ? ' fa-fw' : ''}`} />; // eslint-disable-line react/prop-types
const FAMap = props => <i className={`fa fa-map-marker${props.fw ? ' fa-fw' : ''}`} />; // eslint-disable-line react/prop-types
const FAUser = props => <i className={`fa fa-user-o${props.fw ? ' fa-fw' : ''}`} />; // eslint-disable-line react/prop-types

export default class AgendaTile extends TileContent {
  constructor(props) {
    super(props);
    this.agendaViewSelector = createSelector(_.identity, agendaViewTransform);
    this.showModal = this.showModal.bind(this);
    this.hideModal = this.hideModal.bind(this);
    this.renderSingleEvent = this.renderSingleEvent.bind(this);
  }

  hideModal() {
    this.props.showModal(null);
  }

  showModal(heading, subHeading, body, href) {
    const modal = (<DismissableInfoModal
      heading={heading}
      subHeading={subHeading}
      onDismiss={this.hideModal}
      href={href}
    >
      {body}
    </DismissableInfoModal>);
    this.props.showModal(modal);
  }

  getEventsToday() {
    const events = this.getAgendaViewItems();

    const startOfToday = localMoment().startOf('day');
    const startOfTomorrow = localMoment().add(1, 'day').startOf('day');

    return _.filter(events, e =>
      localMoment(e.start).isBetween(startOfToday, startOfTomorrow, null, '[)'),
    );
  }

  getAgendaViewItems() {
    return this.agendaViewSelector(this.props.content.items);
  }

  getLargeBody() {
    const items = this.getAgendaViewItems();
    return <LargeBody showModal={this.showModal}>{ items }</LargeBody>;
  }

  static renderSingleEventDate(event) {
    if (event.isAllDay) {
      return `All day ${formatDate(event.start)}`;
    }
    const DATETIME_OPTIONS = { printToday: true, onlyWeekday: true };
    const renderedStart = formatDateTime(event.start, undefined, DATETIME_OPTIONS);
    return event.end === undefined || event.start === event.end ?
      renderedStart : `${renderedStart}–${formatTime(event.end)}`;
  }

  static getLocationString(location) {
    if (!location) {
      return null;
    }

    if (_.isArray(location)) {
      return _.join(_.map(location, 'name'), ', ');
    }

    return location.name;
  }

  renderSingleEvent(event) {
    if (!event) {
      return null;
    }

    const { title, location, extraInfo, organiser, staff, href } = event;

    const eventDate = AgendaTile.renderSingleEventDate(event);
    const list =
      (<ul className="list-unstyled">
        <li className="text-overflow-block agenda__date">
          <FAClock fw />
          { eventDate }
        </li>
        <li className="text-overflow-block">
          <i className="fa fa-fw fa-calendar-check-o" />
          { title }
        </li>
        { !_.isEmpty(location) &&
        <li className="text-overflow-block">
          <FAMap fw />
          { AgendaTile.getLocationString(location) }
        </li>
        }
        { (organiser || staff) &&
        <li className="text-overflow-block">
          <FAUser fw />
          { AgendaTile.renderUser({ organiser, staff }) }
        </li>
        }
      </ul>);

    if (extraInfo) {
      return (
        <a
          role="button"
          onClick={() => this.showModal(
            title,
            <span> <FAClock /> {eventDate} <FAMap /> {location.name}</span>,
            extraInfo,
            href,
          )}
          target="_blank"
          tabIndex={0}
          style={{ display: 'block' }}
        >
          { list }
        </a>
      );
    }

    return (
      <Hyperlink href={ href } style={{ display: 'block' }}>
        { list }
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
            { this.renderSingleEvent(event1) }
          </div>
          <div className="col-xs-6">
            { this.renderSingleEvent(event2) }
            { items.length > 2 &&
            <div className="text-right">
              <a
                role="button"
                tabIndex={0}
                onClick={ this.props.onClickExpand }
                onKeyUp={ this.props.onClickExpand }
              >
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
        { this.renderSingleEvent(event) }
        { items.length > 1 &&
        <div className="text-right">
          <a
            role="button"
            tabIndex={0}
            onClick={ this.props.onClickExpand }
            onKeyUp={ this.props.onClickExpand }
          >
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

  static supportedTileSizes() {
    return DEFAULT_TILE_SIZES.concat([TILE_SIZES.LARGE, TILE_SIZES.TALL]);
  }

  static renderUser(event) {
    const { staff, organiser } = event;

    warning(
      !(staff && organiser),
      'Event has both staff and organiser set - only one should be used: %s',
      this.props,
    );

    const users = staff || (organiser && [organiser]);

    if (!users || users.length === 0) {
      return null;
    }

    function personToString(person) {
      return person.firstName ?
        `${person.firstName} ${person.lastName}`
        : person.name;
    }

    return users.map(personToString).join(', ');
  }
}

export class LargeBody extends React.PureComponent {
  static propTypes = {
    children: PropTypes.arrayOf(PropTypes.shape(AgendaTileItemPropTypes)).isRequired,
    showModal: PropTypes.func.isRequired,
  };

  render() {
    const { children, showModal } = this.props;
    return (
      <GroupedList className="tile-list-group" groupBy={groupItemsForAgendaTile}>
        {children.map(event =>
          <AgendaTileItem key={event.id} showModal={showModal} {...event} />,
        )}
      </GroupedList>
    );
  }
}

export class AgendaTileItem extends React.PureComponent {
  constructor(props) {
    super(props);
    this.handleShowModal = this.handleShowModal.bind(this);
  }

  handleShowModal() {
    const { showModal, title, location: { name: locName }, extraInfo, href } = this.props;
    showModal(
      title,
      <span> <FAClock /> {AgendaTile.renderSingleEventDate(this.props)} <FAMap /> {locName}</span>,
      extraInfo,
      href,
    );
  }

  renderDate() {
    const { isAllDay, start, end } = this.props;

    if (isAllDay) {
      return 'All day';
    }

    if ((start && !end) || start === end) {
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
          <i className="fa fa-circle" style={{ color: colourForModule(parent.shortName) }} />
        </div>
      );
    }

    return null;
  }

  renderTitle() {
    const { title, href, extraInfo, parent } = this.props;

    return (
      <span>
        {parent && <div>{parent.shortName} {parent.fullName}</div>}
        <span
          title={title}
          className={classNames({
            'tile-list-item__title': true,
            'text--dotted-underline': extraInfo || href,
          })}
        >
          {title}
        </span>
      </span>
    );
  }

  renderLocation() {
    const { location } = this.props;

    if (_.isEmpty(this.props.location)) {
      return null;
    }

    if (location.href) {
      return (
        <span className="tile-list-item__location text--light">
          <Hyperlink href={ location.href } className="text--dotted-underline">
            { location.name }
          &nbsp;
            <FAMap />
          </Hyperlink>
        </span>
      );
    }

    return (
      <span className="tile-list-item__location text--light">
        { AgendaTile.getLocationString(location) }
      </span>
    );
  }

  render() {
    const { extraInfo, href } = this.props;

    const content = (
      <div className="agenda-item">
        <div className="agenda-item__cell" style={{ paddingRight: '.25em' }}>
          { this.renderDate() }
        </div>
        { this.renderMarker() }
        <div className="agenda-item__cell" style={{ paddingLeft: '.5em' }}>
          { extraInfo ?
            <a role="button" tabIndex={0} onClick={this.handleShowModal} target="_blank">
              { this.renderTitle() }
            </a>
            : <Hyperlink href={href}>{ this.renderTitle() }</Hyperlink> }
          { ' ' }
          { this.renderLocation() }
          {<div className="text--translucent tile-list-item__organiser">
            { <FAUser /> }
            { AgendaTile.renderUser(this.props) }
          </div>}
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

const AgendaTileItemPropTypes = {
  id: PropTypes.string,
  start: PropTypes.string,
  end: PropTypes.string,
  isAllDay: PropTypes.bool,
  title: PropTypes.string,
  organiser: PropTypes.shape({
    name: PropTypes.string,
  }),
  location: PropTypes.shape({
    name: PropTypes.string,
    href: PropTypes.string,
  }),
  href: PropTypes.string,
  parent: PropTypes.shape({
    shortName: PropTypes.string,
    fullName: PropTypes.string,
  }),
  type: PropTypes.string,
  extraInfo: PropTypes.string,
  staff: PropTypes.arrayOf(PropTypes.shape({
    email: PropTypes.string,
    lastName: PropTypes.string,
    firstName: PropTypes.string,
    userType: PropTypes.string,
    universityId: PropTypes.string,
  })),
  showModal: PropTypes.func.isRequired,
};

AgendaTileItem.propTypes = AgendaTileItemPropTypes;
