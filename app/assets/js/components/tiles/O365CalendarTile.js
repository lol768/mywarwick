import React from 'react';
import AgendaTile from './AgendaTile';

export default class O365CalendarTile extends AgendaTile {
  contentOrDefault(contentFunction) {
    if (this.isEmpty()) {
      return (
        <div>
          <p>{this.props.content.defaultText}</p>
          <a className="text--dotted-underline" href="http://webcalendar.warwick.ac.uk/">
            Open calendar
          </a>
        </div>
      );
    }

    return contentFunction.call(this);
  }

  modalMoreButton() {
    return (
      <a
        type="button"
        className="btn btn-default"
        href="http://webcalendar.warwick.ac.uk/"
      >
        Open calendar
      </a>
    );
  }
}
