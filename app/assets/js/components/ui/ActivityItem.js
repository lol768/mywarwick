import React from 'react';
import * as PropTypes from 'prop-types';
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
    icon: PropTypes.shape({
      name: PropTypes.string,
      colour: PropTypes.string,
    }),
    grouped: PropTypes.bool,
    muteable: PropTypes.bool,
    onMuting: PropTypes.func,
  };

  constructor(props) {
    super(props);
    this.onMuting = this.onMuting.bind(this);
  }

  onMuting() {
    this.props.onMuting(this.props);
  }

  render() {
    const classNames = classnames('activity-item',
      {
        'activity-item--with-url': this.props.url,
        'activity-item--unread': this.props.unread,
      }
    );

    return (
      <div className={ classNames }>
        { (this.props.muteable) ?
          <div className="muting" onClick={ this.onMuting }>
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
