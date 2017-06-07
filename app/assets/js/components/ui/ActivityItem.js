import React, { PropTypes } from 'react';
import Hyperlink from './Hyperlink';
import * as dateFormats from '../../dateFormats';
import classnames from 'classnames';
import AppIcon from './AppIcon';

class ActivityItem extends React.PureComponent {

  static propTypes = {
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

  render() {
    const classNames = classnames('activity-item',
      {
        'activity-item--with-url': this.props.url,
        'activity-item--unread': this.props.unread,
      }
    );

    return (
      <div className={ classNames }>
        { (this.props.mutable) ?
          <div className="muting" onClick={ () => this.props.onMuting(this.props) }>
            <i className="fa fa-chevron-down"></i>
          </div> : null
        }
        <Hyperlink href={ this.props.url }>
          <div>
            <div className="media">
              <div className="media-left">
                <AppIcon icon={ this.props.icon } size="lg" />
              </div>
              <div className="media-body">
                <div className="activity-item__title">{ this.props.title }</div>
                <div className="activity-item__text">{ this.props.text }</div>

                <div className="activity-item__date">
                  { dateFormats.forActivity(this.props.date, this.props.grouped) }
                </div>
              </div>
            </div>
          </div>
        </Hyperlink>
      </div>
    );
  }
}

export default ActivityItem;
