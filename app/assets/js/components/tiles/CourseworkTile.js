import React from 'react';
import ListTile from './ListTile';
import formatDate, { localMoment } from '../../dateFormatter';
import _ from 'lodash';
import { Hyperlink } from './utilities';

export default class CourseworkTile extends ListTile {

  getSmallBody() {
    function numAssignmentsDue(assignments) {
      const nextMonth = localMoment().add(1, 'month');
      const items = _.takeWhile(assignments, (a) =>
        localMoment(a.date).isBefore(nextMonth));
      return items.length;
    }

    const { content } = this.props;
    const { text, date, href } = content.items[0];

    const nextAssignment = (
      <span className="tile__text">
        Next: <Hyperlink child={text} href={href} />, due {formatDate(date)}
      </span>
    );

    const numAssignments = numAssignmentsDue(content.items);

    const callout = (
      <span className="tile__callout">
        { numAssignments }
        <small> assignment{numAssignments === 1 ? null : 's'} in next month</small>
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
