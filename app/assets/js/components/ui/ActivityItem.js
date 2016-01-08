import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import formatDate from '../../dateFormatter';

import AppIcon from './AppIcon';

export default class ActivityItem extends ReactComponent {

  render() {
    return (
      <div className="activity-item">
        <div className="media">
          <div className="media-left">
            <AppIcon app={this.props.provider} size="lg"/>
          </div>
          <div className="media-body">
            <span className="activity-item__title">{this.props.title}</span>
            <span className="activity-item__text">{this.props.text}</span>
            {/*<span className="activity-item__source">{this.props.provider}</span>*/}

            {/* Pass undefined so function is called with default param */}
            <span className="activity-item__date">{formatDate(this.props.date, undefined, this.props.forceDisplayDay)}</span>
          </div>
        </div>
      </div>
    );
  }

}
