import React, { Component } from 'react';

import $ from 'jquery';
import ReactDOM from 'react-dom';

export default class TabBar extends Component {

  componentDidMount() {
    $(ReactDOM.findDOMNode(this)).on('touchmove', (e) => e.preventDefault());
  }

  getChildren() {
    return this.props.children.map((el) => (
      React.cloneElement(el, {
        key: el.props.title,
        ref: el.props.title.toLowerCase(),
        active: el.props.path === this.props.selectedItem,
        onClick: () => this.props.onSelectItem(el.props.path),
        badge: el.props.badge,
        icon: el.props.icon,
      })
    ));
  }

  render() {
    return (
      <nav className="tab-bar">
        <ul className="tab-bar__tabs">
          { this.getChildren() }
        </ul>
      </nav>
    );
  }

}
