import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import classNames from 'classnames';
import { connect } from 'react-redux';
import RequireUser from '../helpers/RequireUser';

let formatBadgeCount = (n) => n > 99 ? '99+' : n;

class TabBarItem extends ReactComponent {

  onClick() {
    if (!this.props.isDisabled) this.props.onClick(this);
  }

  render() {
    return (
      <li className={ classNames({
              'tab-bar-item': true,
              'tab-bar-item--active': this.props.active,
              'disabled' : this.props.isDisabled
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

let select = (state) => state.get('user').toJS();

export default connect(select)(RequireUser(TabBarItem));
