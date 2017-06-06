import React, { PropTypes } from 'react';
import Hyperlink from './Hyperlink';
import * as dateFormats from '../../dateFormats';
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
      { (props.mutable) ?
        <div className="muting" onClick={ props.onMuting }>
          <i className="fa fa-chevron-down"></i>
        </div> : null
      }
      <Hyperlink href={ props.url }>
        <div>
          <div className="media">
            <div className="media-left">
              <AppIcon icon={ props.icon } size="lg" />
            </div>
            <div className="media-body">
              <div className="activity-item__title">{ props.title }</div>
              <div className="activity-item__text">{ props.text }</div>

              <div className="activity-item__date">
                { dateFormats.forActivity(props.date, props.grouped) }
              </div>
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
  providerDisplayName: PropTypes.string,
  title: PropTypes.string.isRequired,
  text: PropTypes.string,
  date: PropTypes.string.isRequired,
  url: PropTypes.string,
  unread: PropTypes.bool,
  icon: React.PropTypes.shape({
    name: React.PropTypes.string,
    colour: React.PropTypes.string,
  }),
  grouped: PropTypes.bool,
  mutable: PropTypes.bool,
  onMuting: PropTypes.func,
};

