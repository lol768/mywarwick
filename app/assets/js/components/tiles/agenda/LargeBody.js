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

export default class LargeBody extends React.PureComponent {
  static propTypes = {
    children: PropTypes.arrayOf(eventPropType).isRequired,
    showModal: PropTypes.func.isRequired,
  };

  render() {
    const { children, showModal } = this.props;
    return (
      <GroupedList className="tile-list-group" groupBy={eventGrouping}>
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
    const { showModal, title, location, extraInfo, href } = this.props;
    showModal(
      title,
      [
        (
          <span><FA.Clock /> {AgendaTile.renderSingleEventDate(this.props)}</span>
        ),
        location && (
          <span><FA.Map /> {location.name}</span>
        ),
      ],
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

  static propTypes = {
    ...eventShape,
    showModal: PropTypes.func.isRequired,
  }
}