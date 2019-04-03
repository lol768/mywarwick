import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import classNames from 'classnames';
import { formatTime, localMoment } from '../../../dateFormats';
import Hyperlink from '../../ui/Hyperlink';
import GroupedList from '../../ui/GroupedList';
import AgendaTile from './AgendaTile';
import { eventPropType, eventShape } from './constants';
import * as FA from '../../FA';
import moment from 'moment-timezone';

const moduleColours = [
  '#00b2dd', // Bright Sky blue
  '#7ecbb6', // Bright Emerald green
  '#ef4050', // Bright Ruby red
  '#f47920', // Bright Burnt orange
  '#ffc233', // Bright Gold
];

/**
 * Returns the same colour for a given input, though only while
 * this module is in memory - it will change the next time.
 */
const colourForModule = _.memoize(() => {
  const nextColour = moduleColours.shift();
  moduleColours.push(nextColour);
  return nextColour;
});

const eventGrouping = {
  description: 'by-date--agenda',
  noRepeatSubtitle: true,

  groupForItem(item, now = localMoment()) {
    const date = localMoment(item.props.start).startOf('day');
    if (date.isSame(now, 'day')) {
      return 0; // today
    } else if (date.isSame(now.clone().add(1, 'day'), 'day')) {
      return 1; // tomorrow
    }
    return date.unix();
  },

  subtitleForGroup(items) {
    if (items.length === 1 && typeof items[0].props.currentWeek === 'number') {
      return `(week ${items[0].props.currentWeek})`;
    }
    return typeof items[0].props.academicWeek === 'number' ? `(week ${items[0].props.academicWeek})` : null;
  },

  titleForGroup(group, items = []) {
    if (items.length === 1 && typeof items[0].props.currentWeek === 'number') {
      return 'Today';
    }

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

export default class LargeBody extends React.PureComponent {
  static propTypes = {
    children: PropTypes.arrayOf(eventPropType).isRequired,
    showModal: PropTypes.func.isRequired,
    currentWeek: PropTypes.number,
  };

  render() {
    const { children, showModal } = this.props;
    const hasEventsThisWeek = !this.props.currentWeek ||
      _.find(children, c => c.academicWeek === this.props.currentWeek);
    const items = children.map(event =>
      <AgendaTileItem key={event.id} showModal={showModal} {...event} />,
    );
    return (
      <GroupedList className="tile-list-group" groupBy={eventGrouping}>
        { hasEventsThisWeek ?
          items
          :
          [<ThisWeekStubAgendaTileItem key="stub" currentWeek={this.props.currentWeek} />]
            .concat(items)
        }
      </GroupedList>
    );
  }
}

class ThisWeekStubAgendaTileItem extends React.PureComponent {
  static propTypes = {
    currentWeek: PropTypes.number.isRequired,
  };

  render() {
    return (
      <div className="tile-list-item">
        <div className="agenda-item">
          <div className="agenda-item__cell">
            <i>No events this week</i>
          </div>
        </div>
      </div>
    );
  }
}

export class AgendaTileItem extends React.PureComponent {
  static propTypes = {
    ...eventShape,
    showModal: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);
    this.handleShowModal = this.handleShowModal.bind(this);
  }

  handleShowModal() {
    const { showModal, title, location, extraInfo, href, academicWeek } = this.props;
    const locName = AgendaTile.getLocationString(location);
    const fullEventDate = AgendaTile.renderSingleEventDate(this.props, { shortDates: false });
    showModal(
      title,
      [
        (<span><FA.Clock fw /> {fullEventDate}</span>),
        typeof academicWeek === 'number' && (<span><FA.Calendar fw /> Week {academicWeek}</span>),
        locName && (<span><FA.Map fw /> {locName}</span>),
      ],
      (extraInfo ? extraInfo.split('\r\n') : extraInfo),
      href,
    );
  }

  renderDate() {
    const { isAllDay, start, end } = this.props;

    if (isAllDay) {
      return (
        <div className="agenda-item__cell__times">
          All day
        </div>
      );
    }

    if ((start && !end) || localMoment(start).isSame(localMoment(end))) {
      return (
        <div className="agenda-item__cell__times">
          <span className="agenda-item__cell__times__start-time">{ formatTime(start) }</span>
        </div>
      );
    }

    return (
      <div className="agenda-item__cell__times">
        <span className="agenda-item__cell__times__start-time">{ formatTime(start) }</span>
        <br />
        <span className="agenda-item__cell__times__end-time">{ formatTime(end) }</span>
      </div>
    );
  }

  renderMarker() {
    const { parent } = this.props;
    const style = (parent) ? { color: colourForModule(parent.shortName) } : {};

    return (
      <div className="agenda-item__cell marker" style={{ paddingLeft: '.5em', paddingRight: '.25em' }}>
        <i className={`fal fa-circle ${(parent) ? '' : 'invisible'}`} style={style} />
      </div>
    );
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
            <FA.Map />
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
    const renderedUser = AgendaTile.renderUser(this.props);
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
              <i className="fa fa-fw fa-info-circle" />
            </a>
            : <Hyperlink href={href}>{ this.renderTitle() }</Hyperlink> }
          { ' ' }
          { this.renderLocation() }
          { renderedUser &&
            <div className="text--translucent tile-list-item__organiser">
              <FA.User /> {renderedUser}
            </div>
          }
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
