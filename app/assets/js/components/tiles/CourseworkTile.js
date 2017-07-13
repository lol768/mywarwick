import React from 'react';
import _ from 'lodash-es';
import ListTile from './ListTile';
import Hyperlink from '../ui/Hyperlink';
import { formatDateTime, localMoment } from '../../dateFormats';

export default class CourseworkTile extends ListTile {
  getSmallBody() {
    function numAssignmentsDue(assignments) {
      const nextMonth = localMoment().add(1, 'month');
      const items = _.takeWhile(assignments, a =>
        localMoment(a.date).isBefore(nextMonth));
      return items.length;
    }

    const { content } = this.props;
    const { text, date, href } = content.items[0];

    const nextAssignment = (
      <span className="tile__text">
        Next: <Hyperlink href={href} >{ text }</Hyperlink>, due {formatDateTime(date)}
      </span>
    );

    const numAssignments = numAssignmentsDue(content.items);

    const callout = (
      <span>
        <span className="tile__callout">
          { numAssignments }
        </span>
        &nbsp;assignment{numAssignments === 1 ? null : 's'} in next month
      </span>
    );

    if (numAssignments === 0) {
      return (
        <div className="tile__item">
          { callout }
        </div>
      );
    }

    return (
      <div className="tile__item">
        { callout }
        { nextAssignment }
      </div>
    );
  }
}
