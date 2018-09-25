/* eslint-env browser */
import React from 'react';
import AgendaTile from './agenda/AgendaTile';

const WEBCAL_URL = 'http://webcalendar.warwick.ac.uk/';

export default class O365CalendarTile extends AgendaTile {
  constructor(props) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    window.open(WEBCAL_URL);
  }

  contentOrDefault(contentFunction) {
    if (this.isEmpty()) {
      return (
        <div>
          <p>{this.props.content.defaultText}</p>
          <a
            className="text--dotted-underline"
            role="button"
            tabIndex={0}
            title="Open calendar"
            onClick={this.handleClick}
          >
            Open calendar
          </a>
        </div>
      );
    }

    return contentFunction.call(this);
  }

  modalMoreButton() {
    return (
      <button
        type="button"
        className="btn btn-default"
        title="Open calendar"
        onClick={this.handleClick}
      >
        Open calendar
      </button>
    );
  }
}
