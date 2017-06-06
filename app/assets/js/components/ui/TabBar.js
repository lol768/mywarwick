import React, { Component, PropTypes } from 'react';

import $ from 'jquery';
import ReactDOM from 'react-dom';
import _ from 'lodash-es';
import { Routes } from '../AppRoot';

export default class TabBar extends Component {

  componentDidMount() {
    $(ReactDOM.findDOMNode(this)).on('touchmove', (e) => e.preventDefault());
  }

  getChildren() {
    return this.props.children.map((el) => (
      React.cloneElement(el, {
        key: el.props.title,
        ref: el.props.title.toLowerCase(),
        active: this.itemIsActive(el.props, this.props.selectedItem),
        onClick: () => this.props.onSelectItem(el.props.path),
        badge: el.props.badge,
        icon: el.props.icon,
      })
    ));
  }

  itemIsActive(item, currentPath) {
    if (item.path === '/' && (
      currentPath === '/' ||
        _.startsWith(currentPath, `/${Routes.TILES}/`) ||
        _.startsWith(currentPath, `/${Routes.EDIT}`)
    )) {
      return true;
    } else if (
      item.path === `/${Routes.NOTIFICATIONS}` &&
        _.startsWith(currentPath, `/${Routes.NOTIFICATIONS}`)
    ) {
      return true;
    }

    return item.path === currentPath;
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

TabBar.propTypes = {
  children: PropTypes.arrayOf(PropTypes.element),
  onSelectItem: PropTypes.func,
  selectedItem: PropTypes.string,
};
