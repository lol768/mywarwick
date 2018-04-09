import * as React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash-es';
import Hyperlink from '../../ui/Hyperlink';
import AgendaTile from './AgendaTile';
import { eventPropType } from './constants';
import * as FA from '../../FA';

/**
 * Card component - display one or two for small and wide renditions.
 */
export default class SingleEvent extends React.PureComponent {
  static propTypes = {
    event: eventPropType,
    showModal: PropTypes.func.isRequired,
  };

  render() {
    const event = this.props.event;
    if (!event) {
      return null;
    }

    const { location, extraInfo, organiser, staff, href, parent, academicWeek } = event;

    const titleComponents = [];
    if (parent) {
      titleComponents.push(parent.shortName);
      titleComponents.push(parent.fullName);
    }
    if (event.title) {
      titleComponents.push(event.title);
    }
    const title = titleComponents.join(' ');

    const eventDate = AgendaTile.renderSingleEventDate(event);
    const list =
      (<ul className="list-unstyled">
        <li className="text-overflow-block agenda__date">
          <FA.Clock fw />
          { eventDate }
        </li>
        <li className="text-overflow-block">
          {extraInfo ?
            <i className="fa fa-fw fa-info-circle" /> :
            <i className="fa fa-fw fa-calendar-check-o" />
          }
          { title }
        </li>
        { !_.isEmpty(location) &&
        <li className="text-overflow-block">
          <FA.Map fw />
          { AgendaTile.getLocationString(location) }
        </li>
        }
        { (organiser || !_.isEmpty(staff)) &&
        <li className="text-overflow-block">
          <FA.User fw />
          { AgendaTile.renderUser({ organiser, staff }) }
        </li>
        }
      </ul>);

    if (extraInfo) {
      const locName = AgendaTile.getLocationString(location);
      return (
        <a
          role="button"
          onClick={() => this.props.showModal(
            title,
            [
              (<span><FA.Clock fw /> {AgendaTile.renderSingleEventDate(event, {shortDates: false})}</span>),
              academicWeek && (<span><FA.Calendar fw /> Week {academicWeek}</span>),
              locName && (<span><FA.Map fw /> {locName}</span>),
            ],
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
}
