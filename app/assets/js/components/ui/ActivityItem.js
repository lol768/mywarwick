import React, { PropTypes } from 'react';
import Hyperlink from './Hyperlink';
import formatDate from '../../dateFormatter';
import classnames from 'classnames';
import AppIcon from './AppIcon';

const ActivityItem = (props) => {
  const classNames = classnames('activity-item',
    {
      'activity-item--with-url': props.url,
      'activity-item--unread': props.unread,
    }
  );

  return (
    <div className={ classNames }>
      <Hyperlink href={ props.url }>
        <div className="media">
          <div className="media-left">
            <AppIcon app={ props.provider } size="lg" />
          </div>
          <div className="media-body">
            <div className="activity-item__title">{ props.title }</div>
            <div className="activity-item__text">{ props.text }</div>

            <div className="activity-item__date">
              { formatDate(props.date, undefined, props.forceDisplayDay) }
            </div>
          </div>
        </div>
      </Hyperlink>
    </div>
  );
};

export default ActivityItem;

ActivityItem.displayName = 'ActivityItem';
ActivityItem.propTypes = {
  provider: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  text: PropTypes.string,
  date: PropTypes.string.isRequired,
  url: PropTypes.string,
  unread: PropTypes.bool,
  forceDisplayDay: PropTypes.bool,
};
