import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import $ from 'jquery';

import Popover from './Popover';

let formatBadgeCount = (n) => n > 99 ? '99+' : n;

export class MastheadPopoverIcons extends ReactComponent {

  constructor(props) {
    super(props);

    this.boundClickOffPopover = this.clickOffPopover.bind(this);

    this.state = {
      popover: false
    };
  }

  onClick(e, key) {
    e.preventDefault();

    if (key !== this.state.popover) {
      this.setState({
        popover: key
      });

      $(document).on('click', this.boundClickOffPopover);
    }
  }

  clickOffPopover(e) {
    if ($(e.target).parents('.popover-content').length == 0) {
      this.setState({
        popover: false
      });

      $(document).off('click', this.boundClickOffPopover);
    }
  }

  render() {
    let me = this;

    let children = this.props.children.map((child) =>
      React.cloneElement(child, {
        popoverActive: this.state.popover == child.key,
        onClick: (e) => me.onClick(e, child.key)
      })
    );

    return (
      <div className="masthead-popover-icons">
        {children}
      </div>
    );
  }

}

export class MastheadIcon extends ReactComponent {

  render() {
    return (
      <span>
      <a href="#" onClick={this.props.onClick} ref="icon" className={this.props.popoverActive ? 'masthead-popover-icon popover-active' : 'masthead-popover-icon'}>
        <i className={"fa fa-" + this.props.icon}>
          <span className="badge">{formatBadgeCount(this.props.badge)}</span>
        </i>
      </a>
        { this.props.popoverActive ?
          <Popover arrow attachTo={this.refs.icon} placement="bottom" height={300} width={300} top={-10} title={this.props.popoverTitle}>
            {this.props.children}
          </Popover>
          : null }
        </span>
    );
  }

}
