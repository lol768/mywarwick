import React from 'react';
import { createSelector } from 'reselect';
import warning from 'warning';
import _ from 'lodash-es';
import { formatDateTime, formatDate, formatTime, localMoment } from '../../../dateFormats';
import TileContent, { DEFAULT_TILE_SIZES, TILE_SIZES } from '../TileContent';
import DismissableInfoModal from '../../ui/DismissableInfoModal';
import LargeBody from './LargeBody';
import SingleEvent from './SingleEvent';
import ShowMore from '../ShowMore';

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

export default class AgendaTile extends TileContent {
  constructor(props) {
    super(props);
    this.agendaViewSelector = createSelector(_.identity, agendaViewTransform);
    this.showModal = this.showModal.bind(this);
    this.hideModal = this.hideModal.bind(this);
  }

  hideModal() {
    this.props.showModal(null);
  }

  modalMoreButton() {
    return null;
  }

  showModal(heading, subHeadings, body, href) {
    const modal = (<DismissableInfoModal
      heading={heading}
      subHeadings={subHeadings}
      onDismiss={this.hideModal}
      href={href}
      moreButton={this.modalMoreButton()}
    >
      {(body && body.length) ? _.map(_.filter(body, item => item.length > 0), (item, i) => (
        <p key={`body-${i}`}>{item}</p>
      )) : body}
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
            <SingleEvent event={event1} showModal={this.showModal} />
          </div>
          <div className="col-xs-6">
            <SingleEvent event={event2} showModal={this.showModal} />
            <ShowMore items={items} showing={2} onClick={this.props.onClickExpand} />
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
        <SingleEvent event={event} showModal={this.showModal} />
        <ShowMore items={items} showing={1} onClick={this.props.onClickExpand} />
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

  static renderSingleEventDate(event, options) {
    if (event.isAllDay) {
      return `All day ${formatDate(event.start)}`;
    }
    const DEFAULT_DATETIME_OPTIONS = { printToday: true };
    const DATETIME_OPTIONS = { ...DEFAULT_DATETIME_OPTIONS, ...options };
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
}
