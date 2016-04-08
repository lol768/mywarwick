import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import formatDate from '../../dateFormatter';

import classnames from 'classnames';

import AppIcon from './AppIcon';

export default class ActivityItem extends ReactComponent {

  render() {
    const content = (
      <div className="media">
        <div className="media-left">
          <AppIcon app={this.props.provider} size="lg" />
        </div>
        <div className="media-body">
          <span className="activity-item__date">
            { formatDate(this.props.date, undefined, this.props.forceDisplayDay) }
          </span>

          <span className="activity-item__title">{ this.props.title }</span>
          <span className="activity-item__text">{ this.props.text }</span>
        </div>
      </div>
    );

    const classNames = classnames(
      'activity-item',
      {
        'activity-item--with-url': this.props.url,
        'activity-item--unread': this.props.unread,
      }
    );

    return (
      <div className={ classNames }>
        { this.props.url ?
          <a href={ this.props.url }>{content}</a>
          : content }
      </div>
    );
  }

}
