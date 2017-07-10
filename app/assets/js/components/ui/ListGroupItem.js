import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';

export default class ListGroupItem extends React.PureComponent {

  static propTypes = {
    icon: PropTypes.string,
    description: PropTypes.string.isRequired,
    onClick: PropTypes.func,
    loading: PropTypes.bool,
    failure: PropTypes.bool,
    disabled: PropTypes.bool,
    uiControl: PropTypes.any,
  };

  static defaultProps = {
    loading: false,
    failure: false,
    disabled: false,
  };

  constructor(props) {
    super(props);
    this.onClick = this.onClick.bind(this);
  }

  onClick() {
    if (typeof this.props.onClick === 'function' && !this.props.disabled) {
      this.props.onClick();
    }
  }

  renderMediaRight() {
    if (this.props.loading) {
      return (
        <i className="fa fa-spinner fa-pulse" />
      );
    }

    if (this.props.failure) {
      return (
        <i className="fa fa-exclamation-triangle" />
      );
    }

    return this.props.uiControl;
  }

  render() {
    return (
      <div className="list-group-item" onClick={ this.onClick }>
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
            { this.renderMediaRight() }
          </div>
        </div>
      </div>
    );
  }

}
