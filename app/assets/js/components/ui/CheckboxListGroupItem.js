import React from 'react';
import * as PropTypes from 'prop-types';
import classNames from 'classnames';

export default class CheckboxListGroupItem extends React.PureComponent {

  static propTypes = {
    id: PropTypes.string.isRequired,
    icon: PropTypes.string,
    description: PropTypes.string.isRequired,
    name: PropTypes.string,
    value: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired,
    checked: PropTypes.bool.isRequired,
    loading: PropTypes.bool,
    failure: PropTypes.bool,
    disabled: PropTypes.bool,
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
    if (this.props.disabled) return;
    this.props.onClick(this.props.value, this.props.name);
  }

  render() {
    let mediaRight = (<span
      className={ classNames('checkbox', {
        'checkbox--checked': this.props.checked,
        'checkbox--disabled': this.props.disabled,
      }) }
    >
      <i className="fa fa-check" />
    </span>);

    if (this.props.loading) {
      mediaRight = (
        <i className="fa fa-spinner fa-pulse" />
      );
    }

    if (this.props.failure) {
      mediaRight = (
        <i className="fa fa-exclamation-triangle" />
      );
    }
    return (
      <div className="list-group-item" onClick={ this.onClick }>
        <div className="media">
          { this.props.icon &&
          <div className="media-left">
            <i className={ `fa fa-fw fa-${this.props.icon}` } />
          </div> }
          <div className={`media-body${this.props.disabled ? ' media-body-disabled' : ''}`}>
            { this.props.description }
          </div>
          <div className="media-right">
            <div className="media-right">
              { mediaRight }
            </div>
          </div>
        </div>
      </div>
    );
  }

}
