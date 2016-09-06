import React from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import classNames from 'classnames';
import { push } from 'react-router-redux';

import Popover from './Popover';

export default class MastheadNavItem extends React.Component {

  constructor(props) {
    super(props);

    this.boundClickOffPopover = this.clickOffPopover.bind(this);
    this.onClick = this.onClick.bind(this);

    this.state = {
      popoverPresented: false,
    };
  }

  onClick(e) {
    e.preventDefault();
    e.target.blur();

    if (this.props.disabled) {
      return;
    }

    if (this.state.popoverPresented) {
      this.dismissPopover();
    } else if (this.props.popover) {
      this.presentPopover();
    } else {
      this.props.dispatch(push(this.props.href));
    }
  }

  onMore() {
    this.dismissPopover();
    this.props.dispatch(push(this.props.href));
  }

  presentPopover() {
    this.setState({
      popoverPresented: true,
    });

    $(document).on('click', this.boundClickOffPopover);
  }

  dismissPopover() {
    this.setState({
      popoverPresented: false,
    });

    $(document).off('click', this.boundClickOffPopover);
  }

  clickOffPopover(e) {
    const node = $(ReactDOM.findDOMNode(this));

    if (node.has(e.target).length === 0) {
      this.dismissPopover();
    }
  }

  render() {
    const { disabled, pathname, href, title, icon, badge } = this.props;

    const active = pathname === href;

    return (
      <li className={ classNames({ disabled, active }) }>
        <a href={href} onClick={ this.onClick } ref="icon" title={title}
          className={classNames({ 'popover-active': this.state.popoverPresented })}
        >
          <i className={`fa fa-lg fa-${icon}`}> </i>
          <span className="hidden-sm">
            &nbsp;{title}
          </span>
          <Badge count={badge} />
        </a>
        { this.state.popoverPresented ?
          <Popover
            arrow attachTo={this.refs.icon} placement="bottom" height={300} width={300} top={-10}
            title={title}
            onMore={href ? this.onMore.bind(this) : null}
          >
            {this.props.children}
          </Popover>
          : null }
      </li>
    );
  }

}

MastheadNavItem.propTypes = {
  badge: React.PropTypes.number,
  children: React.PropTypes.node,
  disabled: React.PropTypes.bool,
  dispatch: React.PropTypes.func.required,
  href: React.PropTypes.string.required,
  icon: React.PropTypes.string.required,
  pathname: React.PropTypes.string.required,
  popover: React.PropTypes.bool,
  title: React.PropTypes.string.required,
};

const formatBadgeCount = (n) => (n > 99 ? '99+' : n);

function Badge({ count }) {
  if (count > 0) {
    return (
      <span>
        &nbsp;
        <span className="badge">{formatBadgeCount(count)}</span>
      </span>
    );
  }

  return null;
}

Badge.propTypes = {
  count: React.PropTypes.number,
};
