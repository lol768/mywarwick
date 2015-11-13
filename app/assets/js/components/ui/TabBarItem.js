import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

let formatBadgeCount = (n) => n > 99 ? '99+' : n;

export default class TabBarItem extends ReactComponent {

  onClick() {
    this.props.onClick(this);
  }

  render() {
    return (
      <li className={this.props.active ? "tab-bar-item tab-bar-item--active" : "tab-bar-item"}
          onClick={this.onClick.bind(this)} ref="li">
        <i className={"fa fa-" + this.props.icon}>
          <span className="badge">{formatBadgeCount(this.props.badge)}</span>
        </i>
        <span className="tab-label">{this.props.title}</span>
      </li>
    );
  }

}