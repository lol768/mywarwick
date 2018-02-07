import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';
import NetworkAwareControl from './NetworkAwareControl';
import wrapKeyboardSelect from '../../keyboard-nav';

export default class ListGroupItem extends React.PureComponent {
  static propTypes = {
    icon: PropTypes.string,
    description: PropTypes.string.isRequired,
    onClick: PropTypes.func,
    loading: PropTypes.bool,
    failure: PropTypes.bool,
    disabled: PropTypes.bool,
    uiControl: PropTypes.any,
    settingColour: PropTypes.number,
  };

  static defaultProps = {
    loading: false,
    failure: false,
    disabled: false,
  };

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
    this.isClickable = this.isClickable.bind(this);
  }

  onClick(e) {
    wrapKeyboardSelect(() => {
      if (this.isClickable()) {
        this.props.onClick();
      }
    }, e);
  }

  isClickable() {
    return typeof this.props.onClick === 'function' && !this.props.disabled;
  }

  render() {
    return (
      <div
        className={`list-group-item ${
          ('settingColour' in this.props) ? `setting-colour-${this.props.settingColour}` : ''
        }`}
        role="button"
        aria-disabled={ !this.isClickable() }
        tabIndex={ this.isClickable() ? 0 : -1 }
        onClick={ this.onClick }
        onKeyUp={ this.onClick }
      >
        <div className="media">
          { this.props.icon &&
          <div className="media-left">
            <i className={ `fa fa-fw fa-${this.props.icon}` } />
          </div> }
          <div
            className={ classNames('media-body', { 'media-body-disabled': this.props.disabled }) }
          >
            { this.props.description }
          </div>
          <div className="media-right">
            <NetworkAwareControl {...this.props}>
              { this.props.uiControl }
            </NetworkAwareControl>
          </div>
        </div>
      </div>
    );
  }
}
