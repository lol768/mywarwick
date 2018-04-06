import classnames from 'classnames';
import React from 'react';
import * as PropTypes from 'prop-types';
import Hyperlink from './Hyperlink';
import * as dateFormats from '../../dateFormats';
import AppIcon from './AppIcon';
import wrapKeyboardSelect from '../../keyboard-nav';

class ActivityItem extends React.PureComponent {
  static propTypes = {
    provider: PropTypes.string.isRequired,
    providerDisplayName: PropTypes.string,
    title: PropTypes.string.isRequired,
    text: PropTypes.string,
    textAsHtml: PropTypes.string,
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

  onMuting(e) {
    wrapKeyboardSelect(() => this.props.onMuting(this.props), e);
  }

  render() {
    const classNames = classnames('activity-item',
      {
        'activity-item--with-url': this.props.url,
        'activity-item--unread': this.props.unread,
      },
    );

    return (
      <div className={ classNames }>
        { (this.props.muteable) ?
          <div className="muting" onClick={ this.onMuting } onKeyUp={ this.onMuting } role="button" tabIndex={0}>
            <i className="fa fa-chevron-down" />
          </div> : null
        }
        <Hyperlink href={ this.props.url }>
          <div>
            <div className="media">
              <div className="media-left">
                <AppIcon icon={ this.props.icon } size="lg" />
              </div>
              <div className="media-body">
                <div className="activity-item__title">
                  { this.props.title }
                  { this.props.url && <i className="fa fa-external-link activity-item__link-indicator" /> }
                </div>
                { (this.props.textAsHtml) ?
                  <div className="activity-item__text"
                       dangerouslySetInnerHTML={{ __html: this.props.textAsHtml }}/> :
                  (this.props.text) && <div className="activity-item__text">{this.props.text}</div>
                }

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
