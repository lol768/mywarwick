import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import classNames from 'classnames';

let formatBadgeCount = (n) => n > 99 ? '99+' : n;

export default class TabBarItem extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      disabled: this.props.isDisabled || false
    };
  }

  onClick() {
    if (!this.state.disabled) this.props.onClick(this);
  }

  render() {
    return (
      <li className={ classNames({
              'tab-bar-item': true,
              'tab-bar-item--active': this.props.active,
              'disabled' : this.state.disabled
          }) }
          onClick={this.onClick.bind(this)} ref="li">
        <i className={"fa fa-" + this.props.icon}>
          <span className="badge">{formatBadgeCount(this.props.badge)}</span>
        </i>
        <span className="tab-label">{this.props.title}</span>
      </li>
    );
  }

}
